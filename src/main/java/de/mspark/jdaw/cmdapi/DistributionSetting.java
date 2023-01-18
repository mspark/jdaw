package de.mspark.jdaw.cmdapi;

import java.util.Arrays;

import de.mspark.jdaw.startup.JDAManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Provides setting options for the way commands are distributed to the respective JDA instances. <br>
 * The respective setting determines which Discord bots (one bot per API token) notify on a {@link TextListenerAction}.
 * 
 * @author marcel
 */
public enum DistributionSetting {

    /**
     * Only the main bot listens to text commands.
     */
    MAIN_ONLY() {
        @Override
        public JDA[] applySetting(JDAManager jdas, ListenerAdapter listener) {
            var jda = jdas.getMain();
            jda.addEventListener(listener);
            return new JDA[] {jda};
        }
    }, 

    /**
     * Distribute all text commands to all configured discord bots. Make sure all
     * bots have the same permissions.
     */
    BALANCE() {
        @Override
        public JDA[] applySetting(JDAManager jdas, ListenerAdapter listener) {
            var jda = jdas.getNextJDA();
            jda.addEventListener(listener);
            return new JDA[] {jda};
        }
    }, 

    /**
     * Every discord bot listens to every command.
     */
    ALL() {
        @Override
        public JDA[] applySetting(JDAManager jdas, ListenerAdapter listener) {
            Arrays.stream(jdas.getAllJdaRaw()).forEach(j -> j.addEventListener(listener));
            return jdas.getAllJdaRaw();
        }
    };

    /**
     * Adopts the setting of the current distribution setting directly on the JDA.
     * 
     * @param jdas
     * @param listener
     * @return The JDA instances where the adapter was registered on
     */
    public abstract JDA[] applySetting(JDAManager jdas, ListenerAdapter listener);
}
