package com.cloudgarden.discordbot.commands;

import com.google.gson.*;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CommandManager extends ListenerAdapter {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Dotenv config = Dotenv.configure().load();
    private String user = "";

    String api = config.get("API");
    String get_plants = config.get("GET_PLANT");
    String get_measurements = config.get("GET_MEASUREMENTS");

    private double[] measurementsArray = new double[4];


    public void setUser(String user) {
        this.user = user;
    }

    private double scale(double num, int in_min, int in_max, int l, int r) {
        return ((num - in_min) * (r - l) / (in_max - in_min) + l);
    }

    private String getPlants() {
        Request request = new Request.Builder()
                .url(api + get_plants + user)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                String json = response.body().string();
                JsonArray plants = JsonParser.parseString(json).getAsJsonArray();

                StringBuilder reply = new StringBuilder();
                reply.append("Plants for user ").append(user).append(":\n");

                List<String> plantNames = new ArrayList<>();

                for (JsonElement plant : plants) {
                    JsonObject plantObject = plant.getAsJsonObject();
                    String plantName = plantObject.get("plantTypeName").getAsString();
                    plantNames.add(plantName);

                    // The below code gets the whole JSON string if you need it
//                    String plantsString = gson.toJson(plant);
//                    reply.append(plantsString).append("\n");
                }

                String[] plantNamesArray = plantNames.toArray(new String[0]);

                int i = 1;
                for (String name : plantNamesArray) {
                    reply.append(i++).append(". ").append(name).append("\n");
                }

                return reply.toString();
            } else {
                return "Failed to fetch platns. HTTP " + response.code();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    private String getMeasurements(int id) {
        if(user.equals("")) {
            return "The user is not set. I can't check your plants! T_T";
        }

        Request request = new Request.Builder()
                .url(api + get_measurements + id)
                .build();

        double lux = 0;
        double temp = 0;
        double humidity = 0;
        double soil = 0;
        String time = "blank";

        StringBuilder responseString = new StringBuilder();


        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                String json = response.body().string();
                JsonArray plants = JsonParser.parseString(json).getAsJsonArray();

                for (JsonElement plant : plants) {
                    int plantId = plant.getAsJsonObject().get("plantId").getAsInt();

                    if (plantId == id) {
                        lux = plant.getAsJsonObject().get("lightIntensity").getAsDouble();
                        temp = plant.getAsJsonObject().get("lightIntensity").getAsDouble();
                        humidity = plant.getAsJsonObject().get("lightIntensity").getAsDouble();
                        soil = plant.getAsJsonObject().get("lightIntensity").getAsDouble();
                        time = plant.getAsJsonObject().get("date").getAsString();

                        // to be used elsewhere
                        measurementsArray[0] = lux;
                        measurementsArray[1] = temp;
                        measurementsArray[2] = humidity;
                        measurementsArray[3] = soil;

                        break;
                    } else {
                        System.out.println("No plant with that ID found in response!");
                    }
                }

                responseString.append("Light intensity: ").append(lux).append("\n");
                responseString.append("Temperature measurement: ").append(temp).append("\n");
                responseString.append("Humidity measurement: ").append(humidity).append("\n");
                responseString.append("Hoil measurement: ").append(soil).append("\n");

                return responseString.toString();

            } else {
                return "Failed to fetch platns. HTTP " + response.code();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public String whoToWater() {
        String plants = getPlants();
        String allPlants[] = plants.split("/r/\n|\r|\n");
        System.out.println(Arrays.stream(allPlants).toList());
        int numberOfPlants = allPlants.length-1; // skips the first line!

        StringBuilder responseString = new StringBuilder();

        for(int i = 1; i < numberOfPlants+1; i++){
            String measurements = getMeasurements(i); // we only do this for the double[] array with measurements

            if (measurements == null) {
                responseString.append("Measurements for plant ").append(i).append(" are empty.");
                continue;
            }

            if((measurementsArray[0] > 30
                    || measurementsArray[1] > 30 || measurementsArray[2] > 30 || measurementsArray[3] > 30)) {
                responseString.append(allPlants[i].replaceAll("[^a-zA-Z]", "")).append(" needs to be watered.\n");
            }

        }

        return responseString.toString();
    }


    private void setUserCommand(SlashCommandInteractionEvent event) {
        OptionMapping messageOption = event.getOption("username");
        String username = messageOption.getAsString();
        setUser(username);

        event.reply("The username has been set to: **" + username + "**!").queue();
    }

    private void plantsCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String reply = getPlants();

        assert reply != null;
        event.getHook().sendMessage(reply).queue();
    }

    private void checkCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        String response = whoToWater();

        event.getHook().sendMessage(response).queue();
    }

    private void measurementsCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        int id_of_plant = event.getOption("plant_id").getAsInt();

        String response = getMeasurements(id_of_plant);

        event.getHook().sendMessage("Measurements for plant " + id_of_plant + " are: \n" + response).queue();
    }

    private void helpCommand(SlashCommandInteractionEvent event) {
        event.reply("This discord bot helps you remember to water your plants by monitoring them." +
                " Using sensors it monitors things like lux, moisture humidity and air humidity to tell you when it's the right" +
                " time to water them. \n \n First and most importantly you have to set the user for which the bot will check" +
                " by using the /set_user command followed by the user. You can find your user inside the IOS application. \n" +
                "After that is done you have to type startChecking in the same channel and the bot will check the status of your plants" +
                " every 15 minutes. To stop checking type stopChecking. \n" +
                "Other commands include check to manually check the status of the plants; plants to get a list of all plants connected; " +
                "And of course the help command").queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandVar = event.getName();

        if (commandVar.equals("set_user")) {
            setUserCommand(event);
        } else if (commandVar.equals("check")) {
            checkCommand(event);
        } else if (commandVar.equals("plants")) {
            plantsCommand(event);
        } else if (commandVar.equals("measurements")) {
            measurementsCommand(event);
        } else if (commandVar.equals("help")){
            helpCommand(event);
        }

    }

    private static List<CommandData> addCommands() {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("help", "Displays information about the bot and how to use it."));
        commandData.add(Commands.slash("roles", "Display all roles on the server."));
        commandData.add(Commands.slash("check", "Manually checks plants' status."));
        commandData.add(Commands.slash("plants", "Display all plants associated to the user."));

        OptionData option1 = new OptionData(OptionType.STRING, "username", "The user you want to set", true);
        OptionData option2 = new OptionData(OptionType.INTEGER, "plant_id", "The plant for which measures we need", true);
        commandData.add(Commands.slash("set_user", "Set the user for which you want to retrieve information.").addOptions(option1));
        commandData.add(Commands.slash("measurements", "Get current measurements for plant.").addOptions(option2));

        return commandData;
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        event.getGuild().updateCommands().addCommands(addCommands()).queue();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        event.getGuild().updateCommands().addCommands(addCommands()).queue();
    }

    // Uncomment this when the bot is production ready
//    @Override
//    public void onReady(ReadyEvent event) {
//        event.getJDA().updateCommands().addCommands(addCommands()).queue();
//    }
}
