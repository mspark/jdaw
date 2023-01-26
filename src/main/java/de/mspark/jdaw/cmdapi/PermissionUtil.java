package de.mspark.jdaw.cmdapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public class PermissionUtil extends net.dv8tion.jda.internal.utils.PermissionUtil {

    public static boolean memberHasEnoughPermission(Member member, TextCommand cmd) {
        return PermissionUtil.checkPermission(member, cmd.userGuildPermissions());
    }

    public static Collection<Permission> checkForMissingPermission(Set<Permission> memberPerm,
            Collection<Permission> checkPermissions) {
        // TODO smarter would be the bit comparison
        List<Permission> missingPermList = new ArrayList<>();
        checkPermissions.forEach(missingPermList::add);
        missingPermList.removeAll(memberPerm);
        return missingPermList;
    }

    public static Collection<Permission> checkForMissingPermission(GuildChannel channelToCheck, Member userToCheck,
            Collection<Permission> checkPermissions) {
        var memberChannelPermission = PermissionUtil.getExplicitPermission(channelToCheck, userToCheck);
        return checkForMissingPermission(Permission.getPermissions(memberChannelPermission), checkPermissions);
    }

    public static boolean isMemberBotAdmin(User u, List<String> botadmins) {
        return botadmins.stream().anyMatch(ba -> ba.equals(u.getId()));
    }

}