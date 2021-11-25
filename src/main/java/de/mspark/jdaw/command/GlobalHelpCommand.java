package de.mspark.jdaw.command;

import java.util.List;

import de.mspark.jdaw.jda.JDAManager;
import de.mspark.jdaw.jda.JDAWConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

/**
 * When implementing this with the {@link HelpCmd} Annotation, a help command will be available.
 *
 * @author marcel
 */
public abstract class GlobalHelpCommand extends Command {
    
    protected List<Command> allLoadedCmds; 
    
    public GlobalHelpCommand(JDAWConfig conf, JDAManager jdas, List<Command> allLoadedCmds) {
        super(conf, jdas);
        this.allLoadedCmds = allLoadedCmds;
    }

    @Override
    public void doActionOnCmd(Message msg, List<String> cmdArguments) {
        if (cmdArguments.isEmpty()) {
            var eb = new EmbedBuilder().setDescription(botDescription());
            allLoadedCmds.stream()
                .filter(cmd -> cmd.userHasEnoughPermission(msg))
                .map(m -> m.getShortDescription())
                .forEach(eb::addField);
            msg.getChannel().sendMessage(eb.build()).submit();
        } else {
            String wantedHelpPage = cmdArguments.get(0);
            allLoadedCmds.stream()
                .filter(c -> c.getTrigger().equalsIgnoreCase(wantedHelpPage))
                .findFirst()
                .filter(cmd -> cmd.userHasEnoughPermission(msg))
                .map(Command::fullHelpPage)
                .ifPresent(helpPage -> msg.getChannel().sendMessage(helpPage).submit());
        }
    }
    
    @Override
    public MessageEmbed fullHelpPage() {
        throw new UnsupportedOperationException();
    }
    
    public abstract String botDescription();

    @Override
    public Field getShortDescription() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public final String getTrigger() {
        return "help";
    }
    
}
