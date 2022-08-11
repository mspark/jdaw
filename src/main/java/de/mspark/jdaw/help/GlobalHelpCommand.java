package de.mspark.jdaw.help;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import de.mspark.jdaw.cmdapi.JdawEventListener;
import de.mspark.jdaw.cmdapi.JdawState;
import de.mspark.jdaw.cmdapi.TextCommand;
import de.mspark.jdaw.cmdapi.TextListenerAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

/**
 * When implementing this with the {@link EnableHelpCommand} Annotation, a help command for all sub commands will be
 * available.
 *
 * @author marcel
 */
public class GlobalHelpCommand extends TextCommand implements JdawEventListener {
    private final HelpConfig config;
    private Collection<TextListenerAction> allLoadedCmds = new HashSet<>();

    public GlobalHelpCommand(HelpConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Null help config is not allowed");
        }
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
    
    @Override
    public void onNewRegistration(JdawState stateOnRegistrationAttempt, TextListenerAction newRegisteredAction) {
        if (this.allLoadedCmds.isEmpty()) {
            stateOnRegistrationAttempt.registeredActions().stream().forEach(this::addAction);
        } else {
            this.addAction(newRegisteredAction);
        }

    }
    
    private void addAction(TextListenerAction action) {
        if (!action.trigger().equals(trigger())) {
            this.allLoadedCmds.add(action);
        }
    }
}
