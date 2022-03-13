package de.mspark.jdaw.commands;

import java.util.List;

import de.mspark.jdaw.Command;
import de.mspark.jdaw.CommandProperties;
import de.mspark.jdaw.JDAManager;
import de.mspark.jdaw.DistributionSetting;
import de.mspark.jdaw.config.JDAWConfig;
import de.mspark.jdaw.guilds.GuildConfigService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@CommandProperties(trigger = "ping", description = "Replies with pong", helpPage = false, executableWihtoutArgs = true)
public class PingCommand extends Command {

    public PingCommand(JDAWConfig conf, GuildConfigService gc, JDAManager jdas) {
        super(conf, gc, jdas, DistributionSetting.ALL);
    }

    @Override
    public void doActionOnCmd(Message msg, List<String> cmdArguments) {
        msg.reply("pong").submit();
    }

    @Override
    protected MessageEmbed fullHelpPage() {
        return null;
    }

}
