package io.github.dependency4j.util;

public final class StrUtil {

    public static String coalesceBlank(String text, String replace) {
        return isNullOrBlank(text) ? replace : text;
    }

    public static boolean isNullOrBlank(String string) {
        return string == null || string.isBlank();
    }

}
