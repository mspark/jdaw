package de.mspark.jdaw.guilds;

import java.util.List;

import de.mspark.jdaw.Command;
import de.mspark.jdaw.DistributionSetting;
import de.mspark.jdaw.JDAManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

// Disabled due to security concerns. 
//@CommandProperties(
//    trigger = "prefix", 
//    description = "Sets a custom prefix for the current guild",
//    userGuildPermissions = Permission.MANAGE_SERVER)
public class PrefixSetCommand extends Command {

    private final GuildRepository repo;
    public final GuildPrefixFilter prefixFilter;

    public PrefixSetCommand(GuildConfigService guildConfig, GuildRepository repo, JDAManager jdas, GuildPrefixFilter filter) {
        super(guildConfig, jdas, DistributionSetting.BALANCE);
        this.repo = repo;
        this.prefixFilter = filter;
    }

    @Override
    public void doActionOnCmd(Message msg, List<String> cmdArguments) {
        prefixFilter.filter(cmdArguments.get(0))
            .ifPresentOrElse(p -> setPrefix(msg, p), () -> msg.reply("Invalid prefix").submit());
    }

    private void setPrefix(Message msg, String prefix) {
        long guildId = msg.getGuild().getIdLong();
        var guildConfig = repo.findById(guildId)
            .map(g -> new CustomGuildConf(g.id(), prefix, g.whitelist()))
            .orElse(new CustomGuildConf(guildId, prefix));
        repo.save(guildConfig);
        if (repo.existsById(guildConfig.id())) {
            msg.reply("ok, prefix is set to " + prefix).submit();
        } else {
            msg.getChannel().sendMessage("This bot don't support a custom prefix").submit();
        }
    }

    @Override
    protected MessageEmbed commandHelpPage() {
        return new EmbedBuilder().setDescription("Change your prefix! :)").build();
    }
}
