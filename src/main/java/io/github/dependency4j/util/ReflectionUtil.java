package io.github.dependency4j.util;

import io.github.dependency4j.Virtual;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class ReflectionUtil {
    
    public static void consumeAllVirtualMethodsFromClassType(Class<?> parentClassType,
                                                             Consumer<Method> virtualMethodConsumer) {

        Checks.nonNull(parentClassType, "parentClassType must not be null.");
        Checks.nonNull(virtualMethodConsumer, "virtualMethodConsumer must not be null.");

        for (Method virtualMethod : parentClassType.getMethods()) {

            if (!virtualMethod.isAnnotationPresent(Virtual.class))
                continue;

            virtualMethodConsumer.accept(virtualMethod);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T defaultValueWhenNull(Class<T> classType, T current) {
        return (current == null) ? (T) defaultValueToClassType(classType) : current;
    }

    public static Object defaultValueToClassType(Class<?> classType) {
        if (classType.equals(boolean.class) || classType.equals(Boolean.class))
            return false;
        else if (classType.equals(byte.class) || classType.equals(Byte.class))
            return 0;
        else if (classType.equals(short.class) || classType.equals(Short.class))
            return 0;
        else if (classType.equals(int.class) || classType.equals(Integer.class))
            return 0;
        else if (classType.equals(long.class) || classType.equals(Long.class))
            return 0L;
        else if (classType.equals(float.class) || classType.equals(Float.class))
            return 0F;
        else if (classType.equals(double.class) || classType.equals(Double.class))
            return 0D;
        else
            return null;
    }
}
