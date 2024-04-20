package com.dependency4j.exception;

/**
 * @author Daviddev16
 **/
public class InstallationFailedException extends RuntimeException {

    public InstallationFailedException(String packageName, Throwable cause) {
        super("Installation failed of class type \"%s\".".formatted(packageName), cause);
    }

}
