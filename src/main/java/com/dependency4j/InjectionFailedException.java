package com.dependency4j;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Executable;
import java.util.StringJoiner;

import static java.lang.String.*;

public class InjectionFailedException extends RuntimeException {

    public InjectionFailedException(String message, Exception cause) {
        super(message, cause);
    }

    public static void throwWithInformation(Injector<?> injector, AccessibleObject accessibleObject,
                                            Object instance, Object[] values, Exception cause) {

        throw new InjectionFailedException(
                format("(%s says): Failed to inject [%s] in [%s] with subject = \"%s\".",
                    injector.getClass().getSimpleName(),
                    createStringListOfObjectsTypes(values),
                    normalizeObjectClassName(instance),
                    getAccessibleObjectName(accessibleObject)),
                cause);
    }

    private static String createStringListOfObjectsTypes(Object[] values) {

        StringJoiner stringJoiner = new StringJoiner(", ");

        for (Object value : values)
            stringJoiner.add( value != null ? value.getClass().getName() : "<null instance>" );

        return "[" + stringJoiner + "]";
    }

    private static String normalizeObjectClassName(Object value) {
        return (value != null) ? value.getClass().getName() : "<null instance>";
    }

    private static String getAccessibleObjectName(AccessibleObject accessibleObject) {

        if (accessibleObject instanceof Executable executable)
            return executable.getName();

        return accessibleObject.getClass().getName();
    }
}
