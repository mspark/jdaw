package de.mspark.jdaw.maintainance;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import de.mspark.jdaw.cmdapi.JdawState;
import de.mspark.jdaw.cmdapi.TextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class BotCheckCommand extends TextCommand  {

    private static class BotGuilds {
        private final JDA bot;
        private final List<Guild> guilds;
        private final boolean onServer;

        public BotGuilds(JDA jda, long currentGuildId) {
            this.bot = jda;
            guilds = jda.getGuilds();
            onServer = guilds.stream().anyMatch(g -> g.getIdLong() == currentGuildId);
        }
    }

    private JDA[] allBots;
    private final Collection<Permission> botNeededPermissions;

    public BotCheckCommand(Collection<Permission> botNeededPermissions) {
        this.botNeededPermissions = botNeededPermissions;
    }

    @Override
    public void onJdaRegistration(JdawState stateOnRegistration) {
        this.allBots = stateOnRegistration.jdaManager().getAllJdaRaw();
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
    public void onTrigger(Message msg, List<String> cmdArguments) {
        checkPowerUps(msg);
    }

    @Override
    public MessageEmbed commandHelpPage() {
        return new EmbedBuilder().setDescription("No help page for this").build();
    }

    public void checkPowerUps(Message msg) {
        long currentGuild = msg.getGuild().getIdLong();
        List<BotGuilds> botGuildList = createBotList(currentGuild);
        String botOnGuildText = botOkMessage(botGuildList);
        String missingBotText = missingBotInfoWithInvites(botGuildList);
        MessageEmbed embed = new EmbedBuilder()
            .setTitle("🔭 Bot check")
            .setDescription(botOnGuildText + "\n" + missingBotText).build();
        msg.getChannel().sendMessageEmbeds(embed).submit();
    }

    private List<BotGuilds> createBotList(long currentGuild) {
        if (allBots == null) {
            throw new IllegalStateException("Command was not registered");
        }
        return Arrays.stream(allBots)
            .map(jda -> new BotGuilds(jda, currentGuild))
            .toList();
    }

    private static String botOkMessage(List<BotGuilds> botGuildList) {
        String botOnGuildText = botGuildList.stream()
            .filter(bg -> bg.onServer)
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

}