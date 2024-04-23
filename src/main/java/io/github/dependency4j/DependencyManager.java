package io.github.dependency4j;

import io.github.dependency4j.exception.ClassCreationFailedException;
import io.github.dependency4j.exception.InstallationFailedException;
import io.github.dependency4j.exception.MemberInjectionFailedException;
import io.github.dependency4j.exception.ReflectionStateException;
import io.github.dependency4j.node.SingletonNode;
import io.github.dependency4j.util.Checks;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.dependency4j.util.StrUtil.*;

/**
 *
 * <b>DependencyManager</b> is the main core manager class. This class has a
 * {@link DependencySearchTree}, it handles the insertion by scanning the classes from
 * an arbitrary package path. This class is responsible for finding and instantiating
 * classes annotated with {@link Managed} annotation. The class propagates the dependencies
 * through the instantiation of an object. It supports three types of dependency injection:
 * <pre>
 *  1. Constructor injection
 *  2. Method invocation injection
 *  3. Field injection
 * </pre>
 * @author daviddev16
 * @version 1.0
 *
 **/
@Managed(dynamic = true)
public final class DependencyManager {

    private final DependencySearchTree dependencySearchTree;
    private final Set<String> strategies;

    public static DependencyManagerChainBuilder builder() {
        return new DependencyManagerChainBuilder();
    }

    public DependencyManager() {
        dependencySearchTree = new DependencySearchTree();
        strategies           = new HashSet<>();
    }

    /**
     *
     * Given a package name, install function searches for all classes annotated with
     * {@link Managed} annotation, appends all available classes to the {@link DependencySearchTree},
     * then starts the recursive instantiation. An available class is a managed class
     * with {@code dynamic} parameter set to true (default value for {@link Managed}.
     * <p>
     * The recursive instantiation works by creating objects whose parameters are managed
     * classes. When a non-managed class is passed, the installation process will give
     * a null value to the non-managed parameter.
     *
     * @param packagePath Package path
     *
     * @throws NullPointerException        {@code packageName} is null or blank.
     * @throws InstallationFailedException When any error occurs while package installation,
     *                                     It will give a cause exception.
     *
     * @since 1.0
     *
     **/
    public void installPackage(String packagePath) {
        try {
            Checks.state(!isNullOrBlank(packagePath), "packageName must not be null or blank.");

            Set<Class<?>> managedClassSet = ClassFinder.scanPackage(packagePath)
                    .stream()
                    .filter(classType -> classType.isAnnotationPresent(Managed.class))
                        .filter(this::checkNonAbstractClassType)
                        .filter(this::checkClassAvailabilityBefore)
                    .collect(Collectors.toSet());

            managedClassSet.forEach(dependencySearchTree::insert);

            managedClassSet.stream()
                    .filter(classType -> !classType.isInterface())
                    .forEach(this::instantiateWithInjection);

        } catch (Exception exception) {
            throw new InstallationFailedException(packagePath, exception);
        }
    }

    /**
     *
     * Performs method and field injection to {@code instance}.
     * <p>
     * The instance class does not need to be annotated with {@link Managed}, but fields and
     * methods can be annotated with {@link Pull} to enable dependency injection.
     * <p>
     * This function also allows you to choose the installation type. When using
     * {@link InstallationType#DEFAULT}, the class type and its instance will be appended
     * to the {@link DependencySearchTree} and can be used later to search with
     * {@code query()} for the same singleton type. If {@link  InstallationType#STANDALONE}
     * is used, the function will only inject dependencies to its fields and methods.
     * The class type will not be handled by the search tree. This function does not
     * support <i>constructor injection</i>.
     *
     * @param instance The instance object that will receive the dependency injection.
     * @param installationType The selected installation type.
     *
     * @return The same {@code instance} object.
     *
     * @throws NullPointerException           When {@code object} or {@code installationType} is null.
     * @throws MemberInjectionFailedException May occur while performing field/method injection.
     *                                        It will give a cause exception.
     *
     * @see DependencyManager#installInstance(Object)
     *
     * @since 1.0
     *
     **/
    public Object installInstance(Object instance, InstallationType installationType) {

        Checks.nonNull(instance, "object must not be null.");
        Checks.nonNull(installationType, "installationType must not be null.");

        performMethodAndFieldInjection(instance);

        if (installationType != InstallationType.STANDALONE)
            dependencySearchTree.insertPropagation(instance.getClass(), instance);

        return instance;
    }

    /**
     *
     * Performs method invocation and field injection to {@code instance}.
     * <p>
     * The instance class does not need to be annotated with {@link Managed}, but fields and
     * methods can be annotated with {@link Pull} to enable dependency injection.
     * <p>
     * By default, the installation type of this function is {@link InstallationType#DEFAULT}.
     * This function does not support <i>constructor injection</i>.
     *
     * @param instance The instance object that will receive the dependency injection.
     *
     * @return The same {@code instance} object.
     *
     * @throws NullPointerException           When {@code object} or {@code installationType} is null.
     * @throws MemberInjectionFailedException May occur while performing field/method injection.
     *                                        It will give a cause exception.
     *
     * @see DependencyManager#installInstance(Object, InstallationType)
     *
     * @since 1.0
     *
     **/
    public Object installInstance(Object instance) {
        return installInstance(instance, InstallationType.DEFAULT);
    }

    /**
     *
     * Creates an instance for a given {@code classType}. This functions acts similar
     * to {@link DependencyManager#installPackage(String)}, initializing the class type
     * with an instance that supports all kinds of dependency injection <i>(Constructor,
     * Field and Method)</i>. It also supports dynamic {@link Managed} classes, allowing
     * detached objects to be instantiated.
     * <p>
     * The class type does not need to be annotated with {@link Managed}, but the constructor,
     * fields or methods can be annotated with {@link Pull} to enable dependency injection.
     * <p>
     * This function also allows you to choose the installation type. When using
     * {@link InstallationType#DEFAULT}, the class type and its instance will be appended
     * to the {@link DependencySearchTree} and can be used later to search with
     * {@code query()} for the same singleton type. If {@link  InstallationType#STANDALONE}
     * is used, the function will only inject dependencies to its constructor, fields or
     * methods. The class type will not be handled by the search tree.
     *
     * @param classType The class type to be instantiated.
     * @param installationType The selected installation type.
     *
     * @return An instance of {@code classType}.
     *
     * @throws NullPointerException           When {@code classType} or {@code installationType} is null.
     * @throws ClassCreationFailedException   May occur during instance creation and dependency injection
     *                                        process. It will give a cause exception.
     *
     * @see DependencyManager#installInstance(Object, InstallationType)
     *
     * @since 1.0
     *
     **/
    public <T> T installType(Class<T> classType, InstallationType installationType) {

        Checks.nonNull(classType, "classType must not be null.");
        Checks.nonNull(installationType, "installationType must not be null.");

        if (installationType != InstallationType.STANDALONE)
            dependencySearchTree.insert(classType);

        return instantiateWithInjection(classType);
    }

    /**
     *
     * Creates an instance for a given {@code classType}. This functions acts similar
     * to {@link DependencyManager#installPackage(String)}, initializing the class type
     * with an instance that supports all kinds of dependency injection <i>(Constructor,
     * Field and Method)</i>. It also supports dynamic {@link Managed} classes, allowing
     * detached objects to be instantiated.
     * <p>
     * The class type does not need to be annotated with {@link Managed}, but the constructor,
     * fields or methods can be annotated with {@link Pull} to enable dependency injection.
     * <p>
     * By default, the installation type of this function is {@link InstallationType#DEFAULT}.
     *
     * @param classType The class type to be instantiated.
     *
     * @return An instance of {@code classType}.
     *
     * @throws NullPointerException           When {@code classType} is null.
     * @throws ClassCreationFailedException   May occur during instance creation and dependency injection
     *                                        process. It will give a cause exception.
     *
     * @see DependencyManager#installInstance(Object, InstallationType)
     *
     * @since 1.0
     *
     **/
    public <T> T installType(Class<T> classType) {
        return installType(classType, InstallationType.DEFAULT);
    }

    /**
     *
     * The main core function of {@link DependencyManager}. This function starts the class type
     * instantiation and dependency propagation through the constructor. Firstly, the function
     * will try to find an already instantiated object for {@code classType}, if no instance is
     * found, it will start creating the instance from scratch.
     * <p>
     * The function is only able to create constructors with more than one parameter if the
     * constructor is annotated with {@link Pull}. If no constructor annotated is found, it will
     * try to create with an empty constructor method.
     * <p>
     * After instantiation and constructor injection, a method and field injection will be
     * performed in methods and fields annotated with {@link Pull} to the built instance.
     * <p>
     * All instances created by this function will be propagated to the {@link DependencySearchTree}.
     *
     * @param classType The class type to be instantiated.
     *
     * @return A fully handled instance of {@code classType}.
     *
     * @throws ClassCreationFailedException May occur during instance creation and dependency
     *                                      injection process. It will give a cause exception.

     * @throws IllegalStateException        If the function could not instantiate any of the
     *                                      class constructors.
     *
     * @see DependencyManager#installPackage(String)
     * @see DependencyManager#installType(Class, InstallationType)
     * @see DependencyManager#installType(Class)
     *
     * @since 1.0
     *
     **/
    @SuppressWarnings("Unchecked")
    private <T> T instantiateWithInjection(Class<T> classType) {
        try {
            SingletonNode classTypeSingletonNode
                    = dependencySearchTree.querySingletonNode(classType, QueryOptions.none());

            if (classTypeSingletonNode != null && classTypeSingletonNode.getNodeInstance() != null)
                return (T) classTypeSingletonNode.getNodeInstance();

            Constructor<?> constructorAnnotatedWithPull = getConstructorAnnotatedWithPull(classType);
            Object newInstanceOfType;

            if(constructorAnnotatedWithPull != null) {
                Parameter[] constructorParameters = constructorAnnotatedWithPull.getParameters();

                List<Object> listOfCreatedObjects =
                        createObjectsFromParameters(classType, constructorAnnotatedWithPull, constructorParameters);

                newInstanceOfType = constructorAnnotatedWithPull.newInstance(listOfCreatedObjects.toArray());
            } else {
                newInstanceOfType = createInstanceWithEmptyConstructorFromClassType(classType);
            }
            if (newInstanceOfType == null)
                throw new IllegalStateException("Could not created a instance to " + classType.getSimpleName());

            performMethodAndFieldInjection(newInstanceOfType);
            dependencySearchTree.propagateSingletonInstanceToNodes(classType, newInstanceOfType);

            return (T) newInstanceOfType;

        } catch (Exception exception) {
            throw new ClassCreationFailedException(classType, exception);
        }
    }

    /**
     *
     * This function works together with {@link #fetchOrCreateObjectFromClassType(Class, Class, AccessibleObject)}
     * To fetch and create a list of all required objects for a specific {@code accessibleObject}.
     * It will map with Stream all the parameters and collect the objects to that specific
     * parameter.
     * <p>
     * In the current implementation of Dependency4j, an {@code accessibleObject} can be a
     * {@link Constructor} or {@link Method}.
     *
     * @param parentClassType  Is the class type of the current handled instance.
     * @param accessibleObject A {@link Constructor} or {@link Method} depending on the current
     *                         injection method being executed.
     * @param parameters       The list of parameters of the current {@code accessibleObject}.
     *
     * @return A {@link List} of objects instances. An element can be null when no instance was
     *         found to that particular parameter type.
     *         
     * @see #fetchOrCreateObjectFromClassType(Class, Class, AccessibleObject)
     *
     * @since 1.0
     *
     **/
    private List<Object> createObjectsFromParameters(Class<?> parentClassType,
                                                     AccessibleObject accessibleObject, Parameter[] parameters) {
        return Stream.of(parameters)
                .map(parameter
                        -> fetchOrCreateObjectFromClassType(parentClassType, parameter.getType(), accessibleObject))
                .collect(Collectors.toList());
    }

    /**
     *
     * This method is used to fetch/retrieve a valid object for the {@code subjectClassType}
     * required on the current injection type. This function works together with
     * {@code instantiateWithInjection} to guarantee the correct instantiation and injection
     * for all classes being installed through:
     *
     * {@link #installPackage(String)},
     * {@link #installType(Class, InstallationType)} and
     * {@link #createObjectsFromParameters(Class, AccessibleObject, Parameter[])}
     *
     * functions.
     *
     * @param parentClassType the parent instantiated object class type.
     *
     * @param subjectClassType The class type relative to the member that is being injected.
     *                         For a field injection, this is equivalent to {@link Field#getType()},
     *                         and for a method invoke injection, this is equivalent to the current
     *                         parameter type being fetched and injected, equivalent to
     *                         {@link Parameter#getType()}.
     *
     * @param accessibleObject The member that is being injected. The value can be a {@link Method},
     *                         {@link Field} or {@link Constructor} reference, it depends on the type
     *                         of the current injection that calls this method.
     *
     * @return assignable object for {@code subjectClassType}. Returns null if no {@link SingletonNode}
     *         is found on the {@link DependencySearchTree}.
     *
     * @throws IllegalStateException If parentClassType and subjectClassType are equals, meaning
     *                               a loop on itself.
     *
     * @since 1.0
     *
     **/
    private Object fetchOrCreateObjectFromClassType(Class<?> parentClassType, Class<?> subjectClassType,
                                                    AccessibleObject accessibleObject) {

        Pull pullAnnotation = accessibleObject.getAnnotation(Pull.class);

        QueryOptions queryOptions = AnnotationTransformer
                .transformPullAnnotationToQueryOptions(pullAnnotation);

        SingletonNode singletonNode = dependencySearchTree
                .querySingletonNode(subjectClassType, queryOptions);

        /* singleton nodes are null when the class type was not found in the installPackage section. */
        if (singletonNode == null)
            return null;

        if (parentClassType.equals(singletonNode.getNodeClassType()))
            throw new IllegalStateException("\"" + parentClassType.getSimpleName() +
                    "\" loops itself on member: \"" + accessibleObject + "\".");


        Object instanceValue = singletonNode.getNodeInstance();

        return (instanceValue != null) ? instanceValue :
                instantiateWithInjection(singletonNode.getNodeClassType());
    }

    /**
     *
     * This method is used to fetch/retrieve a valid object for the {@code subjectClassType}
     * Creates an instance of {@code classType} using its empty constructor. This function
     * is used by {@link #instantiateWithInjection(Class)} when a constructor annotated with
     * {@link Pull} was not found.
     *
     * @param classType The class type to be instantiated.
     *
     * @return An instance of {@code classType}.
     *
     * @throws ReflectionStateException May occur during instantiation of an empty constructor
     *                                  process. It will give a cause exception.
     *
     * @since 1.0
     *
     * */
    @SuppressWarnings("Unchecked")
    private Object createInstanceWithEmptyConstructorFromClassType(Class<?> classType) {
        try {
            Constructor<?>[] constructors = classType.getConstructors();
            Constructor<?> emptyConstructor = null;

            for (Constructor<?> constructor : constructors)
                if (constructor.getParameterCount() == 0) {
                    emptyConstructor = constructor;
                    break;
                }

            if (emptyConstructor == null)
                return null;

            return emptyConstructor.newInstance();
        } catch (Exception e) {
            if (e instanceof InvocationTargetException)
                throw new ReflectionStateException("The empty constructor of \"%s\" threw a error."
                        .formatted(classType.getName()), e);

            else if (e instanceof IllegalAccessException)
                throw new ReflectionStateException("The empty constructor of \"%s\" is inaccessible."
                        .formatted(classType.getName()), e);
            else
                throw new ReflectionStateException("An error ocurred while instantiating the" +
                        " empty constructor of \"%s\".".formatted(classType.getName()), e);
        }
    }

    /**
     *
     * Performs field injection in a {@code instance}. This function is used with
     * {@link #performMethodAndFieldInjection(Object)} to perform both method and field
     * injection. The field injection is done on each field of the instance object and
     * uses {@link #fetchOrCreateObjectFromClassType(Class, Class, AccessibleObject)} to
     * get and set the assignable instance value.
     *
     * @param instance A receiver object to field injection.
     *
     * @throws MemberInjectionFailedException May occur during field injection if any
     *                                        reflexive exception is thrown. It will
     *                                        give a cause exception.
     *
     * @since 1.0
     *
     **/
    private void performFieldInjection(Object instance) {

        Class<?> parentClassType = instance.getClass();

        for (Field field : parentClassType.getDeclaredFields()) {

            if (!field.isAnnotationPresent(Pull.class))
                continue;

            Class<?> fieldClassType = field.getType();

            Object objectFromClassType =
                    fetchOrCreateObjectFromClassType(parentClassType, fieldClassType, field);

            if (!field.canAccess(instance))
                field.setAccessible(true);

            try {
                field.set(instance, objectFromClassType);
            } catch (IllegalAccessException cause) {
                throw new MemberInjectionFailedException(field, parentClassType, cause);
            }
        }
    }

    /**
     *
     * Performs method invocation injection in a {@code instance}. This function is used with
     * {@link #performMethodAndFieldInjection(Object)} to perform both method and field
     * injection. Method injection occurs in any method whose name begins with "set" and it is
     * annotated with {@link Pull} in a given instance object. It uses
     * {@link #createObjectsFromParameters(Class, AccessibleObject, Parameter[])} to get and set
     * the assignable instances values.
     *
     * @param instance A receiver object to method injection.
     *
     * @throws MemberInjectionFailedException May occur during method injection if any
     *                                        reflexive exception is thrown. It will
     *                                        give a cause exception.
     *
     * @since 1.0
     *
     **/
    private void performSetterMethodInvocationInjection(Object instance) {

        Class<?> parentClassType = instance.getClass();

        for (Method method : parentClassType.getDeclaredMethods()) {

            if (!method.isAnnotationPresent(Pull.class) || !method.getName().startsWith("set"))
                continue;

            Parameter[] parameters = method.getParameters();

            List<Object> instantiatedValues =
                    createObjectsFromParameters(parentClassType, method, parameters);

            try {
                method.invoke(instance, instantiatedValues.toArray());
            } catch (IllegalAccessException | InvocationTargetException cause) {
                throw new MemberInjectionFailedException(method, parentClassType, cause);
            }
        }
    }

    /**
     *
     * Performs method invocation and field injection in a {@code instance}.
     * Method injection occurs in any method whose name begins with "set" and
     * it is annotated with {@link Pull} in a given instance object. It uses
     * {@link #createObjectsFromParameters(Class, AccessibleObject, Parameter[])}
     * to get and set the assignable instances values. The field injection is
     * done on each field of the instance object and uses
     * {@link #fetchOrCreateObjectFromClassType(Class, Class, AccessibleObject)}
     * to get and set the assignable instance value.
     *
     * @param instance A receiver object to method injection.
     *
     * @throws MemberInjectionFailedException May occur during method/field injection if
     *                                        any reflexive exception is thrown. It will
     *                                        give a cause exception.
     *
     * @since 1.0
     *
     **/
    private void performMethodAndFieldInjection(Object instance) {
        performSetterMethodInvocationInjection(instance);
        performFieldInjection(instance);
    }

    /**
     *
     * Checks if a given class type can be instantiated and handled by the manager.
     * Given a class type, this function returns the first {@link Constructor} annotated
     * with {@link Pull}. Used by {@link #instantiateWithInjection(Class)} to perform
     * constructor injection.
     *
     * @param classType The class type to be instantiated.
     *
     * @return The first constructor annotated will {@link Pull}
     *
     * @see #instantiateWithInjection(Class)
     *
     * @since 1.0
     *
     **/
    private Constructor<?> getConstructorAnnotatedWithPull(Class<?> classType) {
        return Stream.of(classType.getConstructors())
                .filter(constructor
                        -> constructor.isAnnotationPresent(Pull.class))
                .findFirst()
                .orElse(null);
    }

    /**
     *
     * Checks if a given class type can be instantiated and handled by the manager.
     * The availability options are:
     * <pre>
     * 1. It checks if the {@code dynamic} parameter of {@link Managed} is {@code true}.
     * If it is {@code true}, then it will return false since dynamic classes are used
     * for handling dynamic dependency injection on new instances during the program
     * execution with {@link #installType(Class)} and {@link #installInstance(Object)}
     * functions.
     * 2. If no strategy was defined on {@link DependencyManager} instance,
     * there are no restrictions to instantiate a package class. It will return
     * {@code true}.
     * 3. If strategies were defined, the function will check the {@link Managed}
     * strategies to check if at least one them match with the {@link DependencyManager}
     * strategies. If one matches, it will return {@code true}.
     * 4. If no strategy matched and the {@link Managed} is not disposable, the
     * class must be instantiated anyway. It will return {@code true}.
     *</pre>
     *
     * @param classType The class type to be checked.
     *
     * @return {@code true} if the class type match with the availability options.
     *
     * @see Managed#dynamic()
     * @see Managed#disposable()
     * @see Managed#strategy()
     *
     * @since 1.0
     *
     **/
    private boolean checkClassAvailabilityBefore(Class<?> classType) {

        Managed managedAnnotation = classType.getAnnotation(Managed.class);
        boolean flagInstanceAnyways = !managedAnnotation.disposable();

        if (managedAnnotation.dynamic())
            return false;

        /*
         * if no strategy was defined, all managed classes
         * should be instantiated.
         */
        if (strategies.isEmpty())
            return true;

        Strategy strategyAnn = managedAnnotation.strategy();

        for (String classStrategyName : strategyAnn.value()) {
            for (String strategyName : strategies) {
                if (classStrategyName.equals(strategyName))
                    return true;
            }
        }

        return flagInstanceAnyways;
    }

    /**
     *
     * Checks if {@code classType} is not an interface or abstract class. This classes
     * types cannot be instantiated. Used in {@link #installPackage(String)}
     *
     * @param classType The class type to be checked.
     *
     * @return {@code true} if {@code classType} is neither an interface nor an abstract
     *         class.
     *
     * @since 1.0.2
     *
     **/
    public boolean checkNonAbstractClassType(Class<?> classType) {
        return !classType.isInterface() && !Modifier.isAbstract(classType.getModifiers());
    }

    /**
     *
     * Append the current {@link DependencyManager} instance to itself {@link DependencySearchTree}.
     * This function can be useful when a dependency class uses {@code query()} function to
     * search through the tree. This is equivalent to:
     * <pre>
     *  dependencySearchTree.insertPropagation(DependencyManager.class, dependencyManager);
     *  <i>or</i>
     *  dependencyManager.installInstance(dependencyManager);
     * </pre>
     *
     * @since 1.0.2
     *
     **/
    public void includeDependencyManagerAsDependency() {
        dependencySearchTree.insertPropagation(DependencyManager.class, this);
    }

    /**
     *
     * Adds strategies to the strategy set of {@link DependencyManager}.
     * Strategies will eventually be used to define which class should be instantiated
     * and handled by the dependency search tree. This function should be called before
     * {@link #installPackage(String)} to apply the strategy validation.
     *
     * @param strategyNames A single or multiple strategy names.
     *
     * @throws NullPointerException If any of {@code strategyNames} is null or blank.
     *
     * @since 1.0.2
     *
     **/
    public void addStrategy(String... strategyNames) {
        Arrays.stream(strategyNames)
                .map(strategyName -> Checks.nonNullOrBlank(strategyName, "The strategy name must not be null."))
                .forEach(strategies::add);
    }

    /**
     * Acts like a facade to {@link DependencySearchTree#query(Class, QueryOptions)}.
     *
     * Given a {@code classType} and {@code queryOptions}, this function should
     * return a correspondent singleton instance of the specified class type.
     * {@code queryOptions} can be used to specify certain criteria to the search.
     * <p>
     * When {@link QueryOptions#retrieveAnyways()} is true, the function will bring
     * the most non-null value, which means that if you specify a filter that searches
     * by the name of the Managed instance, and the name was not found, the query
     * function still returns some instance if it exists in the tree hierarchy.
     *
     * @param classType    The class type to be searched in the tree.
     * @param queryOptions Optional configuration to the search.
     *
     * @return A child or itself instance of the class type passed in {code classType}.
     *         If no correspondent was found, the function will return null.
     *
     * @since 1.0
     *
     **/
    public <T> T query(Class<? extends T> classType, QueryOptions queryOptions) {
        return dependencySearchTree.query(classType, queryOptions);
    }

    /**
     * Acts like a facade to {@link DependencySearchTree#query(Class, QueryOptions)}.
     *
     * Given a {@code classType}, where its {@code QueryOptions} is equivalent to
     * {@link QueryOptions#none()}, this function should return a correspondent
     * singleton instance of the specified class type.
     * <p>
     * When {@link QueryOptions#retrieveAnyways()} is true, the function will bring
     * the most non-null value, which means that if you specify a filter that searches
     * by the name of the Managed instance, and the name was not found, the query
     * function still returns some instance if it exists in the tree hierarchy.
     *
     * @param classType The class type to be searched in the tree.
     *
     * @return A child or itself instance of the class type passed in {code classType}.
     *         If no correspondent was found, the function will return null.
     *
     * @since 1.0
     *
     **/
    public <T> T query(Class<? extends T> classType) {
        return query(classType, QueryOptions.none());
    }

    /**
     *
     * The {@link DependencySearchTree} used instance.
     *
     * @since 1.0
     *
     **/
    public DependencySearchTree getDependencySearchTree() {
        return dependencySearchTree;
    }

    /**
     *
     * A {@link Set} of all {@link DependencyManager} strategies.
     *
     * @since 1.0
     *
     **/
    public Set<String> getStrategies() {
        return strategies;
    }
}
