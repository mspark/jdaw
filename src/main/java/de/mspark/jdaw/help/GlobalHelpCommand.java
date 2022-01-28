package de.mspark.jdaw.help;

import java.util.List;

import de.mspark.jdaw.Command;
import de.mspark.jdaw.JDAManager;
import de.mspark.jdaw.config.JDAWConfig;
import de.mspark.jdaw.guilds.GuildConfigService;
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
    
    public GlobalHelpCommand(JDAWConfig conf, GuildConfigService gc, JDAManager jdas, List<Command> allLoadedCmds) {
        super(conf, gc, jdas, false);
        this.allLoadedCmds = allLoadedCmds;
    }

    @Override
    public void doActionOnCmd(Message msg, List<String> cmdArguments) {
        if (cmdArguments.isEmpty()) {
            var eb = new EmbedBuilder().setTitle(botName()).setDescription(botDescription());
            allLoadedCmds.stream()
                .filter(cmd -> cmd.userHasEnoughPermission(msg))
                .filter(cmd -> cmd.helpPage(msg).isPresent())
                .forEach(cmd -> eb.addField(cmd.getTrigger(), cmd.getShortDescription(), false));
            msg.getChannel().sendMessageEmbeds(eb.build()).submit();
        } else {
            String wantedHelpPage = cmdArguments.get(0);
            allLoadedCmds.stream()
                .filter(c -> c.getTrigger().equalsIgnoreCase(wantedHelpPage))
                .findFirst()
                .filter(cmd -> cmd.userHasEnoughPermission(msg))
                .flatMap(c -> c.helpPage(msg))
                .ifPresentOrElse(
                        helpPage -> msg.getChannel().sendMessageEmbeds(helpPage).submit(), 
                        () -> msg.reply("No help page").submit()
                );
        }
    }
    
    @Override
    public MessageEmbed fullHelpPage() {
        throw new UnsupportedOperationException();
    }
    
    public abstract String botDescription();
    
    public abstract String botName();

    @Override
    public String getShortDescription() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public final String getTrigger() {
        return "help";
    }
    
}
