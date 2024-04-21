package io.github.dependency4j;

import io.github.dependency4j.exception.ClassCreationFailedException;
import io.github.dependency4j.exception.InstallationFailedException;
import io.github.dependency4j.exception.MemberInjectionFailedException;
import io.github.dependency4j.node.SingletonNode;
import io.github.dependency4j.util.Checks;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * <b>DependencyManager</b> is the main core manager class. This class
 * has a {@link DependencySearchTree}, it handles the insertion by scanning
 * the classes from an arbitrary package path. This class is responsible for
 * finding and instantiating classes annotated with {@link Managed} annotation.
 * The class propagates the dependencies through the instantiation of an object.
 * It supports three types of dependency injection:
 *
 *  1. Constructor injection;
 *  2. Method invocation injection;
 *  3. Field injection;
 *
 * @author daviddev16
 * @version 1.0
 *
 **/
@Managed(dynamic = true)
public final class DependencyManager {

    private final DependencySearchTree dependencySearchTree;
    private final Set<String> strategySet;

    public static DependencyManagerChainBuilder builder() {
        return new DependencyManagerChainBuilder();
    }

    public DependencyManager() {
        dependencySearchTree = new DependencySearchTree();
        strategySet          = new HashSet<>();
    }

    public Object installSingleInstance(Object object, boolean standalone) {
        Checks.nonNull(object, "object must not be null.");
        performMethodAndFieldInjection(object);
        if (!standalone) {
            final Class<?> objClassType = object.getClass();
            dependencySearchTree.insert(objClassType);
            dependencySearchTree.propagateSingletonInstanceToNodes(objClassType, object);
        }
        return object;
    }

    public Object installSingleInstance(Object object) {
        return installSingleInstance(object, false);
    }

    public <T> T installType(Class<T> classType) {
        Checks.nonNull(classType, "classType must not be null.");
        return instantiateWithInjection(classType);
    }

    public <T> T query(Class<? extends T> classType, QueryOptions queryOptions) {
        return dependencySearchTree.query(classType, queryOptions);
    }
    
    public <T> T query(Class<? extends T> classType) {
        return query(classType, QueryOptions.none());
    }

    public void install(String packageName) {
        try {
            Checks.nonNull(packageName, "packageName must not be null.");

            Set<Class<?>> managedClassSet = ClassFinder.scanPackage(packageName)
                    .stream()
                    .filter(classType -> classType.isAnnotationPresent(Managed.class))
                    .filter(this::validateClassStrategyFilter)
                    .collect(Collectors.toSet());

            managedClassSet.forEach(dependencySearchTree::insert);

            managedClassSet.stream()
                    .filter(classType -> !classType.isInterface())
                    .forEach(this::instantiateWithInjection);

        } catch (Exception exception) {
            throw new InstallationFailedException(packageName, exception);
        }
    }

    @SuppressWarnings("Unchecked")
    private <T> T instantiateWithInjection(Class<T> classType) {
        try {

            SingletonNode classTypeSingletonNode = dependencySearchTree
                    .querySingletonNode(classType, QueryOptions.none());

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

            performFieldInjection(newInstanceOfType);
            performSetterMethodInvocationInjection(newInstanceOfType);

            dependencySearchTree.propagateSingletonInstanceToNodes(classType, newInstanceOfType);

            return (T) newInstanceOfType;

        } catch (InvocationTargetException | IllegalAccessException | InstantiationException exception) {
            throw new ClassCreationFailedException(classType, exception);
        }
    }

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
     * required on the current injection type.
     *

     *
     *
     * @param parentClassType the parent instantiated object class type.
     *
     * @param subjectClassType The class type relative to the member that is being injected.
     *                         To a field injection, this is equals to {@link Field#getType()},
     *                         and to a method invoke injection, this is equals to the current
     *                         parameter type that is being fetched and injected, equals to
     *                         {@link Parameter#getType()}.
     *
     * @param accessibleObject The member that is being injected. The value can be a {@link Method},
     *                         {@link Field} or {@link Constructor} reference, it depends on the type
     *                         of current injection that invokes this method.
     *
     * @return assignable object for {@code subjectClassType}. Returns null if no {@link SingletonNode}
     *         is found on the {@link DependencySearchTree}.
     *
     * @since 1.0
     * */
    private Object fetchOrCreateObjectFromClassType(Class<?> parentClassType, Class<?> subjectClassType,
                                                    AccessibleObject accessibleObject) {

        Pull pullAnnotation = accessibleObject.getAnnotation(Pull.class);

        QueryOptions queryOptions = AnnotationTransformer
                .transformPullAnnotationToQueryOptions(pullAnnotation);

        SingletonNode singletonNode = dependencySearchTree
                .querySingletonNode(subjectClassType, queryOptions);

        /* singleton nodes are null when the class type was not found in the scan section. */
        if (singletonNode == null)
            return null;

        if (parentClassType.equals(singletonNode.getNodeClassType()))
            throw new IllegalStateException("\"" + parentClassType.getSimpleName() +
                    "\" loops itself on member: \"" + accessibleObject + "\".");


        Object instanceValue = singletonNode.getNodeInstance();

        return (instanceValue != null) ? instanceValue :
                instantiateWithInjection(singletonNode.getNodeClassType());
    }

    @SuppressWarnings("Unchecked")
    private Object createInstanceWithEmptyConstructorFromClassType(Class<?> classType)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {

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
    }

    private void performFieldInjection(Object object) throws MemberInjectionFailedException {

        Class<?> parentClassType = object.getClass();

        for (Field field : parentClassType.getDeclaredFields()) {

            if (!field.isAnnotationPresent(Pull.class))
                continue;

            Class<?> fieldClassType = field.getType();

            Object objectFromClassType =
                    fetchOrCreateObjectFromClassType(parentClassType, fieldClassType, field);

            if (!field.canAccess(object))
                field.setAccessible(true);

            try {
                field.set(object, objectFromClassType);
            } catch (IllegalAccessException cause) {
                throw new MemberInjectionFailedException(field, parentClassType, cause);
            }
        }
    }

    private void performSetterMethodInvocationInjection(Object object) throws MemberInjectionFailedException {

        Class<?> parentClassType = object.getClass();

        for (Method method : parentClassType.getDeclaredMethods()) {

            if (!method.isAnnotationPresent(Pull.class) || !method.getName().startsWith("set"))
                continue;

            Parameter[] parameters = method.getParameters();

            List<Object> instantiatedValues =
                    createObjectsFromParameters(parentClassType, method, parameters);

            try {
                method.invoke(object, instantiatedValues.toArray());
            } catch (IllegalAccessException | InvocationTargetException cause) {
                throw new MemberInjectionFailedException(method, parentClassType, cause);
            }
        }
    }

    private boolean validateClassStrategyFilter(Class<?> classType) {

        Managed managedAnnotation = classType.getAnnotation(Managed.class);
        boolean flagInstanceAnyways = !managedAnnotation.disposable();

        if (managedAnnotation.dynamic())
            return false;

        /*
         * if no strategy was defined, all managed classes
         * should be instantiated.
         */
        if (strategySet.isEmpty())
            return true;

        Strategy strategyAnn = managedAnnotation.strategy();

        for (String classStrategyName : strategyAnn.value()) {
            for (String strategyName : strategySet) {
                if (classStrategyName.equals(strategyName))
                    return true;
            }
        }

        return flagInstanceAnyways;
    }

    private Constructor<?> getConstructorAnnotatedWithPull(Class<?> classType) {
        return Stream.of(classType.getConstructors())
                .filter(constructor
                        -> constructor.isAnnotationPresent(Pull.class))
                .findFirst()
                .orElse(null);
    }

    public void addStrategy(String... strategies) {
        Arrays.stream(strategies)
                .map(strategyName -> Checks.nonNullOrBlank(strategyName, "The strategy name must not be null."))
                .forEach(strategySet::add);
    }

    private void performMethodAndFieldInjection(Object object) {
        performFieldInjection(object);
        performSetterMethodInvocationInjection(object);
    }

    public DependencySearchTree getDependencySearchTree() {
        return dependencySearchTree;
    }

    public Set<String> getStrategies() {
        return strategySet;
    }
}
