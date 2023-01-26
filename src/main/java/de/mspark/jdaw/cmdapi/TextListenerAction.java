package de.mspark.jdaw.cmdapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.mspark.jdaw.guilds.GuildSettingsFinder;
import de.mspark.jdaw.startup.JDAManager;
import de.mspark.jdaw.startup.JdawConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Decorator for {@link TextCommand}. It has the core functionality of a text command including permission management.
 * 
 * @author marcel
 * @see de.mspark.jdaw.cmdapi.TextCommand
 */
public final class TextListenerAction extends ListenerAdapter {

    private final TextCommand commandProperties;
    private final GuildSettingsFinder guildConfig;
    private final JdawConfig jdawConfig;
    private JDA[] selfBots;

    /**
     * Defines a command. {@link TextCommand} is necessary to define properties.
     * 
     * @param conf
     * @param guilConfig
     * @param balanceSetting
     */
    public TextListenerAction(JdawState state, TextCommand commandDefinition) {
        this.guildConfig = state.guildConfig();
        this.commandProperties = commandDefinition;
        this.jdawConfig = state.globalConfig();
    }
    
    /**
     * The action will listen on text events. 
     * 
     * @param jdas
     */
    public void startListenOnDiscordEvents(JDAManager jdas) {
        this.selfBots = commandProperties.distributionSetting().applySetting(jdas, this);
        commandProperties.onJdaRegistration(new JdawState(List.of(this), guildConfig, jdas, jdawConfig));
    }
    
    // in favor of getCommandSpecification()
    @Deprecated(since = "6.3")
    public String description() {
        return commandProperties.description();
    }
    
    // in favor of getCommandSpecification()
    @Deprecated(since = "6.3")
    public String trigger() {
        return commandProperties.trigger();
    }

    // in favor of getCommandSpecification()
    @Deprecated(since = "6.3")
    public String[] aliases() {
        return commandProperties.aliases();
    }

    public TextCommand getCommandSpecification() {
        return this.commandProperties;
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (Stream.of(selfBots).map(b -> b.getSelfUser().getId()).anyMatch(event.getAuthor().getId()::equals)) {
            return;
        }
        if (!checkAllowedScope(event)) {
            return;
        }
        if (!event.getMessage().getContentStripped().startsWith(getPrefix(event.getMessage()))) {
            return;
        }
        if (!matchesTrigger(event.getMessage())) {
            return;
        }
        var arguments = getCmdArguments(event.getMessage());
        arguments.remove(0);
        invokeOnTrigger(event.getMessage(), arguments);
    }
    
    private boolean checkAllowedScope(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            return commandProperties.privateChatAllowed();
        } else {
            return event.isFromType(ChannelType.TEXT) && isChannelAllowed(event);
        }
    }

    private boolean isChannelAllowed(MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return true;
        }
        var whitelist = guildConfig.getWhitelistChannel(event.getMessage().getGuild().getIdLong());
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
    
    private void invokeOnTrigger(Message event, List<String> arguments) {
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

    private void invokeWithArguments(Message event, List<String> arguments) {
        boolean enoughArguments = commandProperties.executableWihtoutArgs() || arguments.size() >= 1;
        if (enoughArguments) {
            commandProperties.onTrigger(event, arguments);
        } else {
            event.getChannel().sendMessage("Zu wenig Argumente!. Benutze den help befehl").submit();
        }
    }

    private boolean permissionCheck(Message event) {
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
        String prefix = getPrefix(msg);
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

    public final boolean userHasEnoughPermission(Member memberToCheck) {
        var memberPerm = memberToCheck.getPermissions();
        var missingPerms = extractMissingPermission(commandProperties.userGuildPermissions(), memberPerm);

        return missingPerms.isEmpty() && globalBotAdminCheck(memberToCheck.getUser());
    }

    private boolean globalBotAdminCheck(User u) {
        if (commandProperties.botAdminOnly()) {
            return Arrays.stream(jdawConfig.botAdmins()).anyMatch(ba -> ba.equals(u.getId()));
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
            String prefix = getPrefix(msg);
            String aliasAppendix = allTrigger.stream()
                .map(a -> prefix + a)
                .collect(Collectors.joining(", "));
            emb.setFooter("Aliases: " + aliasAppendix);
        }
        return emb;
    }
    
    private String getPrefix(Message msg) {
        try {
            var guild = msg.getGuild();
            return guildConfig.getGuildSpecificPrefix(guild.getIdLong()).orElse(jdawConfig.defaultPrefix());
        } catch (IllegalStateException ex) {
            return jdawConfig.defaultPrefix();
        }
    }

}
