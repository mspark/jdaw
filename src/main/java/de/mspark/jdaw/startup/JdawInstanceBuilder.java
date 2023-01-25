package de.mspark.jdaw.startup;

import static java.util.Optional.of;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mspark.jdaw.cmdapi.TextCommand;
import de.mspark.jdaw.guilds.GuildRepository;
import de.mspark.jdaw.guilds.GuildSettingsFinder;
import de.mspark.jdaw.guilds.PrefixSetCommand;
import de.mspark.jdaw.help.GlobalHelpCommand;
import de.mspark.jdaw.help.HelpConfig;
import de.mspark.jdaw.maintainance.BotCheckCommand;
import de.mspark.jdaw.maintainance.Changelog;
import de.mspark.jdaw.maintainance.ChangelogCmd;
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
    private Optional<String> changelogPath = Optional.empty();

    public JdawInstanceBuilder(JdawConfig config) {
        if (config == null ) {
            throw new IllegalArgumentException("Illegal config. Null");
        }
        if (config.defaultPrefix() == null) {
            throw new IllegalArgumentException("Illegal config. You provided a JdawConfig but without a defaultPrefix");
        }
        if (config.apiTokens() == null || config.apiTokens().length == 0) {
            throw new IllegalArgumentException("Illegal config. You provided a JdawConfig but without a discord API token");
        }
        
        this.conf = config;
    }

    // TODO
    public JdawInstanceBuilder enableGuildSpecificSettings(GuildRepository repo) {
        this.repo = of(repo);
        return this;
    }

    // TODO
    public JdawInstanceBuilder enableHelpCommand(HelpConfig config) {
        this.helpConfig = Optional.of(config);
        return this;
    }

    /**
     * This disables default commands like "ping", "mlist", "botcheck". 
     * 
     * @return
     */
    public JdawInstanceBuilder disableMaintenanceCommands() {
        this.loadDefaultCommands = false;
        return this;
    }

    // TODO
    public JdawInstanceBuilder addJdaModifier(JDAConfigModifier... visitor) {
        this.configModifiers.addAll(List.of(visitor));
        return this;
    }

    /**
     * Add a text command event listener to the JdawInstance. The register action takes place during build, see
     * {@link #buildJdawInstance()}. Each command is also added as event listener during {@link #buildJdawInstance()}.
     * 
     * @param cmd The command to add to the bot
     * @return builder
     * @see JdawInstance#register(TextCommand...)
     */
    public JdawInstanceBuilder addCommand(TextCommand... cmd) {
        this.cmds.addAll(List.of(cmd));
        return this;
    }
    
    /**
     * This enables the !sendchangelog command. For this you have to provide a path to a changelog file accessable 
     * by the jar. It must have the following layout:
     * <code><pre>
     * Version 1.0.0
     * - change 1
     * - change 2
     * ####
     * Version 0.0.1
     * - change 1
     * </code></pre>
     * When enabling the changelog command, you must also provide a {@link HelpConfig}. See wit
     * @param changelog Path to a file which must be accessible or the JDAW build will fail
     * @return
     */
    public JdawInstanceBuilder withChangelog(String changelog) {
        this.changelogPath = Optional.of(changelog);
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
        // those intents are mandatory for this bot (due to the text based nature)
        var priviledgedDefaultIntents = List.of(
                GatewayIntent.GUILD_MESSAGES, // to receive messages
                GatewayIntent.MESSAGE_CONTENT, // to receive message contents of guild channels
                GatewayIntent.GUILD_MEMBERS); // Dont know? maybe permissions?
        List<JDABuilder> jdaBuilderList = Stream.of(conf.apiTokens())
                .map(token -> JDABuilder.createDefault(token))
                .toList();
        jdaBuilderList.forEach(jdaBuilder -> {
            jdaBuilder.enableIntents(priviledgedDefaultIntents);
            configModifiers.forEach(j -> j.modify(jdaBuilder));            
        });
        Logger log = LoggerFactory.getLogger(JdawInstanceBuilder.class);
        var jdas = jdaBuilderList.stream()
                .map(Unchecked.function(JDABuilder::build))
                .peek(jda -> log.info("Connected as " + jda.getSelfUser().getAsTag()))
                .toArray(JDA[]::new);
        return new JDAManager(jdas);
    }
    
    private void configureDefaultCommands(JdawInstance instance) {
        configureHelpCommand(instance);
        configureListCommand();
        configurePrefixCommand();
        configureChangelogCommand();
        cmds.add(new PingCommand());
        cmds.add(new BotCheckCommand());
    }

    private void configureChangelogCommand() {
        changelogPath.ifPresent(path -> {
            var help = helpConfig.orElseThrow(() -> new IllegalArgumentException("You must provide a HelpConfig in order to activate the changelog cmd."));
            try {
                var changelog = new Changelog(new File(path));
                var changeCmd = new ChangelogCmd(help, changelog);
                cmds.add(changeCmd);
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        });
    }

    private void configurePrefixCommand() {
        repo.ifPresent(r -> {
            var prefixCmd = new PrefixSetCommand(r);
            cmds.add(prefixCmd);
        });
    }

    private void configureListCommand() {
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
