package de.mspark.jdaw.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.mspark.jdaw.Command;
import de.mspark.jdaw.CommandProperties;
import de.mspark.jdaw.DistributionSetting;
import de.mspark.jdaw.JDAManager;
import de.mspark.jdaw.guilds.GuildConfigService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@CommandProperties(
    trigger = "botcheck", description = "Check bot availability on this guild",
    userGuildPermissions = Permission.MANAGE_SERVER, 
    executableWihtoutArgs = true)
public class BotCheckCommand extends Command {

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

    public BotCheckCommand(GuildConfigService guilConfig, JDAManager jdas, JDA[] allJDA) {
        super(guilConfig, jdas, DistributionSetting.BALANCE);
        this.allBots = allJDA;
    }

    @Override
    public void doActionOnCmd(Message msg, List<String> cmdArguments) {
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
        msg.getChannel().sendMessage(embed).submit();
    }

    private List<BotGuilds> createBotList(long currentGuild) {
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

    private static String missingBotInfoWithInvites(List<BotGuilds> botGuildList) {
        var perms = new Permission[] {Permission.VOICE_MOVE_OTHERS};
        String missingBotText = botGuildList.stream()
                .filter(bg -> !bg.onServer)
                .map(bg -> "❌ %s is not present on this Server. [Invite it.](%s) \n"
                .formatted(bg.bot.getSelfUser().getAsTag(), bg.bot.getInviteUrl(perms)))
                .reduce((a,b) -> a + "\n" + b).orElse("\nEvery bot is up!");
        return missingBotText;
    }

}