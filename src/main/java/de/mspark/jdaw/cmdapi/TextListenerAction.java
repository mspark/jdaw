package de.mspark.jdaw.cmdapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.mspark.jdaw.guilds.GuildConfigService;
import de.mspark.jdaw.startup.JDAManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Decorator for {@link TextCommand}. It has the core functionality of a text command including permission management.
 * 
 * @author marcel
 * @see de.mspark.jdaw.cmdapi.TextCommand
 */
public final class TextListenerAction extends ListenerAdapter implements Triggerable {

    private final TextCommand commandProperties;
    private final GuildConfigService guildConfig;

    /**
     * Defines a command. {@link TextCommand} is necessary to define properties.
     * 
     * @param conf
     * @param guilConfig
     * @param balanceSetting
     */
    public TextListenerAction(GuildConfigService guildConfig, TextCommand action) {
        this.guildConfig = guildConfig;
        this.commandProperties = action;
    }
    
    /**
     * The action will listen on text events. 
     * 
     * @param jdas
     */
    public void startListenOnDiscordEvents(JDAManager jdas) {
        commandProperties.distributionSetting().applySetting(jdas, this);
        commandProperties.onJdaRegistration(new JdawState(List.of(this), guildConfig, jdas));
    }

    public String description() {
        return commandProperties.description();
    }

    public String trigger() {
        return commandProperties.trigger();
    }

    public String[] aliases() {
        return commandProperties.aliases();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (checkAllowedScope(event)) {
            var arguments = getCmdArguments(event.getMessage());
            if (matchesTrigger(event.getMessage())) {
                arguments.remove(0);
                invoke(event, arguments);
            }
        }
    }
    
    private boolean checkAllowedScope(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            return commandProperties.privateChatAllowed();
        } else {
            return event.isFromType(ChannelType.TEXT) && isChannelAllowed(event);
        }
    }

    private boolean isChannelAllowed(MessageReceivedEvent event) {
        var whitelist = guildConfig.getWhitelistChannel(event);
        return whitelist.isEmpty() || whitelist.contains(event.getChannel().getId());
    }

    private boolean matchesTrigger(Message msg) {
        var arguments = getCmdArguments(msg);
        if (arguments.isEmpty())
            return false;
        var cmd = arguments.get(0);
        var allTrigger = new ArrayList<>(Arrays.asList(aliases()));
        allTrigger.add(trigger());
        return allTrigger.stream().filter(t -> cmd.equalsIgnoreCase(t)).findAny().isPresent();
    }

    private void invoke(MessageReceivedEvent event, List<String> arguments) {
        if (!globalBotAdminCheck(event.getAuthor())) {
            event.getAuthor().openPrivateChannel().complete().sendMessage("You are not allowed to invoke this command")
                .submit();
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
        if (enoughArguments) {
            commandProperties.onTrigger(event.getMessage(), arguments);
        } else {
            event.getChannel().sendMessage("Zu wenig Argumente!. Benutze den help befehl").submit();
        }
    }

    private boolean permissionCheck(MessageReceivedEvent event) {
        var permsMissingUser = extractMissingPermission(commandProperties.userGuildPermissions(),
            event.getMember().getPermissions());
        Member ownUser = event.getGuild().getSelfMember();
        var permsMissingBot = extractMissingPermission(commandProperties.botGuildPermissions(),
            ownUser.getPermissions());

        if (!permsMissingUser.isEmpty()) {
            var embed = new EmbedBuilder().setDescription("❌ Missing Permission:\n");
            permsMissingUser.forEach(missingPerm -> embed.appendDescription(missingPerm.name()));
            event.getChannel().sendMessageEmbeds(embed.build()).submit();
            return false;
        }
        if (!permsMissingBot.isEmpty()) {
            var embed = new EmbedBuilder()
                .setDescription("The bot needs the following permissions in order to execute the command:\n");
            permsMissingBot.forEach(missingPerm -> embed.appendDescription(missingPerm.name()));
            event.getChannel().sendMessageEmbeds(embed.build()).submit();
            return false; 
        }
        return true;
    }

    private final List<String> getCmdArguments(Message msg) {
        String[] arguments = msg.getContentRaw().split("\\s+");
        String prefix = guildConfig.getPrefix(msg);
        if (arguments.length > 0 && arguments[0].startsWith(prefix)) {
            arguments[0] = arguments[0].substring(prefix.length());
        }
        return new ArrayList<String>(Arrays.asList(arguments));
    }

    private static List<Permission> extractMissingPermission(Permission[] neededPermission,
        Set<Permission> givenPermissions) {
        var neededPermList = new ArrayList<Permission>(Arrays.asList(neededPermission));
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
            return Arrays.stream(guildConfig.globalConfig().botAdmins()).anyMatch(ba -> ba.equals(u.getId()));
        }
        return true;
    }

    public Optional<MessageEmbed> helpPageWithAliases(Message msg) {
        return Optional.ofNullable(commandProperties.commandHelpPage())
                .map(EmbedBuilder::new)
                .map(e -> this.appendAliasesToEmbed(msg, e).build());
    }

    private EmbedBuilder appendAliasesToEmbed(Message msg, EmbedBuilder emb) {
        if (aliases().length > 0) {
            List<String> allTrigger = new ArrayList<String>();
            allTrigger.add(trigger());
            allTrigger.addAll(List.of(aliases()));
            String prefix = guildConfig.getPrefix(msg);
            String aliasAppendix = allTrigger.stream()
                .map(a -> prefix + a)
                .collect(Collectors.joining(", "));
            emb.setFooter("Aliases: " + aliasAppendix);
        }
        return emb;
    }

}
