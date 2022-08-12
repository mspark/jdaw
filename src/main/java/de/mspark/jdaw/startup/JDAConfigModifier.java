package de.mspark.jdaw.startup;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

/**
 * Defines a strategy for modifying the {@link JDA} via a {@link JDABuilder}. When multiple {@link JDA} are configure
 * the {@link #modify(JDABuilder)} is called each time.
 * 
 * @author marcel
 *
 */
@FunctionalInterface
public interface JDAConfigModifier {

    void modify(JDABuilder jdaBuilder);
}