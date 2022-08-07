package de.mspark.jdaw.help;

import java.util.List;

import de.mspark.jdaw.core.TextListenerAction;
import de.mspark.jdaw.core.TextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

/**
 * When implementing this with the {@link EnableHelpCommand} Annotation, a help command for all sub commands will be
 * available.
 *
 * @author marcel
 */
public class GlobalHelpCommand extends TextCommand {
    private final HelpConfig config;
    private List<TextListenerAction> allLoadedCmds;

    public GlobalHelpCommand(List<TextListenerAction> allLoadedCmds, HelpConfig config) {
        this.allLoadedCmds = allLoadedCmds;
        this.config = config;
    }

    @Override
    public void doActionOnTrigger(Message msg, List<String> cmdArguments) {
        if (cmdArguments.isEmpty()) {
            var eb = new EmbedBuilder().setTitle(config.botName()).setDescription(config.botDescription());
            allLoadedCmds.stream()
                .filter(cmd -> cmd.userHasEnoughPermission(msg))
                .filter(cmd -> cmd.helpPageWithAliases(msg).isPresent())
                .forEach(cmd -> eb.addField(cmd.trigger(), cmd.description(), false));
            msg.getChannel().sendMessageEmbeds(eb.build()).submit();
        } else {
            String wantedHelpPage = cmdArguments.get(0);
            allLoadedCmds.stream()
                .filter(c -> c.trigger().equalsIgnoreCase(wantedHelpPage))
                .findFirst()
                .filter(cmd -> cmd.userHasEnoughPermission(msg))
                .flatMap(c -> c.helpPageWithAliases(msg))
                .ifPresentOrElse(
                    helpPage -> msg.getChannel().sendMessageEmbeds(helpPage).submit(),
                    () -> msg.reply("No help page").submit());
        }
    }

    @Override
    public String trigger() {
        return "help";
    }

    @Override
    public String description() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean executableWihtoutArgs() {
        return true;
    }
    
    public void setActions(List<TextListenerAction> actions) {
        this.allLoadedCmds = actions;
    }
}
