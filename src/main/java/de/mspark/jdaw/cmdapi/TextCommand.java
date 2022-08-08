package de.mspark.jdaw.core;

import java.util.List;

import de.mspark.jdaw.config.JDAManager;
import de.mspark.jdaw.guilds.GuildConfigService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

interface RegisterObserver {

    void onRegister(JDAManager usedJdaManager, GuildConfigService usedGuildConfig);
}

public abstract class TextCommand implements RegisterObserver, Triggerable {

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

    public String[] aliases() {
        return new String[0];
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
     * The action which is executed when the command matches.
     * 
     * @param event
     * @param cmd   The list of addiotional arguments of the command. The trigger itself is not present. May be empty
     *              when no arguments were given.
     */
    public abstract void doActionOnTrigger(Message msg, List<String> cmdArguments);

    /**
     * Help page for the command (typically explains all sub-commands).
     * 
     * @return Can be null when no help page for the command is desired otherwise it must contain a printable embed
     */
    public MessageEmbed commandHelpPage() {
        return null;
    }

    /**
     * 
     * @param registeredInstance
     * @param guildConfig
     */
    @Override
    public void onRegister(JDAManager registeredInstance, GuildConfigService guildConfig) {
    }

}