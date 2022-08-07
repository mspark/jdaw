package de.mspark.jdaw.config;

import static java.util.Optional.of;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jooq.lambda.Unchecked;

import de.mspark.jdaw.core.TextCommand;
import de.mspark.jdaw.guilds.GuildConfigService;
import de.mspark.jdaw.guilds.GuildRepository;
import de.mspark.jdaw.help.HelpConfig;
import de.mspark.jdaw.maintainance.BotCheckCommand;
import de.mspark.jdaw.maintainance.PingCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

/**
 * Builder for a {@link JdawInstance}. It configures the instance with all possible 
 * 
 * Example Usage:
 * <pre><code>
 * new JdawInstanceBuilder(config).enableHelpCommand(config)
 *     .enableGuildConfigurations(repo)
 *     .addForRegister(command)
 *     .buildJdawInstance();
 * </code></pre>
 *
 * @author marcel
 */
public class JdawInstanceBuilder {

    private Optional<GuildRepository> repo = Optional.empty();
    private List<JDAConfigurationVisitor> jdaVisitors;
    private List<TextCommand> cmds = new ArrayList<>();
    private JDAWConfig conf;
    private HelpConfig helpConfig;
    private boolean loadDefaultCommands = true;

    public JdawInstanceBuilder(JDAWConfig config) {
        this.conf = config;
    }

    public JdawInstanceBuilder enableGuildConfigurations(GuildRepository repo) {
        this.repo = of(repo);
        return this;
    }

    public JdawInstanceBuilder enableHelpCommand(HelpConfig config) {
        this.helpConfig = config;
        return this;
    }

    public JdawInstanceBuilder disableMaintenanceCommands() {
        this.loadDefaultCommands = false;
        return this;
    }

    public JdawInstanceBuilder setJdawConfigurationVisitors(JDAConfigurationVisitor visitor) {
        return setJdawConfigurationVisitors(Arrays.asList(visitor));
    }
    
    public JdawInstanceBuilder setJdawConfigurationVisitors(List<JDAConfigurationVisitor> visitors) {
        this.jdaVisitors = visitors;
        return this;
    }
    
    /**
     * Add a text command event listener to the JdawInstance. The register action takes place during build, 
     * see {@link #buildJdawInstance()}. 
     * 
     * @param cmd The command to add to the bot
     * @return builder
     */
    public JdawInstanceBuilder addForRegister(TextCommand cmd) {
        this.cmds.add(cmd);
        return this;
    }

    /**
     * Starts  JDAW with all configured options. The discord bot is logged in and all {@link TextCommand} listen on 
     * their trigger. 
     * 
     * @return configured JDAW Instance, can be used for further command registrations after the build
     */
    public JdawInstance buildJdawInstance() {
        if (conf.apiTokens() == null || conf.apiTokens().length == 0) {
            throw new IllegalStateException("No Discord API-Tokens found: Bean is present but has no values");
        }
        var jdas = configureDiscord();
        var instance = new JdawInstance(new JDAManager(jdas), new GuildConfigService(conf, repo), helpConfig);
        if (loadDefaultCommands) {
            cmds.add(new PingCommand());
            cmds.add(new BotCheckCommand());
            instance.register(cmds.toArray(TextCommand[]::new));
        }
        return instance;
    }

    private JDA[] configureDiscord() {
        List<JDABuilder> jdaBuilderList = Stream.of(conf.apiTokens()).map(JDABuilder::createDefault).toList();
        jdaBuilderList.forEach(a -> jdaVisitors.forEach(j -> j.visit(a)));
        var jdas = jdaBuilderList.stream().map(Unchecked.function(JDABuilder::build)).toArray(JDA[]::new);
        return jdas;
    }
}
