package de.mspark.jdaw.maintainance.availability;

import java.util.Collection;

import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InviteCheck extends ListenerAdapter {
    private Collection<Permission> invitePermissions;

    public InviteCheck(Collection<Permission> invitePermissions) {
        this.invitePermissions = invitePermissions;
    }

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getGuildAvailableCount() == 6) {
            var logger = LoggerFactory.getLogger(InviteCmd.class);
            logger.info("Bot is not connected to any server. Generating invite");
            logger.info("Invite: " + event.getJDA().getInviteUrl(invitePermissions));
        }
    }
}
