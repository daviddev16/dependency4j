package io.github.dependency4j;

import io.github.dependency4j.exception.ClassCreationFailedException;
import io.github.dependency4j.exception.InstallationFailedException;
import io.github.dependency4j.exception.MemberInjectionFailedException;
import io.github.dependency4j.exception.ReflectionStateException;
import io.github.dependency4j.node.SingletonNode;
import io.github.dependency4j.node.VirtualSingletonNode;
import io.github.dependency4j.util.Checks;
import io.github.dependency4j.util.ReflectionUtil;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.dependency4j.util.StrUtil.*;
import static java.lang.String.format;

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
public final class DependencyManager implements QueryableProxy {

    public static final String ANNOTATED_CONSTRUCTOR = "ANNOTATED";
    public static final String DEFAULT_CONSTRUCTOR   = "EMPTY/DEFAULT";

    private final DependencySearchTree dependencySearchTree;
    private final Set<String> strategies;

    private boolean enablePrimitiveDefaultValue = false;

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
                        .filter(this::checkClassEligibility)
                    .collect(Collectors.toSet());

            managedClassSet.forEach(dependencySearchTree::insert);
            managedClassSet.forEach(this::instantiateWithInjection);

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

        if (installationType != InstallationType.STANDALONE) {
            dependencySearchTree.insertPropagation(instance.getClass(), instance);
            installVirtualMethodsToSingleInstance(instance);
        }

        return instance;
    }

    /**
     *
     * Handles installation of virtual methods to a user's single instance. If the instance
     * is installed as {@link InstallationType#STANDALONE}, virtual methods will not be used
     * in this case. Standalone instances are not stored in the {@link DependencySearchTree}
     * so its dependencies will not be inserted to the tree too.
     *
     * @param instance The instance object that will receive the dependency injection.
     *
     * @see DependencyManager#installInstance(Object)
     *
     * @since 1.0.4
     *
     **/
    private void installVirtualMethodsToSingleInstance(Object instance) {

        Class<?> parentClassType = instance.getClass();

        ReflectionUtil.consumeAllVirtualMethodsFromClassType(parentClassType, (virtualMethod) ->
        {
            Class<?> virtualMethodReturnType = virtualMethod.getReturnType();

            if (virtualMethodReturnType.isPrimitive())
                return;

            dependencySearchTree.propagateSingletonInstanceToNodes(virtualMethodReturnType,
                    invokeMethodWithInjection(instance, virtualMethod));
        });
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
     * @param classTypeSingletonNode The singleton node to be instantiated.
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
     * @since 1.0.4
     *
     **/
    @SuppressWarnings("Unchecked")
    private <T> T instantiateWithInjection(SingletonNode classTypeSingletonNode) {

        final Class<?> nodeClassType = classTypeSingletonNode.getNodeClassType();

        if (classTypeSingletonNode.getNodeInstance() != null)
            return (T) classTypeSingletonNode.getNodeInstance();

        try {
            Object newInstanceOfType;

            if (classTypeSingletonNode instanceof VirtualSingletonNode virtualSingletonNode)
                newInstanceOfType =
                        handleVirtualSingletonClassInstantiation(virtualSingletonNode);
            else
                newInstanceOfType =
                        handleConcreteSingletonClassInstantiation(classTypeSingletonNode);

            if (newInstanceOfType == null)
                throw new IllegalStateException("Could not created a instance to " + nodeClassType);

            performMethodAndFieldInjection(newInstanceOfType);
            dependencySearchTree.propagateSingletonInstanceToNodes(nodeClassType, newInstanceOfType);

            return (T) newInstanceOfType;

        } catch (Exception exception) {
            throw new ClassCreationFailedException(nodeClassType, exception);
        }
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
     * This is a convenient function to {@link #instantiateWithInjection(SingletonNode)}.
     *
     * @param classType The class type to be searched or instantiated recursively.
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
     * @since 1.0.4
     *
     **/
    @SuppressWarnings({"Unchecked"})
    private <T> T instantiateWithInjection(Class<T> classType) {

        Managed managedAnnotation = classType.getAnnotation(Managed.class);

        if (managedAnnotation != null && managedAnnotation.dynamic())
            return (T) handleSingletonClassInstantiation(classType);

        SingletonNode classTypeSingletonNode =
                dependencySearchTree.querySingletonNode(classType, QueryOptions.none());

        if (classTypeSingletonNode == null)
            return null;
        else if (classTypeSingletonNode.getNodeInstance() != null)
            return (T) classTypeSingletonNode.getNodeInstance();

        return instantiateWithInjection(classTypeSingletonNode);
    }

    /**
     *
     * Handles instantiation of a generic class type. This function instantiates any class
     * type that is annotated with {@link Managed}. There are two types of class instantiation:
     * <pre>
     * 1. Instantiation with Empty constructor, the instantiation is made with the
     *    default java constructor if the class does not private it.
     * 2. Instantiation with constructor annotated with {@link Pull}. This type of instantiation
     *    will also propagates the constructor dependency injection.
     * </pre>
     *
     * @param dependencyClassType The singleton node to have its node instance created.
     *
     * @return An instance assignable to {@link SingletonNode#getNodeClassType()} which
     *         is the dependency class type.
     *
     * @see DependencyManager#instantiateWithInjection(SingletonNode)
     *
     * @since 1.0.4
     *
     **/
    private Object handleSingletonClassInstantiation(Class<?> dependencyClassType) {

        Constructor<?> annotatedConstructor = getConstructorAnnotatedWithPull(dependencyClassType);

        if (annotatedConstructor != null)
            return createInstanceWithAnnotatedConstructor(annotatedConstructor);

        return createInstanceWithEmptyConstructor(dependencyClassType);
    }

    /**
     *
     * Handles instantiation of a concrete SingletonNode. A class type to be concrete
     * needs to be scanned with {@link #installPackage(String)}, installed by
     * {@link #installType(Class)} or {@link #installInstance(Object)}. This function
     * is able to create an instance of a given {@link SingletonNode} and should be
     * used only by {@link #instantiateWithInjection(SingletonNode)} to created new
     * objects.
     * <p>
     * This is a convenient function used to handle concrete managed classes using
     * {@link #instantiateWithInjection(SingletonNode)}
     *
     * @param singletonNode The singleton node to have its node instance created.
     *
     * @return An instance assignable to {@link SingletonNode#getNodeClassType()} which
     *         is the dependency class type.
     *
     * @see DependencyManager#instantiateWithInjection(SingletonNode)
     *
     * @since 1.0.4
     *
     **/
    private Object handleConcreteSingletonClassInstantiation(SingletonNode singletonNode) {
        return handleSingletonClassInstantiation(singletonNode.getNodeClassType());
    }

    /**
     *
     * Handles instantiation of a virtual SingletonNode. A class type to be virtual
     * needs to have a parent {@link SingletonNode}. Virtual singleton nodes are
     * singletons detected when using the {@link Virtual} annotation in method.
     * The annotation indicates that the method is virtual and its results should be
     * treated as a Singleton instance. A virtual singleton node is handled by the
     * node class {@link VirtualSingletonNode}.
     *
     * @param virtualSingletonNode The singleton node to have its node instance created.
     *
     * @return An instance assignable to {@link VirtualSingletonNode#getNodeClassType()}
     *         which is the dependency class type.
     *
     * @see DependencyManager#instantiateWithInjection(SingletonNode)
     *
     * @since 1.0.4
     *
     **/
    private Object handleVirtualSingletonClassInstantiation(VirtualSingletonNode virtualSingletonNode) {

        SingletonNode parentSingletonNode = virtualSingletonNode.getParentSingletionNode();

        Object virtualizedObject = parentSingletonNode.getNodeInstance();

        if (virtualizedObject == null)
            virtualizedObject = instantiateWithInjection(parentSingletonNode.getNodeClassType());

        Checks.nonNull(virtualizedObject, "virtualized object failed to create.");

        Method virtualizedMethod = virtualSingletonNode.getVirtualMethod();

        return invokeMethodWithInjection(virtualizedObject, virtualizedMethod);

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
                .map(parameter ->
                    fetchOrCreateObjectFromClassType(
                            parentClassType,
                            parameter.getType(),
                            accessibleObject))
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

        if (subjectClassType.isPrimitive())
            if (enablePrimitiveDefaultValue)
                return ReflectionUtil.defaultValueToClassType(subjectClassType);
            else
                return null;

        Pull pullAnnotation = accessibleObject.getAnnotation(Pull.class);

        final QueryOptions optionalQueryOptions = (pullAnnotation != null)
                ? AnnotationTransformer.transformPullAnnotationToQueryOptions(pullAnnotation)
                : QueryOptions.none();

        SingletonNode singletonNode = dependencySearchTree
                .querySingletonNode(subjectClassType, optionalQueryOptions);

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
     * Creates an instance of {@code classType} using its {@link Pull} annotated constructor.
     * This function is used by {@link #instantiateWithInjection(Class)} when a constructor
     * annotated with {@link Pull} was found.
     *
     * @param annotatedConstructor The {@link Pull} annotated java constructor.
     *
     * @return An instance of {@code classType}.
     *
     * @throws ReflectionStateException May occur during instantiation of an empty constructor
     *                                  process. It will give a cause exception.
     *
     * @since 1.0
     *
     * */
    private Object createInstanceWithAnnotatedConstructor(Constructor<?> annotatedConstructor) {

        final Class<?> classType = annotatedConstructor.getDeclaringClass();

        try {
            List<Object> listOfCreatedObjects =
                    createObjectsFromParameters(classType, annotatedConstructor,
                            annotatedConstructor.getParameters());

            return annotatedConstructor.newInstance(listOfCreatedObjects.toArray());
        }
        catch (Exception exception) {
            handleConstructorInstantiationException(ANNOTATED_CONSTRUCTOR, classType, exception);
        }

        return null;
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
     **/
    @SuppressWarnings("Unchecked")
    private Object createInstanceWithEmptyConstructor(Class<?> classType) {
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
        } catch (Exception exception) {
            handleConstructorInstantiationException(DEFAULT_CONSTRUCTOR, classType, exception);
        }

        return null;
    }

    /**
     *
     * Handles a thrown exception from a creation of instance by {@link Constructor}. This
     * function is used by {@link #createInstanceWithAnnotatedConstructor(Constructor)} and
     * {@link #createInstanceWithEmptyConstructor(Class)} to handle generically reflexive
     * excetion such as {@link InstantiationException}.
     *
     * @param constructorType {@link DependencyManager#ANNOTATED_CONSTRUCTOR} or
     *                        {@link DependencyManager#DEFAULT_CONSTRUCTOR}
     *
     * @param classType The {@link Class} to being instantiated.
     *
     * @param exception The thrown reflexive exception.
     *
     * @throws ReflectionStateException This exception is used to handle all reflexive exceptions
     *                                  caused by an error while instantiating a given class type.
     *
     * @since 1.0
     *
     * */
    private void handleConstructorInstantiationException(String constructorType,
                                                         Class<?> classType, Exception exception) {

        String exceptionMessage;
        if (exception instanceof InvocationTargetException)
            exceptionMessage = format("The constructor of \"%s\" threw a error.", classType.getName());

        else if (exception instanceof IllegalAccessException)
            exceptionMessage = format("The constructor of \"%s\" is inaccessible.", classType.getName());
        else
            exceptionMessage = format("An error occurred while instantiating the " +
                    "constructor of \"%s\".", classType.getName());

        exceptionMessage = format("[Constructor type: %s] %s", constructorType, exceptionMessage);

        throw new ReflectionStateException(exceptionMessage);
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

        final Class<?> parentClassType = instance.getClass();

        for (Method method : parentClassType.getDeclaredMethods()) {

            if (!method.isAnnotationPresent(Pull.class) || !method.getName().startsWith("set"))
                continue;

            invokeMethodWithInjection(instance, method);
        }
    }

    /**
     *
     * Performs method invocation injection in a {@code instance}. This function is used with
     * {@link #performSetterMethodInvocationInjection(Object)} and
     * {@link #handleVirtualSingletonClassInstantiation(VirtualSingletonNode)} to perform method
     * It uses {@link #createObjectsFromParameters(Class, AccessibleObject, Parameter[])} to get and
     * set the assignable instances values.
     *
     * @param instance A receiver object to method injection.
     *
     * @throws MemberInjectionFailedException May occur during method injection if any
     *                                        reflexive exception is thrown. It will
     *                                        give a cause exception.
     *
     * @since 1.0.4
     *
     **/
    private Object invokeMethodWithInjection(Object instance, Method method) {

        final Class<?> parentClassType = instance.getClass();

        try {
            Parameter[] parameters = method.getParameters();

            List<Object> parameterValues =
                    createObjectsFromParameters(parentClassType, method, parameters);

            return method.invoke(instance, parameterValues.toArray());

        } catch (IllegalAccessException | InvocationTargetException cause) {
            throw new MemberInjectionFailedException(method, parentClassType, cause);
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
     * The eligibility options are:
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
     * @return {@code true} if the class type match with the eligibility options.
     *
     * @see Managed#dynamic()
     * @see Managed#disposable()
     * @see Managed#strategy()
     *
     * @since 1.0
     *
     **/
    private boolean checkClassEligibility(Class<?> classType) {

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
     * Adds strategies to the strategy set of {@link DependencyManager}. Strategies will
     * eventually be used to define which class should be instantiated and handled by the
     * dependency search tree. This function should be called before {@link #installPackage(String)}
     * to apply the strategy validation.
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
     *
     * Enables variables with a primitive type to be initialized with its default value.
     * This is useful when using methods where its parameters contain a primitive type.
     * When this flag is {@link true}, the injected property will not receive a null value.
     *
     * @since 1.0.4
     *
     **/
    public void enablePrimitiveDefaultValue() {
        this.enablePrimitiveDefaultValue = true;
    }

    /**
     *
     * {@inheritDoc}
     *
     */
    @Override
    public <T> T query(Class<? extends T> classType, QueryOptions queryOptions) {
        return dependencySearchTree.query(classType, queryOptions);
    }

    /**
     *
     * {@inheritDoc}
     *
     */
    @Override
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
