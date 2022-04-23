package de.mspark.jdaw.guilds;

import java.util.Optional;

@FunctionalInterface
public interface GuildPrefixFilter {

    public Optional<String> filter(String requestedNewPrefix);

}
