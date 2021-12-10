package de.mspark.jdaw.commands;

import java.util.List;
import java.util.Optional;

import de.mspark.jdaw.Command;
import de.mspark.jdaw.CommandProperties;
import de.mspark.jdaw.JDAManager;
import de.mspark.jdaw.config.JDAWConfig;
import de.mspark.jdaw.guilds.GuildConfigService;
import de.mspark.jdaw.guilds.GuildRepository;
import de.mspark.jdaw.guilds.model.CustomGuildConf;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@CommandProperties(
	trigger = "prefix", 
	description = "Sets a custom prefix for the current guild",
	userGuildPermissions = Permission.MANAGE_SERVER, 
	helpPage = false)
public class PrefixSetCommand extends Command {
    private GuildRepository repo;
    
    public PrefixSetCommand(JDAWConfig config, GuildConfigService guildConfig, GuildRepository repo, JDAManager jdas) {
        super(config, guildConfig, jdas, true);
        this.repo = repo;
    }

    @Override
    public void doActionOnCmd(Message msg, List<String> cmdArguments) {
        String prefix = filter(cmdArguments.get(0)).get();
        long guildId = msg.getGuild().getIdLong();
        var guildConfig = repo.findOne(guildId)
            .map(g -> new CustomGuildConf(g.id(), prefix, g.whitelist()))
            .orElse(new CustomGuildConf(guildId, prefix));
        repo.save(guildConfig);
        if (repo.exists(guildConfig.id())) {
        	msg.reply("ok, prefix is set to " + prefix).submit();
        } else {
        	msg.getChannel().sendMessage("This bot don't support a custom prefix").submit();
        }
    }

    @Override
    protected MessageEmbed fullHelpPage() {
        return null;
    }
    
    // !!! SQL Injection possible - use this just as an example !!!
    public Optional<String> filter(String desiredPrefix) {
        return Optional.of(desiredPrefix);
    }
}
