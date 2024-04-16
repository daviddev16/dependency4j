package com.dependency4j;

import java.lang.reflect.AccessibleObject;

public interface Injector<T> {

    @SuppressWarnings("Unchecked")
    default void inject(DependencySearchTree dependencySearchTree, Object instance, AccessibleObject injectable) {
        inject(dependencySearchTree, instance, (T) injectable);
    }

    void inject(DependencySearchTree dependencySearchTree, Object instance, T injectable);

}
