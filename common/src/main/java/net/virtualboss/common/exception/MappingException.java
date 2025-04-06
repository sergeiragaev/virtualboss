package net.virtualboss.common.exception;

public class MappingException extends RuntimeException {
    public MappingException(String message, Throwable cause) {
        super(message, cause);
    }
    public MappingException(String message) {
        super(message);
    }
}
