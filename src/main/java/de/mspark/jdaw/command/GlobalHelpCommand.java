package de.mspark.jdaw.command;

import java.util.List;

import de.mspark.jdaw.jda.JDAManager;
import de.mspark.jdaw.jda.JDAWConfig;
import net.dv8tion.jda.api.entities.Message;
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
        if (cmdArguments.size() == 1) {
            msg.getChannel().sendMessage(fullHelpPage()).submit();            
        } else {
            String wantedHelpPage = cmdArguments.get(1);
            if (wantedHelpPage.equals("intro")) {
                sendIntroduction(msg);
            } else {
                allLoadedCmds.stream()
                    .filter(c -> c.getTrigger().equalsIgnoreCase(wantedHelpPage))
                    .findFirst()
                    .map(Command::fullHelpPage)
                    .ifPresent(helpPage -> msg.getChannel().sendMessage(helpPage).submit());
            }
        }
    }

    protected abstract void sendIntroduction(Message msg);
    
    @Override
    public Field getShortDescription() {
        throw new UnsupportedOperationException();
    }
}
