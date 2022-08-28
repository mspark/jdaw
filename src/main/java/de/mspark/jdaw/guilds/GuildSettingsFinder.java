package de.mspark.jdaw.guilds;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

/**
 * Decorator for {@link GuildRepository} which provides method for finding guild specific settings 
 * based on JDA context. Currently only {@link Message} types are supported.
 * 
 * @author marcel
 */
public class GuildSettingsFinder {
    private final Optional<GuildRepository> repo;
    
    public GuildSettingsFinder(Optional<GuildRepository> repo) {
        this.repo = repo;
    }
    
    public <T extends Message> Optional<String> retrieveGuildPrefix(T msg) {
//        var t = repo.flatMap(r ->retrieveGuildSpecificSetting(msg, r)).orElseThrow();
//        return Optional.of(t.getListenPrefix());
                return repo.flatMap(r -> retrieveGuildSpecificSetting(msg, r))
                .map(SingleGuildSettings::getListenPrefix);
    }

    public <T extends Message> Optional<SingleGuildSettings> retrieveGuildSpecificSetting(T msg) {
        return repo.flatMap(r -> GuildSettingsFinder.retrieveGuildSpecificSetting(msg, r));
    }

    public <T extends Message> List<String> getWhitelistChannel(T event) {
        var settings= repo.flatMap(repo -> retrieveGuildSpecificSetting(event, repo))
            .map(SingleGuildSettings::getChannelWhitelist)
            .orElseGet(Collections::emptyList);
        return settings.stream().map(a -> a.getWhitelistChannelId()).toList();
    }

    private static <T extends Message> Optional<SingleGuildSettings> retrieveGuildSpecificSetting(T msg, GuildRepository repo) {
        return getGuild(msg).flatMap(g -> repo.findById(g.getIdLong()));
    }
    
    private static <T extends Message> Optional<Guild> getGuild(T msg) {
        return Optional.ofNullable(msg.getGuild());
    }
}