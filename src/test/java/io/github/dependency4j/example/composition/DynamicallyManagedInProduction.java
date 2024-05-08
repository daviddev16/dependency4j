package io.github.dependency4j.example.composition;

import io.github.dependency4j.Managed;
import io.github.dependency4j.Mapped;
import io.github.dependency4j.Strategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Managed
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicallyManagedInProduction {

    @Mapped("@Managed.dynamic") boolean dynamicDefaultConfiguration() default true;

    @Mapped("@Managed.strategy") Strategy inProductionStrategy() default @Strategy("Production");

}
