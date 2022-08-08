package de.mspark.jdaw.startup;

import de.mspark.jdaw.cmdapi.TextCommand;

/** 
 * Provides basic information in order to run JDAW. Must be implemented and then scanned by spring. For example
 * 
 * <code>
    \@Bean
    public JDAWConfig jdawConfig() {
        return new JDAWConfig() {
        // .......
        };
    }
 * </code>
 * 
 * 
 * @author marcel
 *
 */
public interface JDAWConfig {
    
    /**
     * Global prefix for every command trigger. Text in messages have to start with this string in order to trigger the command action (except for custom TextListener).
     * 
     * @return Custom prefix
     */
    String defaultPrefix();
    
    /**
     * Discord API tokens. The first one is always the main token which is used for communicating by default.
     * @return
     */
    String[] apiTokens();
    
    /**
     * Specifies users which are bot admins. This can be used for commands which aren't specific to guilds like "restart"
     * or "reload config". See {@link TextCommand#botAdminOnly()} setting for enabling the botAdmin check inside a
     * command. 
     * @return List of User IDs
     */
    default String[] botAdmins() {
        return new String[0];
    }
}
