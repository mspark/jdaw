package de.mspark.jdaw;

import java.util.List;

import de.mspark.jdaw.config.JDAWConfig;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class TextListener extends ListenerAdapter {
    protected final JDAWConfig conf;
    
    public TextListener(JDAWConfig conf, JDAManager jdas, boolean balance) {
        this.conf = conf;
        if (balance) {
            jdas.getNextJDA().addEventListener(this);            
        } else {
            jdas.getMain().addEventListener(this);
        }
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (checkAllowedScope(event)) {
            onTextMessageReceived(event);
        }
    }
    
    private boolean isChannelAllowed(String cid) {
        return conf.channelWhitelist().length == 0 || List.of(conf.channelWhitelist()).contains(cid);
    }
    
    private boolean checkAllowedScope(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            return isPrivateChatAllowed();
        } else {
            return event.isFromType(ChannelType.TEXT) && isChannelAllowed(event.getChannel().getId());
        }
    }

    public abstract boolean isPrivateChatAllowed();
    public abstract void onTextMessageReceived(MessageReceivedEvent event);
}
