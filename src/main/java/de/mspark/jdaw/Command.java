package de.mspark.jdaw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.mspark.jdaw.config.JDAWConfig;
import de.mspark.jdaw.help.EnableHelpCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
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
        this(conf, jdas, false);
    }
    
    /**
     * Defines a command. {@link CommandProperties} is necessary to define properties. 
     * 
     * @param conf
     * @param jdas
     * @param balance When true, the command is executed (and listens) on any configured discord bot otherwise its 
     *        always the main bot
     */
    public Command(JDAWConfig conf, JDAManager jdas, boolean balance) {
        super(conf, jdas, balance);
        var annotation = this.getClass().getAnnotation(CommandProperties.class);
        if (annotation == null) {
            var helpAnnontation = this.getClass().getAnnotation(EnableHelpCommand.class);
            annotation = helpAnnontation.annotationType().getDeclaredAnnotation(CommandProperties.class);
            if (annotation == null) {
                throw new Error("No annotation with properties found. Fix this and rebuild application");
            }
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
     * Help page for the command (typically explains all sub-commands). All command aliases are appended at the end. 
     * 
     * @return Is allowed to be <code>null</code> when {@link CommandProperties#helpPage()} is set to false
     */
    protected abstract MessageEmbed fullHelpPage();
   
    /**
     * A short description what this command is for. It is shown in the global command overview inside a big message
     * embeds.
     * 
     * @return
     */
    public String getShortDescription() {
        return commandProperties.helpPage() ? commandProperties.description() : null;
    }
    
    @Override
    public boolean isPrivateChatAllowed() {
        return commandProperties.privateChatAllowed();
    }

    @Override
    public final void onTextMessageReceived(MessageReceivedEvent event) {
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
        var allTrigger = new ArrayList<>(Arrays.asList(getAliases()));
        allTrigger.add(getTrigger());
        return allTrigger.stream().filter(t -> cmd.equalsIgnoreCase(t)).findAny().isPresent();
    }
    
    public String getTrigger() {
        return commandProperties.trigger();
    }
    
    public String[] getAliases() {
        return commandProperties.aliases();
    }

    private void invoke(MessageReceivedEvent event, List<String> arguments) {
        if (!globalBotAdminCheck(event.getAuthor())) {
            event.getAuthor().openPrivateChannel().complete().sendMessage("You are not allowed to invoke this command").submit();
            return;
        }
        if (event.isFromType(ChannelType.PRIVATE)) {
            invokeWithArguments(event, arguments);            
        } else if (permissionCheck(event)) {
            invokeWithArguments(event, arguments);
        }
    }

    private void invokeWithArguments(MessageReceivedEvent event, List<String> arguments) {
        boolean enoughArguments = commandProperties.executableWihtoutArgs() || arguments.size() >= 1;
        if (enoughArguments){
            doActionOnCmd(event.getMessage(), arguments);
        } else {
            event.getChannel().sendMessage("Zu wenig Argumente!. Benutze den help befehl").submit();
        }
    }

    private boolean permissionCheck(MessageReceivedEvent event) {
        var missingUserPerm = extractMissingPermission(commandProperties.userGuildPermissions(), event.getMember().getPermissions());
        Member ownUser = event.getGuild().getSelfMember();
        var missingBotPerm = extractMissingPermission(commandProperties.botGuildPermissions(), ownUser.getPermissions());
        
        if (!missingUserPerm.isEmpty()) {
            var embed = new EmbedBuilder().setDescription("❌ Missing Permission:\n");
            missingUserPerm.forEach(missingPerm -> embed.appendDescription(missingPerm.name()));
            event.getChannel().sendMessage(embed.build()).submit();
            return false;
        }
        if (!missingBotPerm.isEmpty()) {
            var embed = new EmbedBuilder().setDescription("The bot needs the following permissions in order to execute the command:\n");
            missingBotPerm.forEach(missingPerm -> embed.appendDescription(missingPerm.name()));
            event.getChannel().sendMessage(embed.build()).submit();
            return false; 
        }
        return true;
    }

    protected final List<String> getCmdArguments(Message msg) {
        String[] arguments = msg.getContentRaw().split("\\s+");
        if (arguments.length > 0 && arguments[0].startsWith(conf.prefix())) {
            arguments[0] = arguments[0].substring(1);
        }
        return new ArrayList<String>(Arrays.asList(arguments));
    }

    private static List<Permission> extractMissingPermission(Permission[] neededPermission, Set<Permission> givenPermissions) {
        var neededPermList= new ArrayList<Permission>(Arrays.asList(neededPermission));
        neededPermList.removeAll(givenPermissions);
        return neededPermList;
    }

    public final boolean userHasEnoughPermission(Message context) {
        var memberPerm = context.getMember().getPermissions();
        var missingPerms = extractMissingPermission(commandProperties.userGuildPermissions(), memberPerm);
        
        return missingPerms.isEmpty() && globalBotAdminCheck(context.getAuthor());
    }

    private boolean globalBotAdminCheck(User u) {
        if (commandProperties.botAdminOnly()) {
            return Arrays.stream(conf.botAdmins()).anyMatch(ba -> ba.equals(u.getId()));
        } 
        return true;
    }
    
    public final Optional<MessageEmbed> helpPage() {
        if (commandProperties.helpPage()) {
            var embed = Optional.of(fullHelpPage());
            if (getAliases().length > 0) {
                String aliasAppendix = Arrays.stream(getAliases()).map(a -> conf.prefix() + a).collect(Collectors.joining(", "));
                embed = Optional.of(
                        new EmbedBuilder(embed.orElseThrow()).appendDescription("\n\n *Aliases: " + aliasAppendix + "*").build()
                    );
            }
            return embed;
        } else {
            return Optional.empty();
        }
    }
    
}
