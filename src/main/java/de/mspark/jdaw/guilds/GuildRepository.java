package de.mspark.jdaw.guilds;

import java.util.Optional;

import de.mspark.jdaw.guilds.model.CustomGuildConf;

public interface GuildRepository {
    
    CustomGuildConf save(CustomGuildConf g);
    
    void delete(CustomGuildConf entity);
    
    boolean exists(Long gid);
    
    Optional<CustomGuildConf> findOne(Long gid);

}
