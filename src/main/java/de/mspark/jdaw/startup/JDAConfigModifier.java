package de.mspark.jdaw.startup;

import net.dv8tion.jda.api.JDABuilder;

@FunctionalInterface
public interface JDAConfigModifier {
    void modify(JDABuilder jdaBuilder);
}