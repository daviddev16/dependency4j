package io.github.dependency4j;

import java.util.List;

public interface TypeInformationHolder {

    String getName();

    List<String> getStrategies();

    boolean isDisposable();

    boolean isDynamic();

    Class<?> getWrappedClassType();

}
