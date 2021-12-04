package de.mspark.jdaw.help;

import java.util.List;

import de.mspark.jdaw.Command;
import de.mspark.jdaw.JDAManager;
import de.mspark.jdaw.config.JDAWConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * When implementing this with the {@link EnableHelpCommand} Annotation, a help command for all sub commands will be available .
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
                .filter(cmd -> cmd.helpPage().isPresent())
                .forEach(cmd -> eb.addField(cmd.getTrigger(), cmd.getShortDescription(), false));
            msg.getChannel().sendMessage(eb.build()).submit();
        } else {
            String wantedHelpPage = cmdArguments.get(0);
            allLoadedCmds.stream()
                .filter(c -> c.getTrigger().equalsIgnoreCase(wantedHelpPage))
                .findFirst()
                .filter(cmd -> cmd.userHasEnoughPermission(msg))
                .flatMap(Command::helpPage)
                .ifPresentOrElse(
                        helpPage -> msg.getChannel().sendMessage(helpPage).submit(), 
                        () -> msg.reply("No help page").submit()
                );
        }
    }
    
    @Override
    public MessageEmbed fullHelpPage() {
        throw new UnsupportedOperationException();
    }
    
    public abstract String botDescription();

    @Override
    public String getShortDescription() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public final String getTrigger() {
        return "help";
    }
    
}
