package com.cloudgarden.discordbot.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class EventListener extends ListenerAdapter {

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

        String message = "You watered your plants " + desiredEmoji + " on "
                + reactionTimeLocal.format(DateTimeFormatter.ofPattern("dd/MM' at 'HH:mm:ss"));

        if (emoji.equals(desiredEmoji))
            channel.sendMessage(message).queue();
    }

}
