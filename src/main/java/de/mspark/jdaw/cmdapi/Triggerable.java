package de.mspark.jdaw.cmdapi;

public interface Triggerable {

    /**
     * A literal which specifies an invocation trigger.
     * 
     * @return
     */
    String trigger();

    /**
     * A short description what this command is for. It is shown in the global command overview inside a big message
     * embeds.
     * 
     * @return
     */
    String description();

    /**
     * Aliases for {@link #trigger()}. They can be used instead of the trigger itself to invoke a command.
     * @return
     */
    String[] aliases();
}