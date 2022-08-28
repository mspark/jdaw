package de.mspark.jdaw.guilds;

@FunctionalInterface
public interface GuildPrefixFilter {

    public boolean isAllowed(String requestedNewPrefix);

}
