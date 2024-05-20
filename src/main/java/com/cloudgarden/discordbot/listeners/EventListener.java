package com.cloudgarden.discordbot.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EventListener extends ListenerAdapter {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private int flag =0;

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

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (flag == 0 && event.getAuthor().isBot()) {

            scheduler.schedule(() -> {
                event.getChannel().sendMessage("Number 3. Lilly needs to be watered").queue();
               flag = 1;
            }, 10, TimeUnit.SECONDS);
        }
//            String message = event.getMessage().getContentRaw();
//            if (message.equals("")) {
//                event.getChannel().sendMessage("Waiting for 10 seconds...").queue();
//        }
//        ) return;
//
//        String message = event.getMessage().getContentRaw();
//        if (message.equals("")) {
//            event.getChannel().sendMessage("Waiting for 10 seconds...").queue();
//
//            // Schedule a task to send a message after 10 seconds
//            scheduler.schedule(() -> {
//                event.getChannel().sendMessage("10 seconds have passed!").queue();
//            }, 10, TimeUnit.SECONDS);
//        }
    }

}