package de.mspark.jdaw.maintainance.availability;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import de.mspark.jdaw.cmdapi.JdawState;
import de.mspark.jdaw.cmdapi.PermissionUtil;
import de.mspark.jdaw.cmdapi.TextCommand;
import de.mspark.jdaw.cmdapi.TextListenerAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public class BotCheckCommand extends TextCommand {

    private static class BotGuilds {
        private final JDA bot;
        private final List<Guild> guilds;
        private final boolean onServer;
        private final Member selfmember;
        private final boolean enoughPermission;
        private final Collection<Permission> botNeededPermissions;

        public BotGuilds(JDA jda, long currentGuildId, Collection<Permission> botNeededPermissions) {
            this.bot = jda;
            guilds = jda.getGuilds();
            onServer = guilds.stream().anyMatch(g -> g.getIdLong() == currentGuildId);
            selfmember = bot.getGuildById(currentGuildId).getSelfMember();
            enoughPermission = PermissionUtil.checkPermission(selfmember,
                    botNeededPermissions.toArray(Permission[]::new));
            this.botNeededPermissions = botNeededPermissions;
        }

        Collection<Permission> getMissingPermission() {
            return PermissionUtil.checkForMissingPermission(selfmember.getPermissions(), botNeededPermissions);
        }

    }

    private JDA[] allBots;
    private final Collection<Permission> botNeededPermissions;
    private List<TextListenerAction> enabledCommands = new LinkedList<>();

    public BotCheckCommand(Collection<Permission> botNeededPermissions) {
        this.botNeededPermissions = botNeededPermissions;
    }

    @Override
    public void onJdaRegistration(JdawState stateOnRegistration) {
        this.allBots = stateOnRegistration.jdaManager().getAllJdaRaw();
    }

    @Override
    public void onNewRegistration(JdawState stateOnRegistrationAttempt, TextListenerAction newRegisteredAction) {
        if (this.enabledCommands.isEmpty()) {
            this.enabledCommands.addAll(stateOnRegistrationAttempt.registeredActions());
        }
        this.enabledCommands.add(newRegisteredAction);
    }

    @Override
    public String trigger() {
        return "botcheck";
    }

    @Override
    public String description() {
        return "Check bot availability on this guild";
    }

    @Override
    public boolean executableWihtoutArgs() {
        return true;
    }

    @Override
    public Permission[] userGuildPermissions() {
        return new Permission[] { Permission.ADMINISTRATOR };
    }

    @Override
    public MessageEmbed commandHelpPage() {
        return newEmbed().setDescription("Arguments:")
                .addField(new Field("none",
                        "No Argument: This command will check if all configured Accounts are members of this guild. Afterwards it checks if those Accounts have sufficient permissions to function.",
                        false))
                .addField(new Field("cmd",
                        "Checks the necessary permissions on command level for the channel where this command is executed. This is done for all configured accounts separately. It list only the commands which can't function without additional permissions in this channel.",
                        false))
                .addField(new Field("invite", 
                        "Sends an invite for every account which will create and assign a role with sufficient permissions.", 
                        false))
                .build();
    }

    @Override
    public void onTrigger(Message msg, List<String> cmdArguments) {
        if (cmdArguments.isEmpty()) {
            checkPowerUps(msg);
        } else if (cmdArguments.get(0).equals("cmd")) {
            checkCmdInChannel(msg);
        } else if (cmdArguments.get(0).equals("invite")) {
            Arrays.stream(allBots).forEach(jda -> msg.getChannel().sendMessage(jda.getInviteUrl(botNeededPermissions)).queue());
        }
    }

    private void checkCmdInChannel(Message msg) {
        List<MessageEmbed> embeds = Arrays.stream(allBots)
                .map(jda -> jda.getGuildById(msg.getGuild().getId()))
                .map(g -> g.getSelfMember())
                .map(member -> checkCmdChannelPerms(member, msg.getGuildChannel(), msg.getMember())).toList();
        msg.getChannel().sendMessageEmbeds(embeds).queue();
    }

    private MessageEmbed checkCmdChannelPerms(Member member, GuildChannel guildChannel, Member cmdInvoker) {
        var embed = newEmbed().setTitle("Permissions for " + member.getEffectiveName())
                .setDescription(
                        "Below are all the commands that lack permissions to run in this channel. This checks affects only those commands, you have access to."
                        + "\n\n Mention for role changes: " + member.getAsMention());
        enabledCommands.stream()
                .filter(action -> action.userHasEnoughPermission(cmdInvoker))
                .map(TextListenerAction::getCommandSpecification)
                .forEach(cmd -> {
                    var missingPerms = PermissionUtil.checkForMissingPermission(guildChannel, member,
                            List.of(cmd.botGuildPermissions()));
                    if (!missingPerms.isEmpty()) {
                        String missingPermMessageString = permissionsAsDiscordMessageList(missingPerms);
                        embed.addField(new Field("CMD: " + cmd.trigger(), missingPermMessageString, false));
                    }
                });
        if (embed.getFields().isEmpty()) {
            embed.appendDescription("\n\n ✅ The bot has sufficient permission in this channel for all commands.");
        }
        return embed.build();
    }

    public void checkPowerUps(Message msg) {
        long currentGuild = msg.getGuild().getIdLong();
        List<BotGuilds> botGuildList = createBotList(currentGuild);
        String botOnGuildText = botOkMessage(botGuildList);
        String missingPerms = botMissingPermMessage(botGuildList);
        String missingBotText = missingBotInfoWithInvites(botGuildList);
        MessageEmbed embed = newEmbed()
                .setTitle("🔭 Bot check")
                .setDescription(botOnGuildText + "\n" + missingPerms + "\n" + missingBotText).build();
        msg.getChannel().sendMessageEmbeds(embed).submit();
    }

    private List<BotGuilds> createBotList(long currentGuild) {
        if (allBots == null) {
            throw new IllegalStateException("Command was not registered");
        }
        return Arrays.stream(allBots)
                .map(jda -> new BotGuilds(jda, currentGuild, botNeededPermissions))
                .toList();
    }

    private static String botMissingPermMessage(List<BotGuilds> botGuildList) {
        return botGuildList.stream()
                .filter(bg -> !bg.enoughPermission)
                .map(
                        bg -> "⚠️ %s lacks permissions\n➞ %s".formatted(bg.selfmember.getAsMention(),
                                permissionsAsDiscordMessageList(bg.getMissingPermission())))
                .collect(Collectors.joining("\n"));
    }

    private static String permissionsAsDiscordMessageList(Collection<Permission> perms) {
        String permsSeperated = perms.stream().map(Permission::getName).collect(Collectors.joining(", "));
        return "[" + permsSeperated + "]";
    }

    private static String botOkMessage(List<BotGuilds> botGuildList) {
        String botOnGuildText = botGuildList.stream()
                .filter(bg -> bg.onServer)
                .filter(bg -> bg.enoughPermission)
                .map(bg -> "✅ %s is on this Server".formatted(bg.bot.getSelfUser().getAsTag()))
                .collect(Collectors.joining("\n"));
        return botOnGuildText;
    }

    private String missingBotInfoWithInvites(List<BotGuilds> botGuildList) {
        String missingBotText = botGuildList.stream()
                .filter(bg -> !bg.onServer)
                .map(bg -> "❌ %s is not present on this Server. [Invite it.](%s) \n"
                        .formatted(bg.bot.getSelfUser().getAsTag(), bg.bot.getInviteUrl(botNeededPermissions)))
                .reduce((a, b) -> a + "\n" + b).orElse("\nEvery bot is up!");
        return missingBotText;
    }
    
    private EmbedBuilder newEmbed() {
        return new EmbedBuilder().setColor(new Color(64, 25, 255));
    }
}