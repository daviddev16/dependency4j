package io.github.dependency4j;

import static java.lang.String.*;

public final class ScanFailedException extends RuntimeException {

    public ScanFailedException(String packageName, ClassLoader classLoader, Throwable cause) {
        super(format("Occurred an error while scanning the package \"%s\" with \"%s\" class loader.",
                packageName, classLoader.getName()), cause);
    }

    public ScanFailedException(String message) {
        super(message);
    }

}
