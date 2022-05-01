package de.mspark.jdaw.config;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jooq.lambda.Unchecked;

import de.mspark.jdaw.guilds.GuildConfigService;
import de.mspark.jdaw.guilds.GuildRepository;
import de.mspark.jdaw.help.HelpConfig;
import de.mspark.jdaw.maintainance.BotCheckCommand;
import de.mspark.jdaw.maintainance.PingCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class JdawBuilder {

    private Optional<GuildRepository> repo = Optional.empty();
    private List<JDAConfigurationVisitor> jdaVisitors;
    private JDAWConfig conf;
    private HelpConfig helpConfig;
    private boolean loadDefaultCommands = true;

    public JdawBuilder(JDAWConfig config) {
        super();
        this.conf = config;
    }

    public JdawBuilder enableGuildConfigurations(GuildRepository repo) {
        this.repo = of(repo);
        return this;
    }

    public JdawBuilder enableHelpCommand(HelpConfig config) {
        this.helpConfig = config;
        return this;
    }

    public JdawBuilder disableMaintenanceCommands() {
        this.loadDefaultCommands = false;
        return this;
    }

    public JdawBuilder setJdawConfigurationVisitors(JDAConfigurationVisitor visitor) {
        return setJdawConfigurationVisitors(Arrays.asList(visitor));
    }
    
    public JdawBuilder setJdawConfigurationVisitors(List<JDAConfigurationVisitor> visitors) {
        this.jdaVisitors = visitors;
        return this;
    }

    public JdawInstance buildInstance() {
        if (conf.apiTokens() == null || conf.apiTokens().length == 0) {
            throw new RuntimeException("No Discord API-Tokens found: Bean is present but has no values");
        }
        var jdas = configureDiscord();
        var instance = new JdawInstance(new JDAManager(jdas), new GuildConfigService(conf, repo),
            ofNullable(helpConfig));
        if (loadDefaultCommands) {
            instance.register(new PingCommand(), new BotCheckCommand(jdas));
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
