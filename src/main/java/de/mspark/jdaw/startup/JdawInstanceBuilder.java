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
import de.mspark.jdaw.guilds.GuildRepository;
import de.mspark.jdaw.guilds.GuildSettingsFinder;
import de.mspark.jdaw.guilds.PrefixSetCommand;
import de.mspark.jdaw.help.GlobalHelpCommand;
import de.mspark.jdaw.help.HelpConfig;
import de.mspark.jdaw.maintainance.BotCheckCommand;
import de.mspark.jdaw.maintainance.ListCommand;
import de.mspark.jdaw.maintainance.PingCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

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

    private final JdawConfig conf;

    private boolean loadDefaultCommands = true;
    private Optional<HelpConfig> helpConfig = Optional.empty();
    private Optional<GuildRepository> repo = Optional.empty();
    private Collection<JDAConfigModifier> configModifiers = new LinkedList<>();
    private Collection<TextCommand> cmds = new ArrayList<>();

    public JdawInstanceBuilder(JdawConfig config) {
        this.conf = config;
    }

    /**
     * Enable guild 
     * @param repo
     * @return
     */
    public JdawInstanceBuilder enableGuildSpecificSettings(GuildRepository repo) {
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
        var jdaManager = configureDiscord();
        var guildConfig = new GuildSettingsFinder(repo);
        var instance = new JdawInstance(jdaManager, guildConfig, conf);
        if (loadDefaultCommands) {
            configureDefaultCommands(instance);
        }
        var cmdArray = cmds.toArray(TextCommand[]::new);
        instance.addJdawEventListener(cmdArray);
        instance.register(cmds.toArray(cmdArray));
        return instance;
    }

    private JDAManager configureDiscord() {
        var priviledgedIntents = List.of(
                GatewayIntent.GUILD_MESSAGES, 
                GatewayIntent.MESSAGE_CONTENT, 
                GatewayIntent.GUILD_MEMBERS);
        List<JDABuilder> jdaBuilderList = Stream.of(conf.apiTokens())
                .map(token -> JDABuilder.createDefault(token, priviledgedIntents))
                .toList();
        jdaBuilderList.forEach(a -> configModifiers.forEach(j -> j.modify(a)));
        var jdas = jdaBuilderList.stream().map(Unchecked.function(JDABuilder::build)).toArray(JDA[]::new);
        return new JDAManager(jdas);
    }
    
    private void configureDefaultCommands(JdawInstance instance) {
        configureHelpCommand(instance);
        configureListCommand(instance);
        configurePrefixCommand(instance);
        cmds.add(new PingCommand());
        cmds.add(new BotCheckCommand());
    }

    private void configurePrefixCommand(JdawInstance instance) {
        repo.ifPresent(r -> {
            var prefixCmd = new PrefixSetCommand(r, a -> Optional.of(a) /* TODO */);
            cmds.add(prefixCmd);
        });
    }

    private void configureListCommand(JdawInstance instance) {
        var listCmd = new ListCommand();
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
