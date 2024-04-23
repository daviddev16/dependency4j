package io.github.dependency4j.util;

import io.github.dependency4j.exception.StateException;

public final class Checks {

    public static <E> E nonNull(E object, String contextualMessage, boolean throwError) {
        if (object == null && throwError)
            throw new NullPointerException(contextualMessage);
        return object;
    }

    public static String nonNullOrBlank(String string, String contextualMessage) {
        if (StrUtil.isNullOrBlank(string))
            throw new NullPointerException(contextualMessage);
        return string;
    }

    public static void state(boolean state, String contextualMessage) {
        if (!state)
            throw new StateException(contextualMessage);
    }

    public static <E> E nonNull(E object, String contextualMessage) {
        return nonNull(object, contextualMessage, true);
    }

}
