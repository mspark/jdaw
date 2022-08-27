package de.mspark.jdaw.guilds;

import java.util.List;
import java.util.Optional;

import de.mspark.jdaw.cmdapi.DistributionSetting;
import de.mspark.jdaw.cmdapi.TextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class PrefixSetCommand extends TextCommand {

    private final GuildRepository repo;
    private final GuildSettingsFinder settingsFinder;
    private final GuildPrefixFilter prefixFilter;

    public PrefixSetCommand(GuildRepository repo, GuildPrefixFilter filter) {
        this.repo = repo;
        this.prefixFilter = filter;
        this.settingsFinder = new GuildSettingsFinder(Optional.of(repo));
    }

    @Override
    public void onTrigger(Message msg, List<String> cmdArguments) {
        prefixFilter.filter(cmdArguments.get(0))
            .ifPresentOrElse(p -> setPrefix(msg, p), () -> msg.reply("Invalid prefix").submit());
    }

    private void setPrefix(Message msg, String newPrefix) {
        long gid = msg.getGuild().getIdLong();
        var guildConfig = settingsFinder.retrieveGuildSpecificSetting(msg)
                .orElse(repo.createAndSaveNew(gid, newPrefix));
        guildConfig.setListenPrefix(newPrefix);
        repo.save(guildConfig);

        if (repo.existsById(guildConfig.getGuildId())) {
            msg.reply("ok, prefix is set to " + newPrefix).submit();
        } else {
            msg.getChannel().sendMessage("This bot don't support a custom prefix").submit();
        }
    }

    @Override
    public boolean privateChatAllowed() {
        return false;
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
