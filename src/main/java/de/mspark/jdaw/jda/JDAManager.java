package de.mspark.jdaw.jda;

import net.dv8tion.jda.api.JDA;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/**
 * Thread safe way to receive balance all actions to all available JDA instances. 
 * 
 * This does not mean, that the JDAs by itself are thread safe.
 * 
 * @author marcel
 */
@Component
public class JDAManager {

    private final JDA[] jdas;
    private final AtomicInteger jdaIndex = new AtomicInteger(0);

    public JDAManager(JDA[] jdas) {
        this.jdas = jdas;
    }

    public JDA getNextJDA() {
        jdaIndex.compareAndSet(jdas.length, 0);
        return jdas[jdaIndex.getAndIncrement()];
    }

    public JDA getNextJDAWithoutMain() {
        JDA jda = this.getNextJDA();
        if (this.jdaIndex.get() == 0) {
            return getNextJDAWithoutMain();
        }
        return jda;
    }

    public JDA getMain() {
        return this.jdas[0];
    }
}
