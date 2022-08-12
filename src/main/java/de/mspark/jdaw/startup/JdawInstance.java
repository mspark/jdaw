package de.mspark.jdaw.startup;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import de.mspark.jdaw.cmdapi.JdawEventListener;
import de.mspark.jdaw.cmdapi.JdawState;
import de.mspark.jdaw.cmdapi.TextCommand;
import de.mspark.jdaw.cmdapi.TextListenerAction;
import de.mspark.jdaw.guilds.GuildConfigService;

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

    private final List<TextListenerAction> registeredActions = new LinkedList<>();
    private Collection<JdawEventListener> actionListeners = new HashSet<>();

    JdawInstance(JDAManager jdas, GuildConfigService guildConfig) {
        this.jdas = jdas;
        this.guildConfig = guildConfig;
    }
    
    /**
     * Immediately register a command on the discord bot. 
     * 
     * @param cmd
     */
    public void register(TextCommand... cmds) {
        Stream.of(cmds).forEach(cmd -> {
            var action = new TextListenerAction(guildConfig, cmd);
            action.startListenOnDiscordEvents(jdas);
            var state = new JdawState(Collections.unmodifiableList(registeredActions), guildConfig, jdas);
            actionListeners.forEach(listener -> listener.onNewRegistration(state, action));
            registeredActions.add(action);
        });
    }

    /**
     * Returns the registered text listener.
     * 
     * @return
     */
    public List<TextListenerAction> getRegisterdActions() {
        var list = new LinkedList<>(registeredActions);
        return list;
    }
    
    public void addJdawEventListener(JdawEventListener... listener) {
        actionListeners.addAll(Arrays.asList(listener));
    }

}