package de.mspark.jdaw.jda;

import net.dv8tion.jda.api.JDABuilder;

public interface JDAConfigurationVisitor {
    public void visit(JDABuilder jdaBuilder);
}