package de.mspark.jdaw.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@CommandProperties(trigger = "", executableWihtoutArgs = true)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableHelpCommand {}