package de.mspark.jdaw.guilds;

import java.util.List;

import de.mspark.jdaw.config.JDAManager;
import de.mspark.jdaw.core.DistributionSetting;
import de.mspark.jdaw.core.TextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class PrefixSetCommand extends TextCommand {

    private final GuildRepository repo;
    private final GuildPrefixFilter prefixFilter;

    public PrefixSetCommand(GuildConfigService guildConfig, GuildRepository repo, JDAManager jdas, GuildPrefixFilter filter) {
        this.repo = repo;
        this.prefixFilter = filter;
    }

    @Override
    public void doActionOnTrigger(Message msg, List<String> cmdArguments) {
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
    public MessageEmbed commandHelpPage() {
        return new EmbedBuilder().setDescription("Change your prefix! :)").build();
    }

    @Override
    public String trigger() {
        return "prefix";
    }

    @Override
    public String description() {
        return "Sets a custom prefix for the current guild";
    }
    
    @Override
    public DistributionSetting distributionSetting() {
        return DistributionSetting.BALANCE;
    }
    
    @Override
    public Permission[] userGuildPermissions() {
        return new Permission[] {Permission.MANAGE_SERVER};
    }
}
