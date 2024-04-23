package io.github.dependency4j.exception;

public class StateException extends RuntimeException {

    public StateException(String message) {
        super(message);
    }

    public StateException(String message, Throwable cause) {
        super(message, cause);
    }
}
