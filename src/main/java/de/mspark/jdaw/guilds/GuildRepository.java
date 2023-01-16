package de.mspark.jdaw.guilds;

import java.util.Optional;

public interface GuildRepository {
    
    SingleGuildSettings save(SingleGuildSettings g);
    
    void delete(SingleGuildSettings entity);
    
    boolean existsById(Long gid);
    
    Optional<SingleGuildSettings> findById(Long gid);

    /**
     * Factory method for a new guild setting. This will be invoked each time a setting on a guild is changed 
     * but no configuration was found in the database. 
     * 
     * @param gid
     * @param prefix
     * @return The created settings object which was saved into persistent storage.
     */
    @Deprecated(since = "6.2")
    default SingleGuildSettings createAndSaveNew(Long gid, String prefix) {
        var setting = new SingleGuildSettings(gid, prefix);
        this.save(setting);
        return setting;
    }

}
