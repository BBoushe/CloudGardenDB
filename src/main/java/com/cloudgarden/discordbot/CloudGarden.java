package com.cloudgarden.discordbot;


import com.cloudgarden.discordbot.commands.CommandManager;
import com.cloudgarden.discordbot.listeners.EventListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CloudGarden {

    private final Dotenv config;
    private final ShardManager shardManager;

    public CloudGarden() throws LoginException {
        // Load environment variables
        config = Dotenv.configure().load();
        String token = config.get("TOKEN");

        // Build shard manager
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.listening("To your plants )_)"));
        shardManager = builder.build();

        // Register listeners
        shardManager.addEventListener(new EventListener(), new CommandManager());
    }

    public Dotenv getConfig() { return config; }

    public ShardManager getShardManager() { return shardManager; }

    public static void main(String[] args) {
        try {
            CloudGarden bot = new CloudGarden();
        } catch (LoginException e) {
            System.out.println("ERROR: Provided bot token is invalid!");
        }
    }
}
