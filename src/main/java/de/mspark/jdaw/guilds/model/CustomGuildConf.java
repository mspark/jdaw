package de.mspark.jdaw.guilds.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CustomGuildConf {
    @Id
    long id;
    String prefix;
    String[] whitelist;

    public CustomGuildConf(long id, String prefix, String[] whitelist) {
        super();
        this.id = id;
        this.prefix = prefix;
        this.whitelist = whitelist;
    }

    public long id() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String prefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String[] whitelist() {
        return whitelist;
    }

    public void setWhitelist(String[] whitelist) {
        this.whitelist = whitelist;
    }

}