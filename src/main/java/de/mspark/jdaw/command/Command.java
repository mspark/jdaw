package de.mspark.jdaw.command;

import java.util.Arrays;
import java.util.List;

import de.mspark.jdaw.jda.JDAManager;
import de.mspark.jdaw.jda.JDAWConfig;
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
        jdas.getMain().addEventListener(this);
    }
    
    /**
     * The action which is executed when the command matches. 
     * @param event
     * @param cmd Command with arguments without prefix
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
        var arguments = getCmdArguments(event.getMessage());
        if (matchesTrigger(event.getMessage())) {
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
        boolean enoughArguments = commandProperties.executableWihtoutArgs() || arguments.size() > 1;
        
        if (!permissionUserCheck(event)) {
            event.getChannel().sendMessage("❌ Missing Permission").submit(); // TODO write what permission
        } else if (enoughArguments && botPermissionCheck(event)) {
            doActionOnCmd(event.getMessage(), arguments);
        } else {
            event.getChannel().sendMessage("Zu wenig Argumente!. Benutze den help befehl").submit();
        }
    }

    protected List<String> getCmdArguments(Message msg) {
        // TODO remove first argument
        String[] arguments = msg.getContentRaw().split("\\s+");
        if (arguments.length > 0 && arguments[0].startsWith(conf.prefix())) {
            arguments[0] = arguments[0].substring(1); // remove prefix
        }
        return Arrays.asList(arguments);
    }
    
    private boolean permissionUserCheck(MessageReceivedEvent event) {
        return event.getMember().getPermissions().containsAll(Arrays.asList(commandProperties.userGuildPermissions()));
    }
   
    protected boolean botPermissionCheck(MessageReceivedEvent event) {
        var gPerm = commandProperties.botGuildPermissions();
        boolean permissionAvailable = true;
        Member ownUser = event.getGuild().getSelfMember();
        if (!ownUser.getPermissions().containsAll(Arrays.asList(gPerm))) {
            event.getChannel().sendMessage("Folgende Serverrechte sind für diesen Befehl notwendig:" + Arrays.toString(gPerm)).submit();
            permissionAvailable = false;
        }
        return permissionAvailable;
    }
}
