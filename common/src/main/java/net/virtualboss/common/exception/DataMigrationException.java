package net.virtualboss.common.exception;

public class DataMigrationException extends RuntimeException {
    public DataMigrationException(String message) {
        super(message);
    }

    public DataMigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
