package de.mspark.jdaw.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@CommandProperties(trigger = "", executableWihtoutArgs = true, description = "")
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableHelpCommand {}