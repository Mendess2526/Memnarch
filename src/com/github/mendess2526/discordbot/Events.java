package com.github.mendess2526.discordbot;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.events.TrackFinishEvent;
import sx.blah.discord.util.audio.events.TrackStartEvent;

import java.sql.Time;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class Events {
    public final static String BOT_PREFIX = "|";

    public static Map<String, Command> miscMap = new HashMap<>();
    public static Map<String, Command> sfxMap = new HashMap<>();
    public static Map<String, Command> rolechannelsMap = new HashMap<>();
    //public static Map<String, Command> greetingsMap = new HashMap<>();

    public static Map<String, Map<String, Command>> commandMap = new HashMap<>();

    static {
        miscMap.put("HELP", MiscCommands::help);

        miscMap.put("PING", MiscCommands::ping);

        miscMap.put("HI", MiscCommands::hi);

        miscMap.put("RESTART",MiscCommands::restart);

        miscMap.put("WHOAREYOU",MiscCommands::whoareyou);

        rolechannelsMap.put("ROLECHANNEL", (RoleChannels::handle));

        rolechannelsMap.put("JOIN", RoleChannels::showJoinableChannels);

        rolechannelsMap.put("LEAVE", RoleChannels::showLeavableChannels);

        sfxMap.put("SFX", AudioModule::sfx);

        sfxMap.put("SFXLIST", AudioModule::sfxlist);

        //greetingsMap.put("GREETME", Greetings::toggle);

        commandMap.put("Miscellaneous",miscMap);
        commandMap.put("Sfx",sfxMap);
        commandMap.put("Rolechannels",rolechannelsMap);
        //commandMap.put("Greetings",greetingsMap);

    }
    //TODO Greetings
    @EventSubscriber
    public void handleReactionEvent(ReactionEvent event) {
        // If it wasn't the bot adding the reaction and it was a reaction added my the bot
        if(event.getReaction().getUserReacted(event.getClient().getOurUser()) &&
                !(event.getUser().equals(event.getClient().getOurUser()))){

            List<IUser> mentions = event.getMessage().getMentions();
            if(mentions.isEmpty() || mentions.contains(event.getUser())){
                if(event.getReaction().getUnicodeEmoji().getAliases().get(0).equals("heavy_multiplication_x")){
                    LoggerService.log(event.getGuild(),event.getUser().getName()+" clicked an :heavy_multiplication_x:",LoggerService.INFO);
                    event.getMessage().delete();
                }else if(event.getMessage().getEmbeds().get(0).getTitle().equals("Select the channel you want to join!")){
                    //BotUtils.waitForReaction(event.getMessage(),"heavy_multiplication_x");
                    RoleChannels.join(event);
                }else if(event.getMessage().getEmbeds().get(0).getTitle().equals("Select the channel you want to leave!")) {
                    //BotUtils.waitForReaction(event.getMessage(),"heavy_multiplication_x");
                    RoleChannels.leave(event);
                }
            }
        }
    }
    @EventSubscriber
    public void handleUserJoin(UserVoiceChannelJoinEvent event) throws InterruptedException {
        if(event.getUser().isBot()){
            return;
        }
        Random rand = new Random();
        int randomNum = rand.nextInt(128);
        LoggerService.log(event.getGuild(),"Random number: "+randomNum,LoggerService.INFO);
        if(randomNum<2){
            TimeUnit.SECONDS.sleep(1);
            Greetings.greet(event);
        }
    }
    @EventSubscriber
    public void handleUserLeave(UserVoiceChannelLeaveEvent event){
        if(event.getVoiceChannel().getConnectedUsers().contains(event.getClient().getOurUser())
            && event.getVoiceChannel().getConnectedUsers().size()==1){
            event.getVoiceChannel().leave();
        }
    }
    @EventSubscriber
    public void handleTrackFinished(TrackFinishEvent event){
        LoggerService.log(event.getPlayer().getGuild(),"Scheduling leave",LoggerService.INFO);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Runnable leave = () -> event.getPlayer().getGuild().getConnectedVoiceChannel().leave();
        Main.leaveVoice = executor.schedule(leave,30, TimeUnit.MINUTES);
    }
    @EventSubscriber
    public void handleTrackStarted(TrackStartEvent event){
        if(Main.leaveVoice != null){
            Main.leaveVoice.cancel(true);
            LoggerService.log(event.getPlayer().getGuild(),"Leave canceled",LoggerService.INFO);
        }
    }
    @EventSubscriber
    public void handleMessageReceived(MessageReceivedEvent event) {
        // When @everyone is tagged with a ? in the same message
        if(event.getMessage().mentionsEveryone() && event.getMessage().getContent().contains("?")){
            RequestBuffer.request(() -> event.getMessage().addReaction(":white_check_mark:")).get();
            RequestBuffer.request(() -> event.getMessage().addReaction(":x:")).get();
            return;
        }

        String command[] = event.getMessage().getContent().toUpperCase().split("\\s");

        if(command.length == 0){
            return;
        }
        if(!command[0].startsWith(BOT_PREFIX)){
            return;
        }else{
            LoggerService.log(event.getGuild(),"Command: "+ Arrays.toString(command),LoggerService.INFO);
        }
        String cmd = command[0].substring(1);

        List<String> args = new ArrayList<>(Arrays.asList(command));
        args.remove(0);
        String[] key = commandMap.keySet()
                                 .stream()
                                 .filter(k -> commandMap.get(k).containsKey(cmd))
                                 .collect(Collectors.toSet())
                                 .toArray(new String[0]);
        if(key.length>1){
            BotUtils.contactOwner(event,"More then one command with the same name: "+event.getMessage().getContent());
            LoggerService.log(event.getGuild(),"There is more than one command group with the same command, contacting owner",LoggerService.ERROR);
            return;
        }
        if(key.length==1 && commandMap.containsKey(key[0]) && commandMap.get(key[0]).containsKey(cmd)){
            LoggerService.log(event.getGuild(),"Valid command: "+cmd, LoggerService.INFO);
            commandMap.get(key[0]).get(cmd).runCommand(event,args);
        }else{
            LoggerService.log(event.getGuild(),"Invalid command "+cmd, LoggerService.INFO);
        }
    }
}
