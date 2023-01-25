package de.mspark.jdaw.help;

import de.mspark.jdaw.startup.JdawInstanceBuilder;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Provides meta information about the whole JDAW Instance. This is used for enabling the {@link GlobalHelpCommand}.
 * 
 * @see JdawInstanceBuilder#enableHelpCommand(HelpConfig)
 * @author marcel
 */
public interface HelpConfig {
    
    String botDescription();

    /**
     * The name of the bot. Will be shown in the help page and changelog. 
     * 
     * @return
     */
    String botName();
    
    /**
     * The Author(s) of this bot. Will be shown in the help page and changelog. If not set, no Footer is added.
     * 
     * @return May be empty
     */
    default String botAuthor() {
        return null;
    }
    
    /**
     * An icon for better author representation. Is used as footer icon. If not set, no Footer is added.
     * 
     * @return May be empty
     */
    default String botAuthorIcon() {
        return null;
    }
    
    public static void addFooter(EmbedBuilder eb, HelpConfig config) {
        if (config.botAuthor() != null) {
            if (config.botAuthorIcon() != null) {
                eb.setFooter(config.botAuthor(), config.botAuthorIcon());
            } else {
                eb.setFooter(config.botAuthor());
            }
        }
    }
}
