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

    /**
     * disposable é usado para definir se a criação da classe será realizada mesmo se não estiver dentro da estrategia
     * de dependencias.
     **/
    boolean disposable() default true;

}
