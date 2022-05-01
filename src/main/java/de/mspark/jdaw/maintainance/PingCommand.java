package de.mspark.jdaw.maintainance;

import java.util.List;

import de.mspark.jdaw.core.TextCommand;
import net.dv8tion.jda.api.entities.Message;

public class PingCommand extends TextCommand {

    @Override
    public void doActionOnTrigger(Message msg, List<String> cmdArguments) {
        msg.reply("pong").submit();
    }

    @Override
    public String trigger() {
        return "ping";
    }

    @Override
    public String description() {
        return "Replies with pong";
    }
    
    @Override
    public boolean executableWihtoutArgs() {
        return true;
    }
}
