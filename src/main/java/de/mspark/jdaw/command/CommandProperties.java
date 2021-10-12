package de.mspark.jdaw.command;

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
    
    String trigger();
    
    boolean executableWihtoutArgs() default false;

//    String[] aliases() default {};
}