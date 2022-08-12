package de.mspark.jdaw.startup;

import static java.util.Optional.of;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jooq.lambda.Unchecked;

import de.mspark.jdaw.cmdapi.TextCommand;
import de.mspark.jdaw.guilds.GuildConfigService;
import de.mspark.jdaw.guilds.GuildRepository;
import de.mspark.jdaw.help.GlobalHelpCommand;
import de.mspark.jdaw.help.HelpConfig;
import de.mspark.jdaw.maintainance.BotCheckCommand;
import de.mspark.jdaw.maintainance.ListCommand;
import de.mspark.jdaw.maintainance.PingCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

/**
 * Builder for a {@link JdawInstance}. It configures the instance with all possible
 * 
 * Example Usage:
 * 
 * <pre>
 * <code>
 * new JdawInstanceBuilder(config).enableHelpCommand(config)
 *     .enableGuildConfigurations(repo)
 *     .addForRegister(command)
 *     .buildJdawInstance();
 * </code>
 * </pre>
 *
 * @author marcel
 */
public class JdawInstanceBuilder {

    private final JDAWConfig conf;

    private boolean loadDefaultCommands = true;
    private Optional<HelpConfig> helpConfig = Optional.empty();
    private Optional<GuildRepository> repo = Optional.empty();
    private Collection<JDAConfigModifier> configModifiers = new LinkedList<>();
    private Collection<TextCommand> cmds = new ArrayList<>();

    public JdawInstanceBuilder(JDAWConfig config) {
        this.conf = config;
    }

    public JdawInstanceBuilder enableGuildConfigurations(GuildRepository repo) {
        this.repo = of(repo);
        return this;
    }

    public JdawInstanceBuilder enableHelpCommand(HelpConfig config) {
        this.helpConfig = Optional.of(config);
        return this;
    }

    public JdawInstanceBuilder disableMaintenanceCommands() {
        this.loadDefaultCommands = false;
        return this;
    }

    public JdawInstanceBuilder addJdaModifier(JDAConfigModifier... visitor) {
        this.configModifiers.addAll(List.of(visitor));
        return this;
    }

    /**
     * Add a text command event listener to the JdawInstance. The register action takes place during build, see
     * {@link #buildJdawInstance()}. Each command is also added as event listener.
     * 
     * @param cmd The command to add to the bot
     * @return builder
     */
    public JdawInstanceBuilder addCommand(TextCommand... cmd) {
        this.cmds.addAll(List.of(cmd));
        return this;
    }

    /**
     * Starts JDAW with all configured options. The discord bot is logged in and all {@link TextCommand} listen on their
     * trigger.
     * 
     * @return configured JDAW Instance, can be used for further command registrations after the build
     */
    public JdawInstance buildJdawInstance() {
        if (conf.apiTokens() == null || conf.apiTokens().length == 0) {
            throw new IllegalStateException("No Discord API-Tokens found: Bean is present but has no values");
        }
        var jdas = configureDiscord();
        var instance = new JdawInstance(new JDAManager(jdas), new GuildConfigService(conf, repo));
        if (loadDefaultCommands) {
            configureDefaultCommands(instance);
        }
        var cmdArray = cmds.toArray(TextCommand[]::new);
        instance.addJdawEventListener(cmdArray);
        instance.register(cmds.toArray(cmdArray));
        return instance;
    }

    private JDA[] configureDiscord() {
        List<JDABuilder> jdaBuilderList = Stream.of(conf.apiTokens()).map(JDABuilder::createDefault).toList();
        jdaBuilderList.forEach(a -> configModifiers.forEach(j -> j.modify(a)));
        var jdas = jdaBuilderList.stream().map(Unchecked.function(JDABuilder::build)).toArray(JDA[]::new);
        return jdas;
    }
    
    private void configureDefaultCommands(JdawInstance instance) {
        configureHelpCommand(instance);
        configureListCommand(instance);
        cmds.add(new PingCommand());
        cmds.add(new BotCheckCommand());
    }

    private void configureListCommand(JdawInstance instance) {
        var listCmd = new ListCommand();
        instance.addJdawEventListener(listCmd);
        cmds.add(listCmd);
    }

    private void configureHelpCommand(JdawInstance instance) {
        helpConfig.ifPresent(config -> {
            var helpCommand = new GlobalHelpCommand(config);
            instance.addJdawEventListener(helpCommand);
            cmds.add(helpCommand);
        });
    }

}
