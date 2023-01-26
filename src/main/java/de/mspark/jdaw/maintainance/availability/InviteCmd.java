package de.mspark.jdaw.maintainance;

import java.util.Collection;

import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InviteCmd extends ListenerAdapter {

    private Collection<Permission> invitePermissions;

    public InviteCmd(Collection<Permission> invitePermissions) {
        this.invitePermissions = invitePermissions;
    }

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getGuildAvailableCount() == 0) {
            var logger = LoggerFactory.getLogger(InviteCmd.class);
            logger.info("Bot is not connected to any server. Generating invite");
            logger.info("Invite: " + event.getJDA().getInviteUrl(invitePermissions));
        }
    }

}