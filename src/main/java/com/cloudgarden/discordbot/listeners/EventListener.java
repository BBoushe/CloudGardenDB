package com.cloudgarden.discordbot.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.cloudgarden.discordbot.commands.CommandManager;
import net.dv8tion.jda.api.interactions.commands.Command;

public class EventListener extends ListenerAdapter {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean shouldExecuteCheck = false;
    private final CommandManager commandManager;

    public EventListener(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    // Used for printing time at which user has watered plant
    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        String emoji = event.getReaction().getEmoji().getAsReactionCode();
        String desiredEmoji = "\uD83D\uDCA6";

        MessageChannel channel = event.getChannel();

        Message msg = channel.retrieveMessageById(event.getMessageId()).complete();
        ZonedDateTime reactionTimeLocal = null;
        if (msg != null) {
            OffsetDateTime reactionTime = msg.getTimeCreated();
            reactionTimeLocal = reactionTime.atZoneSameInstant(ZoneId.of("Europe/Berlin"));
        } else {
            System.out.println("The message is null!");
        }

        String message = "You watered your plant " + desiredEmoji + " on "
                + reactionTimeLocal.format(DateTimeFormatter.ofPattern("dd/MM' at 'HH:mm:ss"));

        if (emoji.equals(desiredEmoji))
            channel.sendMessage(message).queue();
    }


    // used to notify user and periodically check status of the plant
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().contains("startChecking")) {
            shouldExecuteCheck = true;
            startPeriodicCheck(event.getChannel());
        } else if (event.getMessage().getContentRaw().equals("stopChecking")) { // not really useful but nice to have
            shouldExecuteCheck = false;
            event.getChannel().sendMessage("I'll no longer check your plants.").queue();
        }
    }

    private void startPeriodicCheck(MessageChannel channel) {
        scheduler.scheduleAtFixedRate(() -> {
            if (shouldExecuteCheck) {
                String response = commandManager.whoToWater();
                channel.sendMessage(response).queue();
            }
        }, 0, 15, TimeUnit.MINUTES);
    }
}
