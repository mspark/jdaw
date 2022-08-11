package de.mspark.jdaw.cmdapi;

import de.mspark.jdaw.startup.JdawInstance;

/**
 * Event listener for Events which happen in {@link TextListenerAction} and {@link JdawInstance}.
 * 
 * @author marcel
 */
public interface JdawEventListener {
    
    
    // IDEAS
//    void onRegistrationAttempt(TextCommand commandToRegister);
    
//    void onPermissionError(Message msg);  
    
    /**
     * Is invoked on a new registration event when {@link JdawInstance#register(TextCommand...) is called. 
     * 
     * @param stateOnRegistrationAttempt The old state before latest registration.
     * @param newRegisteredAction The new registered command (it already listens to discord events) 
     * @see JdawInstance#register(TextCommand...)
     */
    public void onNewRegistration(JdawState stateOnRegistrationAttempt, TextListenerAction newRegisteredAction);
}