package io.github.dependency4j;

import java.lang.annotation.*;
import io.github.dependency4j.node.*;

/**
 *
 * <b>Managed</b> marks Java classes as managed classes. This annotation is used
 * to indicate that a class should be managed by the {@link DependencyManager}.
 * Managed classes are instantiated by the dependency manager. A managed class can be
 * dynamic, meaning that these classes will be instantiated dynamically, and will not
 * be initialized in the {@link DependencyManager#installPackage(String)} function. Instead,
 * the dynamic setting is meant to be used in classes that will be instantiated with
 * {@link DependencyManager#installType(Class)} and {@link DependencyManager#installInstance(Object)}
 * functions.
 *
 * @author daviddev16
 * @version 1.0
 *
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Managed {

    /**
     *
     * The managed class name. This is also the {@link SingletonNode} node name. If no
     * name is specified {@link SingletonNode} will assume the name of the managed class
     * type, equivalent to {@link Class#getSimpleName()}.
     *
     * @since 1.0
     *
     **/
    String name() default "";

    /**
     *
     * The strategy definition is used to determine whether a managed class is able to be
     * instantiated by the {@link DependencyManager}. If no strategy is defined, the strategy
     * is set to "none".
     *
     * @since 1.0
     *
     **/

    Strategy strategy() default @Strategy({"none"});

    /**
     *
     * Disposable indicates to the {@link DependencyManager} package installation that a
     * managed classes should be discarded if {@link Managed#strategy()} does not match
     * with any of {@link DependencyManager#getStrategies()}. By default, the value is
     * {@code true}.
     * <p>
     * Disposable set to {@code false} should be used whenever you want to instantiate
     * the managed class, regardless of the manager's strategies. It enables managed classes
     * instantiation and dependency injection to be done at a <i>Package-Installation-Level</i>.
     *
     * @since 1.0
     *
     **/
    boolean disposable() default true;

    /**
     *
     * Dynamic tells to the {@link DependencyManager} package installation that a
     * managed classes should be discarded. Dynamic classes are used later, and are
     * created using {@link DependencyManager#installInstance(Object)} or
     * {@link DependencyManager#installType(Class)} to perform instantiation and
     * dependency injection dynamically. By default, the value is {@code false}.
     * <p>
     * It enables managed classes instantiation and dependency injection to be done
     * at a <i>Dynamic-Installation-Level</i>.
     * @since 1.0.2
     *
     **/
    boolean dynamic() default false;

}
