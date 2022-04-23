package de.mspark.jdaw.guilds;

import java.util.Optional;

public interface GuildRepository {
    
    CustomGuildConf save(CustomGuildConf g);
    
    void delete(CustomGuildConf entity);
    
    boolean existsById(Long gid);
    
    Optional<CustomGuildConf> findById(Long gid);

}
