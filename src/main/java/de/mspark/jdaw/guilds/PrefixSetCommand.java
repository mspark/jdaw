package de.mspark.jdaw.guilds;

import java.util.List;

import de.mspark.jdaw.cmdapi.DistributionSetting;
import de.mspark.jdaw.cmdapi.TextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class PrefixSetCommand extends TextCommand {

    private final GuildRepository repo;
    private final GuildPrefixFilter prefixFilter;

    public PrefixSetCommand(GuildRepository repo) {
        this.repo = repo;
        this.prefixFilter = requestedNewPrefix -> {
            boolean containsIllegalCharacter = requestedNewPrefix.startsWith(" ");
            return !containsIllegalCharacter;
        };
    }

    @Override
    public void onTrigger(Message msg, List<String> cmdArguments) {
        if (prefixFilter.isAllowed(cmdArguments.get(0))) {
            setPrefix(msg, cmdArguments.get(0).stripTrailing());
        } else {
            msg.reply("Invalid prefix").submit();
        }
    }

    private void setPrefix(Message msg, String newPrefix) {
        long gid = msg.getGuild().getIdLong();
        repo.findById(gid).ifPresentOrElse(
                gc -> {
                    gc.setListenPrefix(newPrefix); 
                    repo.save(gc); 
                }, 
                () -> createAndSaveNew(gid, newPrefix)
            );
        if (repo.existsById(gid)) {
            msg.reply("ok, prefix is set to " + newPrefix).submit();
        } else {
            msg.getChannel().sendMessage("This bot don't support a custom prefix").submit();
        }
    }

    private SingleGuildSettings createAndSaveNew(Long guildId, String prefix) {
        var setting = new SingleGuildSettings(guildId, prefix);
        repo.save(setting);
        return setting;
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
