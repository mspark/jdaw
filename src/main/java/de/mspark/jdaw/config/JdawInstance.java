package de.mspark.jdaw.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.mspark.jdaw.core.DiscordAction;
import de.mspark.jdaw.core.TextCommand;
import de.mspark.jdaw.guilds.GuildConfigService;
import de.mspark.jdaw.help.GlobalHelpCommand;
import de.mspark.jdaw.help.HelpConfig;

/**
 * Configured JDAW instance which holds all necessary configuration to start the a discord connection with configured 
 * features. For feature configuration use the {@link JdawBuilder}. 
 * 
 * The instance must be started after creation! Use {@link #start()} for this.
 * 
 * Example usage:
 * <pre><code>
 * var instance = new JdawInstance(config).buildInstance();
 * instance.register(new Command());
 * instance.start();
 * </code></pre>
 * 
 * @author marcel
 */
public class JdawInstance {

    private final JDAManager jdas;
    private final GuildConfigService guildConfig;
    private final List<TextCommand> commandActions = new ArrayList<>();
    private final Optional<HelpConfig> helpConfig;
    private boolean started;

    JdawInstance(JDAManager jdas, GuildConfigService guildConfig, Optional<HelpConfig> helpConfig) {
        super();
        this.jdas = jdas;
        this.guildConfig = guildConfig;
        this.helpConfig = helpConfig;
    }

    public JDAManager jdaManager() {
        return jdas;
    }

    public GuildConfigService guildConfig() {
        return guildConfig;
    }

    /**
     * Register text command actions for this JDAW instance. 
     * 
     * @param action
     * @throws IllegalStateException When method is called after instance was started
     */
    public void register(TextCommand... action) {
        if (started) {
            throw new IllegalStateException("Instance was already started");
        }
        commandActions.addAll(Arrays.asList(action));
    }

    /**
     * Creates a running discord actions for all the registed {@link TextCommand}. This can only be done once per
     * instance.
     * 
     * @return All started actions
     */
    public List<DiscordAction> start() {
        List<DiscordAction> cmds = new ArrayList<>();
        for (var action : commandActions) {
            cmds.add(new DiscordAction(guildConfig, jdas, action));
        }
        helpConfig.map(hc -> new GlobalHelpCommand(new ArrayList<>(cmds), hc))
            .map(gc -> new DiscordAction(guildConfig, jdas, gc))
            .ifPresent(cmds::add);
        return cmds;
    }

}