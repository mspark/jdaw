package de.mspark.jdaw.help;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.mspark.jdaw.Command;
import de.mspark.jdaw.JDAManager;
import de.mspark.jdaw.guilds.GuildConfigService;

@Configuration
class HelpBeanConfig {

    @Bean
    public GlobalHelpCommand cmd(GuildConfigService gc, JDAManager jdas, List<Command> allLoadedCmds,
        Optional<HelpConfig> config) {
        return config.map(c -> new GlobalHelpCommand(gc, jdas, allLoadedCmds, c)).orElse(null);
    }

}