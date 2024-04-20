package com.dependency4j.util;

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


    public static <E> E nonNull(E object, String contextualMessage) {
        return nonNull(object, contextualMessage, true);
    }

}
