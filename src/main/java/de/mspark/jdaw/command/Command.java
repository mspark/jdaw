package de.mspark.jdaw.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.mspark.jdaw.jda.JDAManager;
import de.mspark.jdaw.jda.JDAWConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * A new discord command wich has a top level command trigger and does somehting. The {@link CommandProperties} 
 * annotation is necessary for the concrecte implementation, in order to listen to events. 
 *
 * @author marcel
 */
public abstract class Command extends TextListener {
    private CommandProperties commandProperties;
    
    public Command(JDAWConfig conf, JDAManager jdas) {
        super(conf, jdas);
        var annotation = this.getClass().getAnnotation(CommandProperties.class);
        if (annotation == null) {
            var tsadas = this.getClass().getAnnotation(HelpCmd.class);
            annotation = tsadas.annotationType().getDeclaredAnnotation(CommandProperties.class);
        }
        commandProperties = annotation;
    }
    
    /**
     * The action which is executed when the command matches. 
     * 
     * @param event
     * @param cmd The list of addiotional arguments of the command. The trigger itself is not present. May be empty 
     *            when no arguments were given.
     */
    public abstract void doActionOnCmd(Message msg, List<String> cmdArguments);
    
    /**
     * Help page for this command with all subcommands. 
     * 
     * @return
     */
    public abstract MessageEmbed fullHelpPage();
   
    /**
     * A short description what this command is for. It is shown in the global command overview inside a big message
     * embeds.
     * 
     * @return
     */
    public abstract Field getShortDescription();
    

    @Override
    public void onTextMessageReceived(MessageReceivedEvent event) {
        System.out.println(1);
        var arguments = getCmdArguments(event.getMessage());
        if (matchesTrigger(event.getMessage())) {
            arguments.remove(0);
            invoke(event, arguments);
        }
    }
    
    protected boolean matchesTrigger(Message msg) {
        var arguments = getCmdArguments(msg);
        if (arguments.isEmpty()) return false;
        var cmd = arguments.get(0);
        return cmd.equalsIgnoreCase(getTrigger());
    }
    
    public String getTrigger() {
        return commandProperties.trigger();
    }

    private void invoke(MessageReceivedEvent event, List<String> arguments) {
        boolean enoughArguments = commandProperties.executableWihtoutArgs() || arguments.size() >= 1;
        var missingUserPerm = extractMissingPermission(commandProperties.userGuildPermissions(), event.getMember().getPermissions());
        Member ownUser = event.getGuild().getSelfMember();
        var missingBotPerm = extractMissingPermission(commandProperties.botGuildPermissions(), ownUser.getPermissions());
        
        if (!missingUserPerm.isEmpty()) {
            var embed = new EmbedBuilder().setDescription("❌ Missing Permission:\n");
            missingUserPerm.forEach(missingPerm -> embed.appendDescription(missingPerm.name()));
            event.getChannel().sendMessage(embed.build()).submit();
        }
        if (!missingBotPerm.isEmpty()) {
            var embed = new EmbedBuilder().setDescription("The bot needs the following permissions in order to execute the command:\n");
            missingBotPerm.forEach(missingPerm -> embed.appendDescription(missingPerm.name()));
            event.getChannel().sendMessage(embed.build()).submit();
        } else if (enoughArguments){
            doActionOnCmd(event.getMessage(), arguments);
        } else {
            event.getChannel().sendMessage("Zu wenig Argumente!. Benutze den help befehl").submit();
        }
    }

    protected List<String> getCmdArguments(Message msg) {
        String[] arguments = msg.getContentRaw().split("\\s+");
        if (arguments.length > 0 && arguments[0].startsWith(conf.prefix())) {
            arguments[0] = arguments[0].substring(1);
        }
        return new ArrayList<String>(Arrays.asList(arguments));
    }
    
    private List<Permission> extractMissingPermission(Permission[] neededPermission, Set<Permission> givenPermissions) {
        var neededPermList= new ArrayList<Permission>(Arrays.asList(neededPermission));
        neededPermList.removeAll(givenPermissions);
        return neededPermList;
    }
}
