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
import java.util.List;


public class CommandManager extends ListenerAdapter {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Dotenv config =  Dotenv.configure().load();
    private String user = "";

    String api = config.get("API");
    String get_plants = config.get("GET_PLANT");
    String get_measurements = config.get("GET_MEASUREMENTS");


    public void setUser(String user) { this.user = user; }

    private String getPlants(){
        Request request = new Request.Builder()
                .url(api+get_plants+user)
                .build();

        try(Response response = client.newCall(request).execute()) {
            if(response.isSuccessful()) {
                assert response.body() != null;
                String json = response.body().string();
                JsonArray plants = JsonParser.parseString(json).getAsJsonArray();

                StringBuilder reply = new StringBuilder();
                reply.append("Plants for user ").append(user).append(":\n");

                List<String> plantNames = new ArrayList<>();

                for (JsonElement plant : plants){
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

//    private String getMeasurements(int id){
//        Request request = new Request.Builder()
//                .url(api+get_measurements+id)
//                .build();
//
//        try(Response response = client.newCall(request).execute()) {
//            if(response.isSuccessful()) {
//                assert response.body() != null;
//                String json = response.body().string();
//                JsonArray plants = JsonParser.parseString(json).getAsJsonArray();
//                JsonElement first = plants.get(0);
//
////                "id": 21,
////    "lightIntensity": 53.37,
////    "temperatureMeasurement": 22.1,
////    "humidityMeasurement": 56.6,
////    "soilMeasurement": 3.52,
////    "plantId": 3,
////    "date": "2024-05-17T18:41:28.1818351"
//                int  lux = first.get
//                StringBuilder reply = new StringBuilder();
//                reply.append("Plant measurements for user ").append(user).append(":\n");
//
//                List<String> plantItems = new ArrayList<>();
//                int[] measurements = new int[4];
//
//                for (JsonElement plant : plants){
////                    JsonObject plantObject = plant.getAsJsonObject();
////                    String plantName = plantObject.get("plantTypeName").getAsString();
////                    plantNames.add(plantName);
//
//                    // The below code gets the whole JSON string if you need it
//                    String plantsString = gson.toJson(plant);
//                    reply.append(plantsString).append("\n");
//                }
//
//                String[] plantNamesArray = plantNames.toArray(new String[0]);
//
//                int i = 1;
//                for (String name : plantNamesArray) {
//                    reply.append(i++).append(". ").append(name).append("\n");
//                }
//
//                return reply.toString();
//            } else {
//                return "Failed to fetch platns. HTTP " + response.code();
//            }
//        } catch (Exception e) {
//            System.out.println("Error: " + e.getMessage());
//        }
//        return null;
//    }
//    }
//
//    private String parseData(String data) {
//        StringBuilder parsedReply = new StringBuilder();
//
//
//
//
//
//        return parsedReply.toString();
//    }

    private void setUserCommand(SlashCommandInteractionEvent event){
        OptionMapping messageOption = event.getOption("username");
        String username = messageOption.getAsString();
        setUser(username);

        event.reply("The username has been set to: **" + username + "**!").queue();
    }

    private void plantsCommand(SlashCommandInteractionEvent event){
        event.deferReply().queue();

        String reply = getPlants();

        assert reply != null;
        event.getHook().sendMessage(reply).queue();
    }

    private void checkCommand(SlashCommandInteractionEvent event){

    }

    private void measurementsCommand(SlashCommandInteractionEvent event){

    }
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandVar = event.getName();

        if(commandVar.equals("welcome")) {
            event.deferReply().queue();

            String name = event.getUser().getAsMention();
            event.getHook().sendMessage("Hi " + name).queue();
        } else if(commandVar.equals("roles")){
            String response = "";
            for(Role role : event.getGuild().getRoles()){
                response += role.getAsMention() + "\n";
            }

            event.reply(response).queue();
        } else if(commandVar.equals("set_user")){
            setUserCommand(event);
        } else if(commandVar.equals("check")){
            checkCommand(event);
        } else if(commandVar.equals("plants")){
            plantsCommand(event);
        }

    }

    private static List<CommandData> addCommands(){
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("welcome", "Welcomes the user to the server."));
        commandData.add(Commands.slash("roles", "Display all roles on the server."));
        commandData.add(Commands.slash("check", "Manually checks plants' status."));
        commandData.add(Commands.slash("plants", "Display all plants associated to the user."));
        commandData.add(Commands.slash("measurements", "Get current measurements for plant."));

        OptionData option1 = new OptionData(OptionType.STRING, "username", "The user you want to set", true);
        commandData.add(Commands.slash("set_user", "Set the user for which you want to retrieve information.").addOptions(option1));

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
