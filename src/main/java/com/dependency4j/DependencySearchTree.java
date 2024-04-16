package com.dependency4j;

import com.dependency4j.node.JavaTypeNode;
import com.dependency4j.node.RootNode;
import com.dependency4j.node.SingletonNode;
import com.dependency4j.util.Checks;
import com.dependency4j.util.StrUtil;

import java.util.*;

/**
 *
 * <b>DependencySearchTree</b> is a specific Tree structure responsible for
 * storing data of java class and interface hierarchy. The tree is used
 * for searching and matching class types to their respective singleton
 * instances.
 *
 * @author David Duarte Pinheiro
 * @version 1.0
 *
 **/
public final class DependencySearchTree {

    private final RootNode rootNode;

    public DependencySearchTree() {
        rootNode = new RootNode();
    }

    public void insert(final Class<?> dependencyClassType) {

        Checks.nonNull(dependencyClassType, "Could not insert a null dependency " +
                "object in the Dependency Search Tree.");

        final Managed managedAnnotation = dependencyClassType.getAnnotation(Managed.class);

        Checks.nonNull(managedAnnotation, String.format("\"%s\" should be annotated with \"%s\".",
                dependencyClassType.getName(), Managed.class.getName()));

        createTypeFamiliesInSearchTree(dependencyClassType, managedAnnotation);
    }

    private void createTypeFamiliesInSearchTree(Class<?> dependencyClassType, Managed managedAnnotation) {

        Map<Class<?>, Set<Class<?>>> interfacesTreeMapping = createInterfaceTreeMapping(dependencyClassType);

        /* it should add the dependency class to the root node if no interface is implemented */
        if (interfacesTreeMapping.isEmpty())
            rootNode.addChildNode(new SingletonNode(dependencyClassType, managedAnnotation.name()));

        appendClassTypesInSearchTree(interfacesTreeMapping, managedAnnotation, dependencyClassType);

        final Map<Class<?>, Set<Class<?>>> superclassTreeMapping
                = createSuperclassTreeMapping(dependencyClassType);

        appendClassTypesInSearchTree(superclassTreeMapping, managedAnnotation, dependencyClassType);
    }

    private void appendClassTypesInSearchTree(Map<Class<?>, Set<Class<?>>> classTypesMapping,
                                              Managed managedAnnotation, Class<?> dependencyClassType) {

        for (Map.Entry<Class<?>, Set<Class<?>>> classEntries : classTypesMapping.entrySet())
            appendToSearchTree(classEntries.getValue(), dependencyClassType, managedAnnotation);
    }

    private Map<Class<?>, Set<Class<?>>> createInterfaceTreeMapping(Class<?> dependencyClassType) {

        Map<Class<?>, Set<Class<?>>> interfaceTreeMapping = new LinkedHashMap<>();

        for (Class<?> interfaceClassType : dependencyClassType.getInterfaces()) {

            Set<Class<?>> subInterfacesSet = new LinkedHashSet<>();
            generateSubInterfacePathSet(interfaceClassType, subInterfacesSet);
            subInterfacesSet.add(interfaceClassType);

            interfaceTreeMapping.put(interfaceClassType, subInterfacesSet);
        }

        return interfaceTreeMapping;
    }

    private void generateSubInterfacePathSet(Class<?> interfaceClassType, Set<Class<?>> subInterfacesSet) {

        for (Class<?> childInterfaceClassType : interfaceClassType.getInterfaces()) {
            generateSubInterfacePathSet(childInterfaceClassType, subInterfacesSet);
            subInterfacesSet.add(childInterfaceClassType);
        }
    }

    private Map<Class<?>, Set<Class<?>>> createSuperclassTreeMapping(Class<?> dependencyClassType) {

        Map<Class<?>, Set<Class<?>>> superclassTreeMapping = new LinkedHashMap<>();

        Set<Class<?>> subSuperclassesSet = new LinkedHashSet<>();
        generateSuperclassPathSet(dependencyClassType, subSuperclassesSet);

        superclassTreeMapping.put(dependencyClassType, subSuperclassesSet);

        return superclassTreeMapping;
    }

    private void generateSuperclassPathSet(Class<?> dependencySuperclassClassType, Set<Class<?>> subSuperclassesSet) {

        Class<?> superclassClassType = dependencySuperclassClassType.getSuperclass();

        /*
         * If superclassClassType is equals to Object.class, it means it is
         * the root of all java objects, and no longer need to go recursively.
        */
        if (!superclassClassType.equals(Object.class)) {
            generateSuperclassPathSet(superclassClassType, subSuperclassesSet);
            subSuperclassesSet.add(superclassClassType);
        }
    }

    private void appendToSearchTree(Set<Class<?>> orderedClassType,
                                    Class<?> dependencyClassType, Managed managedAnnotation) {

        AbstractNode parentNode = rootNode;

        for (Class<?> classType : orderedClassType)
            parentNode = findOrCreateJavaTypeNode(classType, parentNode);

        /* adds the dependency object to the end of the tree */
        String classNameAlias = coalesceClassName(dependencyClassType, managedAnnotation);
        parentNode.addChildNode(new SingletonNode(dependencyClassType, classNameAlias));
    }

    private JavaTypeNode findJavaTypeNode(Class<?> classType, AbstractNode parentNode) {
        for (AbstractNode childNode : parentNode.children()) {
            if (childNode instanceof JavaTypeNode javaTypeNode) {
                if (classType.equals(javaTypeNode.getNodeClassType())) {
                    return javaTypeNode;
                } else {
                    JavaTypeNode foundNode = findJavaTypeNode(classType, childNode);
                    if (foundNode != null) {
                        return foundNode;
                    }
                }
            }
        }
        return null;
    }

    private JavaTypeNode findOrCreateJavaTypeNode(Class<?> classType,
                                                  final AbstractNode parentNode) {

        JavaTypeNode javaTypeNode = findJavaTypeNode(classType, parentNode);

        if (javaTypeNode == null) {
            javaTypeNode = new JavaTypeNode(classType);
            parentNode.addChildNode(javaTypeNode);
        }

        return javaTypeNode;
    }

    @SuppressWarnings({"Unchecked"})
    public <T> T query(Class<? extends T> classType, QueryOptions queryOptions) {

        SingletonNode singletonNode = querySingletonNode(classType, queryOptions);
        return (singletonNode != null) ? (T) singletonNode.getNodeSingletonInstance() : null;
    }

    public SingletonNode querySingletonNode(Class<?> classType, QueryOptions queryOptions) {

        List<SingletonNode> matchResultSet = querySingletonsByType(classType);
        Iterator<SingletonNode> singletonNodeIterator = matchResultSet.iterator();

        SingletonNode matchedSingletonNode = null;

        String filteredClassName = queryOptions.filteredClassName();

        boolean hasFilteredClassName = !StrUtil.isNullOrBlank(filteredClassName);
        boolean hasTextMatchedWithClassName = false;

        while (singletonNodeIterator.hasNext()) {

            matchedSingletonNode = singletonNodeIterator.next();

            if (!hasFilteredClassName)
                continue;

            if (matchedSingletonNode.getNodeName().equalsIgnoreCase(filteredClassName)) {
                hasTextMatchedWithClassName = true;
                break;
            }
        }

        if (hasFilteredClassName && !hasTextMatchedWithClassName && !queryOptions.retrieveAnyways())
            return null;

        return matchedSingletonNode;
    }

    public List<SingletonNode> querySingletonsByType(Class<?> classType) {

        final List<SingletonNode> matchResultSet = new ArrayList<>();
        queryRecursivelySingletons(classType, rootNode, matchResultSet);

        return matchResultSet;
    }

    private void queryRecursivelySingletons(Class<?> classType, AbstractNode node, List<SingletonNode> matchResultSet) {

        for (AbstractNode childNode : node.children()) {

            if (childNode instanceof JavaTypeNode javaTypeNode)
                queryRecursivelySingletons(classType, javaTypeNode, matchResultSet);

            else if (childNode instanceof SingletonNode singletonNode) {

                final Class<?> singletonClassType = singletonNode.getNodeSingletonClassType();

                if (classType.isAssignableFrom(singletonClassType))
                    matchResultSet.add(singletonNode);
            }
        }
    }

    public void propagateSingletonInstanceToNodes(Class<?> classType, Object nodeInstance) {
        querySingletonsByType(classType)
                .forEach(singletonNode -> singletonNode
                        .setNodeSingletonInstance(nodeInstance));
    }

    private String coalesceClassName(Class<?> dependencyClassType, Managed managedAnnotation) {
        return !StrUtil.isNullOrBlank(managedAnnotation.name()) ?
                managedAnnotation.name() : dependencyClassType.getSimpleName();
    }

    public AbstractNode getRootNode() {
        return rootNode;
    }
}

