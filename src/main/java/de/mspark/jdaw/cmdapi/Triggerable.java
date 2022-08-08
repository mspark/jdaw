package de.mspark.jdaw.cmdapi;

public interface Triggerable {
    String trigger();

    String description();

    String[] aliases();
}