package de.mspark.jdaw.cmdapi;

import java.util.List;

import de.mspark.jdaw.guilds.GuildConfigService;
import de.mspark.jdaw.startup.JDAManager;

public record JdawState (
        List<TextListenerAction> registeredActions, 
        GuildConfigService guildConfig, 
        JDAManager jdaManager) {
}