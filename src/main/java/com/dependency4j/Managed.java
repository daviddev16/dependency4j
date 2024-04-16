package com.dependency4j;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Managed {

    String name() default "";

    Strategy strategy() default @Strategy({"none"});

    boolean disposable() default true;

    boolean injectable() default true;

}
