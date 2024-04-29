package io.github.dependency4j.exception;

public final class ScanFailedException extends RuntimeException {

    public ScanFailedException(String message) {
        super(message);
    }

    public ScanFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
