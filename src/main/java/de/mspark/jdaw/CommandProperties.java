package de.mspark.jdaw;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.Permission;

@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandProperties {

    /**
     * Specified the guild permission which this bot needs in order to execute the command.
     */
    Permission[] botGuildPermissions() default {};

    Permission[] userChannelPermissions() default {};

    /**
     * Specifies the guild permission the user need in order to invoke a command
     * @return
     */
    Permission[] userGuildPermissions() default {};
    
    /**
     * Main trigger. It is mandatory and is used for help page generation.
     * @return
     */
    String trigger();

    String[] aliases() default {};
    
    /**
     * Determines if the command should only be invoked by a global bot administrator.
     * @return
     */
    boolean botAdminOnly() default false;
    
    boolean executableWihtoutArgs() default false;
    
    String description();
    
    boolean helpPage() default true;

    /**
     * Specifies if this command can be executed inside a private chat with the bot (typically this is the case when a command
     * isn't specific to a guild like restart commands).
     * @return
     */
    boolean privateChatAllowed() default false;
    
}