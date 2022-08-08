package de.mspark.jdaw.guilds;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.mspark.jdaw.startup.JDAWConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

public class GuildConfigService {
    private JDAWConfig config;
    private final Optional<GuildRepository> repo;
    
    public GuildConfigService(JDAWConfig config, Optional<GuildRepository> repo) {
        this.repo = repo;
        this.config = config;
    }
    
    public <T extends Message> Optional<Guild> getGuild(T msg) {
        return Optional.ofNullable(msg.getGuild());
    }
    
    public <T extends GenericMessageEvent> Optional<Guild> getGuild(T event) {
        return Optional.ofNullable(event.getGuild());
    }

    public <T extends Message> String getPrefix(Message msg) {
        return repo.flatMap(r ->
            getGuild(msg)
                .flatMap(g -> r.findById(g.getIdLong()))
                .map(cg -> cg.prefix()))
            .orElse(this.config.defaultPrefix());
    }
    
    public <T extends GenericMessageEvent> List<String> getWhitelistChannel(T event) {
        return repo.flatMap(r -> 
                getGuild(event)
                .flatMap(g -> r.findById(g.getIdLong()))
                .map(cg -> cg.whitelist()))
            .orElseGet(Collections::emptyList);
    }
    
    public JDAWConfig globalConfig() {
        return config;
    }
}
