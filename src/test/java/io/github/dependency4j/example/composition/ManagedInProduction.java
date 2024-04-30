package io.github.dependency4j.example.composition;

import io.github.dependency4j.Managed;
import io.github.dependency4j.Strategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Managed(strategy = @Strategy("Production"))
public @interface ManagedInProduction { }
