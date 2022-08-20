package de.mspark.jdaw.cmdapi;

import java.util.List;

import de.mspark.jdaw.startup.JdawInstance;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * Specification of text command. 
 *
 * @author marcel
 */
public abstract class TextCommand extends JdawEventListener  {
    
    /**
     * A literal which specifies an invocation trigger.
     * 
     * @return
     */
    public abstract String trigger();

    /**
     * A short description what this command is for. It is shown in the global command overview inside a big message
     * embeds.
     * 
     * @return
     */
    public abstract String description();

    /**
     * Aliases for {@link #trigger()}. They can be used instead of the trigger itself to invoke a command.
     * 
     * @return
     */
    public String[] aliases() {
        return new String[0];
    };

    /**
     * Is invoked when Trigger and all configured options like permissions and chat type are matching.
     * 
     * @param msg Invocation message event.
     * @param cmd The list of addiotional arguments of the command. The trigger itself is not present. May be empty when
     *            no arguments were given.
     * @see TextListenerAction
     * @see TextCommand
     */
    public abstract void onTrigger(Message msg, List<String> cmdArguments);

    /**
     * Specified the guild permission which this bot needs in order to execute the command.
     */
    public Permission[] botGuildPermissions() {
        return new Permission[0];
    };

    public Permission[] userChannelPermissions() {
        return new Permission[0];
    };

    /**
     * Specifies the guild permission the user need in order to invoke a command
     * 
     * @return
     */
    public Permission[] userGuildPermissions() {
        return new Permission[0];
    };

    /**
     * Determines if the command should only be invoked by a global bot administrator.
     * 
     * @return
     */
    public boolean botAdminOnly() {
        return false;
    };

    public boolean executableWihtoutArgs() {
        return false;
    };

    /**
     * Specifies if this command can be executed inside a private chat with the bot (typically this is the case when a
     * command isn't specific to a guild like restart commands).
     * 
     * @return
     */
    public boolean privateChatAllowed() {
        return false;
    };

    public DistributionSetting distributionSetting() {
        return DistributionSetting.MAIN_ONLY;
    }

    /**
     * Help page for the command (typically explains all sub-commands).
     * 
     * @return Can be null when no help page for the command is desired otherwise it must contain a printable embed
     */
    public MessageEmbed commandHelpPage() {
        return null;
    }

    /**
     * This method is invoked right after it was registered on a running {@link JDA} via {@link DistributionSetting}.
     * 
     * <br>
     * The default implementation does nothing.
     * 
     * @param stateOnRegistration represents the state which was available during registration. The {@link JdawState#registeredActions()} only contains the action for this command.
     * @see JdawInstance#register(TextCommand...)
     */
    public void onJdaRegistration(JdawState stateOnRegistration) {
        return;
    }
    
}