package io.github.dependency4j.exception;

/**
 * @author Daviddev16
 **/
public class InstallationFailedException extends RuntimeException {

    public InstallationFailedException(String packageName, Throwable cause) {
        super("Package \"%s\" installation failed.".formatted(packageName), cause);
    }
}
