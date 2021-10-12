package de.mspark.jdaw.jda;

import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Component;

@Component
public class JDAManager {

    private final JDA[] jdas;
    private int jdaIndex = 0;

    public JDAManager(JDA[] jdas) {
        this.jdas = jdas;
    }

    public JDA getNextJDA() {
        if (this.jdaIndex + 1 >= jdas.length) {
            this.jdaIndex = 0;
        } else {
            this.jdaIndex++;
        }
        JDA jda = jdas[this.jdaIndex];
        return jda;
    }

    public JDA getNextJDAWithoutMain() {
        JDA jda = this.getNextJDA();
        if (this.jdaIndex == 0) {
            return getNextJDAWithoutMain();
        }
        return jda;
    }

    public JDA getMain() {
        return this.jdas[0];
    }
}
