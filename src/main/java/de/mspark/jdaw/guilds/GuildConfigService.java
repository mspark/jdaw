package de.mspark.jdaw.guilds;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import de.mspark.jdaw.config.JDAWConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

@Service
public class GuildConfigService {
    private JDAWConfig config;
    private final GuildRepository repo;
    
    public GuildConfigService(JDAWConfig config, GuildRepository repo) {
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
        return getGuild(msg)
                .flatMap(g -> repo.findOne(g.getIdLong()))
                .map(cg -> cg.prefix())
                .orElse(this.config.prefix());
    }
    
    public <T extends GenericMessageEvent> List<String> getWhitelistChannel(T event) {
        return getGuild(event)
                .flatMap(g -> repo.findOne(g.getIdLong()))
                .map(cg -> cg.whitelist())
                .orElseGet(Collections::emptyList);
    }
}
