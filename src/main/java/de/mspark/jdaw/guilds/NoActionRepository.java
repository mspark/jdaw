package de.mspark.jdaw.guilds;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import de.mspark.jdaw.guilds.model.CustomGuildConf;

@Configuration
class NoActionRepository {

    @Bean
    @Lazy
    public GuildRepository guildRepository() {
        return new GuildRepository() {
            
            @Override
            public Optional<CustomGuildConf> findById(Long gid) {
                return Optional.empty();
            }
            
            @Override
            public boolean existsById(Long gid) {
                return false;
            }
            
            @Override
            public void delete(CustomGuildConf entity) {
                // nothing
            }

            @Override
            public CustomGuildConf save(CustomGuildConf g) {
                return null;
            }
        };
    }
}
