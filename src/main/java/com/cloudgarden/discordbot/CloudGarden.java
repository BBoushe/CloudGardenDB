package com.cloudgarden.discordbot;

import com.cloudgarden.discordbot.listeners.EventListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManager;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.security.auth.login.LoginException;

public class CloudGarden {

    private final ShardManager shardManager; // used to provide bot functionality. This is not the only option
    private final Dotenv config; // used to hide the TOKEN and other sensitive information

    public CloudGarden() throws LoginException {
        config = Dotenv.configure().load();

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(config.get("TOKEN"));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.listening("To your plants O_O"));
        shardManager = builder.build();

        //Register Listeners
        shardManager.addEventListener(new EventListener());
    }

    public Dotenv getConfig(){
        return config;
    }

    public ShardManager getShardManager(){
        return shardManager;
    }

    public static void main(String[] args) {
        try {
            CloudGarden bot = new CloudGarden();
        } catch (LoginException e) {
            System.out.println("ERROR: Provided bot token is invalid");
        }

    }
}
