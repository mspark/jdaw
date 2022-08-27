package de.mspark.jdaw.guilds;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Representation of a guild specific configuration. 
 * 
 * @author marcel
 */
@Entity
public class SingleGuildSettings {

    /**
     * Guild specific settings in which channels bot commands are allowed.
     *
     * @author marcel
     */
    @Entity
    public static class WhitelistSetting implements Serializable {
        private static final long serialVersionUID = 8468569597844466347L;

        @Id
        protected long guildId;

        @Id
        protected String whitelistChannelId;

        public WhitelistSetting(long guildId, String whitelistChannelId) {
            this.guildId = guildId;
            this.whitelistChannelId = whitelistChannelId;
        }

        public long getGuildId() {
            return guildId;
        }

        public void setGuildId(long guildId) {
            this.guildId = guildId;
        }

        public String getWhitelistChannelId() {
            return whitelistChannelId;
        }

        public void setWhitelistChannelId(String whitelistChannelId) {
            this.whitelistChannelId = whitelistChannelId;
        }

    }
    
    /**
     * Creates a new setting for the given guild.
     * 
     * @param guildId The ID of the discord guild
     * @param listenPrefix The prefix to listen on
     */
    public SingleGuildSettings(long guildId, String listenPrefix) {
        this.guildId = guildId;
        this.listenPrefix = listenPrefix;
    }

    /**
     * Constructor for hibernate
     */
    SingleGuildSettings() {
    }
    
    @Id
    protected long guildId;

    protected String listenPrefix;

    @OneToMany(mappedBy = "guildId", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    protected List<WhitelistSetting> whitelist;

    public long getGuildId() {
        return guildId;
    }

    public List<WhitelistSetting> getChannelWhitelist() {
        return whitelist;
    }

    public String getListenPrefix() {
        return listenPrefix;
    }

    public void setListenPrefix(String listenPrefix) {
        this.listenPrefix = listenPrefix;
    }

    public void setChannelWhitelist(List<WhitelistSetting> whitelist) {
        this.whitelist = whitelist;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

}