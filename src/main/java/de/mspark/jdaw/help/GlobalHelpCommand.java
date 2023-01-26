package de.mspark.jdaw.help;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import de.mspark.jdaw.cmdapi.JdawState;
import de.mspark.jdaw.cmdapi.TextCommand;
import de.mspark.jdaw.cmdapi.TextListenerAction;
import de.mspark.jdaw.startup.JdawInstanceBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

/**
 * Command which generates a help page for all commands (in case they provide a configured help page).
 * 
 * @see {@link JdawInstanceBuilder#enableHelpCommand(HelpConfig)}
 * @see TextCommand#commandHelpPage()
 * @author marcel
 */
public class GlobalHelpCommand extends TextCommand  {
    private final HelpConfig config;
    private Collection<TextListenerAction> allLoadedCmds = new HashSet<>();

    public GlobalHelpCommand(HelpConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Null help config is not allowed");
        }
        this.config = config;
    }

    @Override
    public void onTrigger(Message msg, List<String> cmdArguments) {
        if (cmdArguments.isEmpty()) {
            var eb = new EmbedBuilder().setTitle(config.botName()).setDescription(config.botDescription());
            allLoadedCmds.stream()
                .filter(cmd -> cmd.userHasEnoughPermission(msg.getMember()))
                .filter(cmd -> cmd.helpPageWithAliases(msg).isPresent())
                .forEach(cmd -> eb.addField(cmd.getCommandSpecification().trigger(), cmd.getCommandSpecification().description(), false));
            HelpConfig.addFooter(eb, config);
            msg.getChannel().sendMessageEmbeds(eb.build()).submit();
        } else {
            String wantedHelpPage = cmdArguments.get(0);
            allLoadedCmds.stream()
                .filter(c -> c.getCommandSpecification().trigger().equalsIgnoreCase(wantedHelpPage))
                .findFirst()
                .filter(cmd -> cmd.userHasEnoughPermission(msg.getMember()))
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
    
    @Override
    public void onNewRegistration(JdawState stateOnRegistrationAttempt, TextListenerAction newRegisteredAction) {
        addAction(newRegisteredAction);
    }
    
    private void addAction(TextListenerAction action) {
        if (!action.getCommandSpecification().trigger().equals(trigger())) {
            this.allLoadedCmds.add(action);
        }
    }
}
