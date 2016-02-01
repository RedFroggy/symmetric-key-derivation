package fr.redfroggy.sample.derivation.exception;

/**
 * This class represent common smart card exception
 *
 * @author Florent PERINEL
 */
public class DiversificationException extends Exception {

    /**
     * Construct exception with a message and a cause
     *
     * @param message Message of exception
     */
    public DiversificationException(String message) {
        super(message);
    }

    /**
     * Construct exception with a message and a cause
     *
     * @param message Message of exception
     * @param cause   Cause of exception
     */
    public DiversificationException(String message, Throwable cause) {
        super(message, cause);
    }
}