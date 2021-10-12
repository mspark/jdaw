package de.mspark.jdaw.command;

import java.util.stream.Stream;

import de.mspark.jdaw.jda.JDAManager;
import de.mspark.jdaw.jda.JDAWConfig;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class TextListener extends ListenerAdapter {
    protected final JDAWConfig conf;
    
    public TextListener(JDAWConfig conf, JDAManager jdas) {
        this.conf = conf;
        jdas.getMain().addEventListener(this);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT)
                && isChannelAllowed(event.getChannel().getId())) {
            onTextMessageReceived(event);
        }
    }
    
    private boolean isChannelAllowed(String cid) {
        return conf.channelWhitelist().length > 0 || Stream.of(conf.channelWhitelist()).toList().contains(cid);
    }

    public abstract void onTextMessageReceived(MessageReceivedEvent event);
}
