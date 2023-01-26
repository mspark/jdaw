package de.mspark.jdaw.maintainance.availability;

import java.util.Collection;
import java.util.List;

import de.mspark.jdaw.cmdapi.TextCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

public class InviteCmd extends TextCommand {

    private final Collection<Permission> invitePerms;

    public InviteCmd(Collection<Permission> invitePerms) {
        this.invitePerms = invitePerms;
    }

    @Override
    public String trigger() {
        return "!minvite";
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public void onTrigger(Message msg, List<String> cmdArguments) {
        msg.getChannel().sendMessage(msg.getJDA().getInviteUrl(invitePerms)).queue();
    }

    @Override
    public boolean botAdminOnly() {
        return true;
    }
}