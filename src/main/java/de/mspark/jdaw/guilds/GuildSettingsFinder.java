package de.mspark.jdaw.guilds;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.dv8tion.jda.api.entities.Message;

/**
 * Decorator for {@link GuildRepository} which provides method for finding guild specific settings
 * 
 * @author marcel
 */
public class GuildSettingsFinder {
    private final Optional<GuildRepository> repoOptional;
    
    public GuildSettingsFinder(Optional<GuildRepository> repo) {
        this.repoOptional = repo;
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
    
    public <T extends Message> List<String> getWhitelistChannel(long gid) {
        var settings= repoOptional.flatMap(repo -> repo.findById(gid))
                .map(SingleGuildSettings::getChannelWhitelist)
                .orElseGet(Collections::emptyList);
            return settings.stream().map(a -> a.getWhitelistChannelId()).toList();
    }

}
