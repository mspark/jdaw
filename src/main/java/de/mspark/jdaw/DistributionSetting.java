package de.mspark.jdaw;

import java.util.Arrays;

public enum DistributionSetting {
    /**
     * Only the main bot listens to text commands.
     */
    MAIN_ONLY() {
        @Override
        public void applySetting(JDAManager jdas, TextListener listener) {
            jdas.getMain().addEventListener(listener);
        }
    }, 

    /**
     * Distribute all text commands to all configured discord bots. Make sure all
     * bots have the same permissions.
     */
    BALANCE() {
        @Override
        public void applySetting(JDAManager jdas, TextListener listener) {
            jdas.getNextJDA().addEventListener(listener);
        }
    }, 

    /**
     * Every discord bot listens to every command.
     */
    ALL() {
        @Override
        public void applySetting(JDAManager jdas, TextListener listener) {
            Arrays.stream(jdas.getAll()).forEach(j -> j.addEventListener(listener));
        }
    };

    public abstract void applySetting(JDAManager jdas, TextListener listener);
}
