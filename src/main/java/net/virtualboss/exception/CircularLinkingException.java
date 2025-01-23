package net.virtualboss.exception;

public class CircularLinkingException extends RuntimeException {
    public CircularLinkingException(String message) {
        super(message);
    }
}
