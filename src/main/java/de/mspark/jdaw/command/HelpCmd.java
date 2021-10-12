package de.mspark.jdaw.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@CommandProperties(trigger = "help", executableWihtoutArgs = true)
@Retention(RetentionPolicy.RUNTIME)
public @interface HelpCmd {}