package de.mspark.jdaw.help;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.mspark.jdaw.CommandProperties;

@CommandProperties(trigger = "", executableWihtoutArgs = true, description = "")
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableHelpCommand {}