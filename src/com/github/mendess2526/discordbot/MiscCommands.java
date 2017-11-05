package com.github.mendess2526.discordbot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class MiscCommands{

    public static void help(MessageReceivedEvent event, List<String> args) {
        HashMap<String,Set<String>> cmds = new HashMap<>();
        Events.commandMap.keySet().forEach(k -> cmds.put(k,Events.commandMap.get(k).keySet()));
        BotUtils.help(event.getAuthor(),event.getChannel(),cmds);
    }

    public static void ping(MessageReceivedEvent event, List<String> args){
        LocalDateTime askTime = event.getMessage().getTimestamp();
        LocalDateTime respondTime = LocalDateTime.now();
        BotUtils.sendMessage(event.getChannel(),new EmbedBuilder().withTitle("Pong! "+(askTime.until(respondTime, ChronoUnit.MILLIS))+" ms").build(),120,true);
    }

    public static void hi(MessageReceivedEvent event, List<String> args) {
        BotUtils.sendMessage(event.getChannel(),"Hello, minion!",-1,false);
    }

    public static void shutdown(MessageReceivedEvent event, List<String> strings) {
        BotUtils.sendMessage(event.getChannel(),"Shutting down...",-1,false);
        IDiscordClient client = event.getClient();
        client.logout();
    }

    public static void whoAreYou(MessageReceivedEvent event, List<String> strings) {
        BotUtils.sendMessage(event.getChannel(),new EmbedBuilder().withTitle("I AM MEMNARCH").withImage("http://magiccards.info/scans/en/arc/112.jpg").withDesc("Sauce code: [GitHub](https://github.com/Mendess2526/Memnarch)").build(),-1,true);
    }
}
