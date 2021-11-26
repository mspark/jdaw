package de.mspark.jdaw.command;

import java.util.List;

import de.mspark.jdaw.jda.JDAManager;
import de.mspark.jdaw.jda.JDAWConfig;
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
        if (event.isFromType(ChannelType.TEXT)
                && isChannelAllowed(event.getChannel().getId())) {
            onTextMessageReceived(event);
        }
    }
    
    private boolean isChannelAllowed(String cid) {
        return conf.channelWhitelist().length == 0 || List.of(conf.channelWhitelist()).contains(cid);
    }

    public abstract void onTextMessageReceived(MessageReceivedEvent event);
    

}
