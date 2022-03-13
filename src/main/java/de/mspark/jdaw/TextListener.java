package de.mspark.jdaw;

import de.mspark.jdaw.guilds.GuildConfigService;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class TextListener extends ListenerAdapter {
    protected final GuildConfigService guildConfig;
    
    public TextListener(GuildConfigService guildConfig, JDAManager jdas, DistributionSetting setting) {
        this.guildConfig = guildConfig;
        setting.applySetting(jdas, this);
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (checkAllowedScope(event)) {
            onTextMessageReceived(event);
        }
    }
    
    private boolean checkAllowedScope(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            return isPrivateChatAllowed();
        } else {
            return event.isFromType(ChannelType.TEXT) && isChannelAllowed(event);
        }
    }
    
    

    private boolean isChannelAllowed(MessageReceivedEvent event) {
        var whitelist = guildConfig.getWhitelistChannel(event);
        return whitelist.isEmpty() || whitelist.contains(event.getChannel().getId());
    }

    public abstract boolean isPrivateChatAllowed();
    public abstract void onTextMessageReceived(MessageReceivedEvent event);
}
