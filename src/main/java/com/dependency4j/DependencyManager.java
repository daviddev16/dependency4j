package com.dependency4j;

import com.dependency4j.node.SingletonNode;
import com.dependency4j.util.Checks;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DependencyManager {

    private final DependencySearchTree dependencySearchTree;

    public DependencyManager() {
        this.dependencySearchTree = new DependencySearchTree();
    }

    public void install(String packageName) {
        Map<String, Long> timestamps = new HashMap<>(3);
        try {

            long startTime = System.currentTimeMillis();

            Set<Class<?>> managedClassSet = ClassFinder.scanPackage(packageName)
                    .stream()
                    .filter(classType -> classType.isAnnotationPresent(Managed.class))
                    .collect(Collectors.toSet());

            startTime = computeTimestamp(startTime, "Package scan time", timestamps);

            managedClassSet.forEach(dependencySearchTree::insert);

            startTime = computeTimestamp(startTime, "Tree insertion time", timestamps);

            managedClassSet.stream()
                    .filter(classType -> !classType.isInterface())
                    .forEach(this::instantiateWithInjection);

            computeTimestamp(startTime, "Dependency injection and instatiation time", timestamps);

            final StringBuilder stringBuilder = new StringBuilder();

            for (Map.Entry<String, Long> entries : timestamps.entrySet()) {
                stringBuilder
                        .append(" [")
                        .append(entries.getKey()).append(": ")
                        .append(entries.getValue()).append(" ms")
                        .append("] ");
            }

            System.out.println(stringBuilder);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private long computeTimestamp(long startTime, String timestampLabel, Map<String, Long> timestampMap) {

        long lastTime = System.currentTimeMillis();
        timestampMap.put(timestampLabel, lastTime - startTime);

        return lastTime;
    }

    private <T> T instantiateWithInjection(Class<T> classType) {
        try {
            Constructor<?> constructorAnnotatedWithPull = getConstructorAnnotatedWithPull(classType);
            Object newInstanceOfType;

            if(constructorAnnotatedWithPull != null) {
                List<Object> listOfValues = createObjectsFromConstructor(classType, constructorAnnotatedWithPull);
                newInstanceOfType = constructorAnnotatedWithPull.newInstance(listOfValues.toArray());
            } else {
                newInstanceOfType = createInstanceWithEmptyConstructorFromClassType(classType);
            }

            if (newInstanceOfType == null)
                throw new IllegalStateException("Could not created a instance to " + classType.getSimpleName());

            performClassFieldInjection(newInstanceOfType);
            performMethodInvokeInjection(newInstanceOfType);

            dependencySearchTree.propagateSingletonInstanceToNodes(classType, newInstanceOfType);

            return (T) newInstanceOfType;

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Object> createObjectsFromConstructor(Class<?> parentClassType, Constructor<?> constructor) {

        List<Object> instantiatedValues = new ArrayList<>();

        for (Parameter parameter : constructor.getParameters()) {

            Class<?> parameterClassType = parameter.getType();

            QueryOptions queryOptions = AnnotationTransformer
                    .transformPullAnnotationToQueryOptions(constructor.getAnnotation(Pull.class));

            SingletonNode singletonNode = dependencySearchTree
                    .querySingletonNode(parameterClassType, queryOptions);

            if (singletonNode == null)
                continue;

            Object instanceValue = singletonNode.getNodeSingletonInstance();

            if (instanceValue == null) {

                instanceValue = instantiateWithInjection(singletonNode.getNodeSingletonClassType());

                if (parentClassType.equals(singletonNode.getNodeSingletonClassType()))
                    throw new ScanFailedException("\"" + parentClassType.getSimpleName() +
                            "\" loops itself on field: \"" + constructor.getName() + "\".");

            }

            instantiatedValues.add(instanceValue);
        }

        return instantiatedValues;

    }

    private Object fetchOrCreateObjectFromClassType(Class<?> parentClassType,
                                                    Class<?> subjectClassType, AccessibleObject accessibleObject) {

        Pull pullAnnotation = accessibleObject.getAnnotation(Pull.class);

        QueryOptions queryOptions = AnnotationTransformer
                .transformPullAnnotationToQueryOptions(pullAnnotation);

        SingletonNode singletonNode = dependencySearchTree
                .querySingletonNode(subjectClassType, queryOptions);

        /* singleton nodes are null when the class type was not found in the scan section. */
        if (singletonNode == null)
            return null;

        if (parentClassType.equals(singletonNode.getNodeSingletonClassType()))
            throw new ScanFailedException("\"" + parentClassType.getSimpleName() +
                    "\" loops itself on field: \"" + accessibleObject + "\".");


        Object instanceValue = singletonNode.getNodeSingletonInstance();

        return (instanceValue != null) ? instanceValue :
                instantiateWithInjection(singletonNode.getNodeSingletonClassType());
    }

    private List<Object> createObjectsFromParameters(Class<?> parentClassType,
                                                     AccessibleObject accessibleObject, Parameter[] parameters) {
        return Stream.of(parameters)
                .map(parameter ->
                        fetchOrCreateObjectFromClassType(parentClassType, parameter.getType(), accessibleObject))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("Unchecked")
    private Object createInstanceWithEmptyConstructorFromClassType(Class<?> classType) {

        Constructor<?>[] constructors = classType.getConstructors();
        Constructor<?> emptyConstructor = null;

        for (Constructor<?> constructor : constructors)
            if (constructor.getParameterCount() == 0) {
                emptyConstructor = constructor;
                break;
            }

        if (emptyConstructor == null)
            return null;

        try {
            return emptyConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void performClassFieldInjection(Object object) {

        Class<?> classType = object.getClass();

        for (Field field : classType.getDeclaredFields()) {

            if (!field.isAnnotationPresent(Pull.class))
                continue;

            try {
                Class<?> fieldClassType = field.getType();

                Object objectFromClassType =
                        fetchOrCreateObjectFromClassType(classType, fieldClassType, field);

                if (!field.canAccess(object))
                    field.setAccessible(true);

                field.set(object, objectFromClassType);

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void performMethodInvokeInjection(Object object) {

        Class<?> classType = object.getClass();

        for (Method method : classType.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Pull.class)) {

                if (!method.getName().startsWith("set"))
                    continue;

                Parameter[] parameters = method.getParameters();

                List<Object> instantiatedValues =
                        createObjectsFromParameters(classType, method, parameters);

                try {
                    method.invoke(object, instantiatedValues.toArray());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private Constructor<?> getConstructorAnnotatedWithPull(Class<?> classType) {
        return Stream.of(classType.getConstructors())
                .filter(constructor
                        -> constructor.isAnnotationPresent(Pull.class))
                .findFirst()
                .orElse(null);
    }

    public Object installSingleInstance(Object object) {
        Checks.nonNull(object, "object must not be null.");
        performClassFieldInjection(object);
        performMethodInvokeInjection(object);
        return object;
    }

    public <T> T installSingle(Class<T> classType) {
        Checks.nonNull(classType, "classType must not be null.");
        return instantiateWithInjection(classType);
    }

    public DependencySearchTree getDependencySearchTree() {
        return dependencySearchTree;
    }

}
