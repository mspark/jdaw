package de.mspark.jdaw.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import de.mspark.jdaw.core.TextListenerAction;
import de.mspark.jdaw.core.TextCommand;
import de.mspark.jdaw.guilds.GuildConfigService;
import de.mspark.jdaw.help.GlobalHelpCommand;
import de.mspark.jdaw.help.HelpConfig;

/**
 * Configured JDAW instance which holds all necessary configuration to start the a discord connection with configured 
 * features. For feature configuration use the {@link JdawInstanceBuilder}. 
 *
 * Example usage:
 * <pre><code>
 * var instance = new JdawInstanceBuilder(config).buildJdawInstance();
 * instance.register(command);
 * </code></pre>
 * 
 * It is recommended to use the builder for adding commands to the bot when possible.
 * 
 * @author marcel
 */
public class JdawInstance {

    private final JDAManager jdas;
    private final GuildConfigService guildConfig;

    private Optional<GlobalHelpCommand> helpCommand = Optional.empty();
    private final List<TextListenerAction> registeredActions = new LinkedList<>();

    JdawInstance(JDAManager jdas, GuildConfigService guildConfig) {
        this(jdas, guildConfig, null);
    }

    JdawInstance(JDAManager jdas, GuildConfigService guildConfig, HelpConfig config) {
        this.jdas = jdas;
        this.guildConfig = guildConfig;
        if (config != null) {
            var helpCommand = new GlobalHelpCommand(registeredActions, config);
            this.helpCommand = Optional.of(helpCommand);
            var helpAction = new TextListenerAction(guildConfig, helpCommand);
            helpAction.registerOn(jdas);
        }
    }
    
    /**
     * Immediately register a command on the discord bot. 
     * 
     * @param cmd
     */
    public void register(TextCommand cmd) {
        var action = new TextListenerAction(guildConfig, cmd);
        action.registerOn(jdas);
        registeredActions.add(action);
        refreshGlobalHelpCmd();
    }

    /**
     * Register multiple commands simultaneously.
     * 
     * @param textCmds
     */
    public void registerAll(TextCommand... textCmds) {
        List<TextListenerAction> actions = new ArrayList<>();
        for (var action : textCmds) {
            actions.add(new TextListenerAction(guildConfig, action));
        }
        registeredActions.addAll(actions);
        refreshGlobalHelpCmd();
    }

    private void refreshGlobalHelpCmd() {
        helpCommand.ifPresent(help -> help.setActions(Collections.unmodifiableList(registeredActions)));
    }
    
    /**
     * Returns the registered text listener. The global help command is always missing (event if its configured). 
     * 
     * @return
     */
    public List<TextListenerAction> getRegisterdActions() {
        var list = new LinkedList<>(registeredActions);
        return list;
    }

}