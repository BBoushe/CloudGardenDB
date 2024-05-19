package com.cloudgarden.discordbot.commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
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
    private static final String API = "https://ictfinal.azurewebsites.net/User/";
    private static final String getPlants = "GetPlants?username=";
    private String user = "";

    public void setUser(String user) { this.user = user; }


    String api = config.get("API");
    String get_plants = config.get("GET_PLANT");


    private static List<CommandData> addCommands(){
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("welcome", "Welcomes the user to the server."));
        commandData.add(Commands.slash("roles", "Display all roles on the server."));
        commandData.add(Commands.slash("check", "Manually checks plants' status."));

        OptionData option1 = new OptionData(OptionType.STRING, "username", "The user you want to set", true);
        commandData.add(Commands.slash("set_user", "Set the user for which you want to retrieve information.").addOptions(option1));

        return commandData;
    }

    private void setUserCommand(SlashCommandInteractionEvent event){
        OptionMapping messageOption = event.getOption("username");
        String username = messageOption.getAsString();
        setUser(username);

        event.reply("The username has been set to: **" + username + "**!").queue();
    }

    private void checkCommand(SlashCommandInteractionEvent event){
        event.deferReply().queue();

        OptionMapping messageOption = event.getOption("username");
        String username = messageOption.getAsString();
        setUser(username);

        Request request = new Request.Builder()
                .url(API+getPlants+user)
                .build();

        try(Response response = client.newCall(request).execute()) {
            if(response.isSuccessful()) {
                String json = response.body().string();
                JsonArray plants = JsonParser.parseString(json).getAsJsonArray();

                StringBuilder reply = new StringBuilder();
                reply.append("Plants for user ").append(user).append(":\n");

                for (JsonElement plant : plants){
                    String plantsString = gson.toJson(plant);
                    reply.append(plantsString).append("\n");
                }

                event.getHook().sendMessage(reply.toString()).queue();
            } else {
                event.getHook().sendMessage("Failed to fetch platns. HTTP " + response.code()).queue();
            }
        } catch (Exception e) {
            event.reply("Error: " + e.getMessage()).queue();
        }

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
            checkCommand(event);
        }

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
