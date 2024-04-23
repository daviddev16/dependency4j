package io.github.dependency4j.exception;

public class ReflectionStateException extends StateException {

    public ReflectionStateException(String message) {
        super(message);
    }

    public ReflectionStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
