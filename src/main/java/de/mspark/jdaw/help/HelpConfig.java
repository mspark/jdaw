package de.mspark.jdaw.help;

import de.mspark.jdaw.startup.JdawInstanceBuilder;

/**
 * Provides meta information about the whole JDAW Instance. This is used for enabling the {@link GlobalHelpCommand}.
 * 
 * @see JdawInstanceBuilder#enableHelpCommand(HelpConfig)
 * @author marcel
 */
public interface HelpConfig {
    
    String botDescription();

    String botName();
}
