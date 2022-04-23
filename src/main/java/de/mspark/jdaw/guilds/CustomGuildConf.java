package de.mspark.jdaw.guilds;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CustomGuildConf {
	@Id
    private long id;
    private String prefix;
    private String whitelist;

    public CustomGuildConf() {
    }
    
    public CustomGuildConf(long id, String prefix, List<String> whitelist) {
        super();
        this.id = id;
        this.prefix = prefix;
        this.whitelist = whitelist.stream().collect(Collectors.joining(","));
    }
    
    public CustomGuildConf(long id, String prefix) {
        this(id, prefix, Collections.emptyList());
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

    public List<String> whitelist() {
        String[] whitelist = this.whitelist.split(",");
        if (whitelist.length > 0 && !whitelist[0].isBlank()) {
            return List.of(whitelist);
        } 
        return Collections.emptyList();
    }

}