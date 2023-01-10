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
    private final Optional<GuildRepository> repoOptional;
    
    public GuildSettingsFinder(Optional<GuildRepository> repo) {
        this.repoOptional = repo;
    }
    
    @Deprecated
    public <T extends Message> Optional<String> retrieveGuildPrefix(T msg) {
//        var t = repo.flatMap(r ->retrieveGuildSpecificSetting(msg, r)).orElseThrow();
//        return Optional.of(t.getListenPrefix());
                return repoOptional.flatMap(r -> retrieveGuildSpecificSetting(msg, r))
                .map(SingleGuildSettings::getListenPrefix);
    }
    
    /**
     * Gives the prefix for a specific guild identified by the guild id (gid). If no prefix was stores in beforehand,
     * the optional is empty. Same behaviour when no {@link GuildRepository} is configured. 
     * 
     * @param gid
     * @return
     */
    public Optional<String> getGuildSpecificPrefix(long gid) {
        return repoOptional.flatMap(repo -> repo.findById(gid).map(SingleGuildSettings::getListenPrefix));
    }
    
    @Deprecated
    public <T extends Message> Optional<SingleGuildSettings> retrieveGuildSpecificSetting(T msg) {
        return repoOptional.flatMap(r -> GuildSettingsFinder.retrieveGuildSpecificSetting(msg, r));
    }

    public <T extends Message> List<String> getWhitelistChannel(long gid) {
        var settings= repoOptional.flatMap(repo -> repo.findById(gid))
                .map(SingleGuildSettings::getChannelWhitelist)
                .orElseGet(Collections::emptyList);
            return settings.stream().map(a -> a.getWhitelistChannelId()).toList();
    }

    @Deprecated
    public <T extends Message> List<String> getWhitelistChannel(T event) {
        if (event.getGuild() == null) {
            throw new IllegalArgumentException("Event was not triggered on guild");
        }
        return getWhitelistChannel(event.getGuild().getIdLong());
    }

    private static <T extends Message> Optional<SingleGuildSettings> retrieveGuildSpecificSetting(T msg, GuildRepository repo) {
        return getGuild(msg).flatMap(g -> repo.findById(g.getIdLong()));
    }
    
    private static <T extends Message> Optional<Guild> getGuild(T msg) {
        return Optional.ofNullable(msg.getGuild());
    }
}
