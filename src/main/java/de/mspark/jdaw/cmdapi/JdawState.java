package de.mspark.jdaw.cmdapi;

import java.util.List;

import de.mspark.jdaw.guilds.GuildSettingsFinder;
import de.mspark.jdaw.startup.JDAManager;
import de.mspark.jdaw.startup.JdawConfig;

public record JdawState (
        List<TextListenerAction> registeredActions, 
        GuildSettingsFinder guildConfig,
        JDAManager jdaManager,
        JdawConfig globalConfig) {
    
    /**
     * Returns the used configuartion to start the bot. The API Tokens are masked for security reasons.
     * @return
     */
    public JdawConfig globalConfig() {
        return new JdawConfig() {
            
            @Override
            public String defaultPrefix() {
                return globalConfig.defaultPrefix();
            }
            
            @Override
            public String[] apiTokens() {
                throw new UnsupportedOperationException("API Tokens are masked in JDAW states.");
            }
            
            @Override
            public String[] botAdmins() {
                return globalConfig.botAdmins();
            }
        };
    }
}