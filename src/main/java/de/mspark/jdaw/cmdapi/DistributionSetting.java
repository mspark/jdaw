package de.mspark.jdaw.cmdapi;

import java.util.Arrays;

import de.mspark.jdaw.startup.JDAManager;

public enum DistributionSetting {
    /**
     * Only the main bot listens to text commands.
     */
    MAIN_ONLY() {
        @Override
        public void applySetting(JDAManager jdas, TextListenerAction listener) {
            jdas.getMain().addEventListener(listener);
        }
    }, 

    /**
     * Distribute all text commands to all configured discord bots. Make sure all
     * bots have the same permissions.
     */
    BALANCE() {
        @Override
        public void applySetting(JDAManager jdas, TextListenerAction listener) {
            jdas.getNextJDA().addEventListener(listener);
        }
    }, 

    /**
     * Every discord bot listens to every command.
     */
    ALL() {
        @Override
        public void applySetting(JDAManager jdas, TextListenerAction listener) {
            Arrays.stream(jdas.getAllJdaRaw()).forEach(j -> j.addEventListener(listener));
        }
    };

    public abstract void applySetting(JDAManager jdas, TextListenerAction listener);
}
