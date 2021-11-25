package de.mspark.jdaw.jda;


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
    String prefix();
    
    /**
     * Discord API tokens. The first one is always the main token which is used for communicating by default.
     * @return
     */
    String[] apiTokens();
    
    /**
     * List of allowed channels (IDs) where the bot-commands can be used. Allow all when empty.
     * @return
     */
    default String[] channelWhitelist() {
        return new String[0];
    }
}
