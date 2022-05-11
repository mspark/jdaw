package de.mspark.jdaw.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.mspark.jdaw.config.JDAManager;
import de.mspark.jdaw.guilds.GuildConfigService;
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
 * A new discord command wich has a top level command trigger and does somehting. The {@link TextCommand}
 * annotation is necessary for the concrecte implementation, in order to listen to events.
 *
 * @author marcel
 */
public class DiscordAction extends ListenerAdapter {

    private final TextCommand commandProperties;
    private final GuildConfigService guildConfig;

    /**
     * Defines a command. {@link TextCommand} is necessary to define properties.
     * 
     * @param conf
     * @param guilConfig
     * @param jdas
     * @param balanceSetting
     */
    public DiscordAction(GuildConfigService guildConfig, JDAManager jdas, TextCommand action) {
        this.guildConfig = guildConfig;
        this.commandProperties = action;
        commandProperties.distributionSetting().applySetting(jdas, this);
    }

    /**
     * A short description what this command is for. It is shown in the global command overview inside a big message
     * embeds.
     * 
     * @return
     */
    public String getShortDescription() {
        return commandProperties.description();
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

    protected boolean matchesTrigger(Message msg) {
        var arguments = getCmdArguments(msg);
        if (arguments.isEmpty())
            return false;
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
            commandProperties.doActionOnTrigger(event.getMessage(), arguments);
        } else {
            event.getChannel().sendMessage("Zu wenig Argumente!. Benutze den help befehl").submit();
        }
    }

    private boolean permissionCheck(MessageReceivedEvent event) {
        var missingUserPerm = extractMissingPermission(commandProperties.userGuildPermissions(),
            event.getMember().getPermissions());
        Member ownUser = event.getGuild().getSelfMember();
        var missingBotPerm = extractMissingPermission(commandProperties.botGuildPermissions(),
            ownUser.getPermissions());

        if (!missingUserPerm.isEmpty()) {
            var embed = new EmbedBuilder().setDescription("❌ Missing Permission:\n");
            missingUserPerm.forEach(missingPerm -> embed.appendDescription(missingPerm.name()));
            event.getChannel().sendMessageEmbeds(embed.build()).submit();
            return false;
        }
        if (!missingBotPerm.isEmpty()) {
            var embed = new EmbedBuilder()
                .setDescription("The bot needs the following permissions in order to execute the command:\n");
            missingBotPerm.forEach(missingPerm -> embed.appendDescription(missingPerm.name()));
            event.getChannel().sendMessageEmbeds(embed.build()).submit();
            return false; 
        }
        return true;
    }

    protected final List<String> getCmdArguments(Message msg) {
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
        return Optional.ofNullable(commandProperties.commandHelpPage()).map(EmbedBuilder::new)
            .map(e -> this.appendAliasesToEmbed(msg, e).build());
    }

    private EmbedBuilder appendAliasesToEmbed(Message msg, EmbedBuilder emb) {
        if (getAliases().length > 0) {
            List<String> allTrigger = new ArrayList<String>();
            allTrigger.add(getTrigger());
            allTrigger.addAll(List.of(getAliases()));
            String prefix = guildConfig.getPrefix(msg);
            String aliasAppendix = allTrigger.stream()
                .map(a -> prefix + a)
                .collect(Collectors.joining(", "));
            emb.setFooter("Aliases: " + aliasAppendix);
        }
        return emb;
    }

}
