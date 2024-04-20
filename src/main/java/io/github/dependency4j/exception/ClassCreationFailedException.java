package io.github.dependency4j.exception;

/**
 * @author Daviddev16
 **/
public class ClassCreationFailedException extends RuntimeException {

    public ClassCreationFailedException(Class<?> classType, Throwable cause) {
        super("Failed to create \"%s\".".formatted(classType.getName()), cause);
    }
}
