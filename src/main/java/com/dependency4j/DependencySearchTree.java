package com.dependency4j;

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

    /**
     *
     * This function inserts the dependency object into the tree structure, ensuring compatibility
     * by verifying if the object class is annotated with {@link Managed}.
     *
     * @param dependencyObject The managed bean created to be used in dependency injection,
     *                         to be inserted to the dependency search tree.
     *
     * @throws NullPointerException if the dependencyObject passed is null or the class type
     *                              is not annotated with {@link Managed}.
     *
     * @since 1.0
     *
     **/
    public void insert(final Object dependencyObject) {

        Checks.nonNull(dependencyObject,"Could not insert a null dependency " +
                "object in the Dependency Search Tree.");

        final Class<?> dependencyClassType = dependencyObject.getClass();
        final Managed managedAnnotation = dependencyClassType.getAnnotation(Managed.class);

        Checks.nonNull(managedAnnotation, String.format("\"%s\" should be annotated with \"%s\".",
                dependencyClassType.getName(), Managed.class.getName()));

        createTypeFamiliesInSearchTree(dependencyClassType, managedAnnotation, dependencyObject);

    }

    /**
     *
     * This function is responsible for mapping recursively each interface implemented
     * inside the class and its superclasses. It first maps all interfaces class type
     * to a map where the key is the interface and the value is a set of "sub" interfaces.
     * {@link DependencySearchTree#appendClassTypesInSearchTree} will be resposible for
     * taking this Sets of intefaces types and add to the search tree.
     * <p>
     * If we have an interface A which extends B, if A is implemeneted by a {@link Managed}
     * class, the map will look like a key equals to "{@code Class<A>}" and value
     * "{@code Set<Class<?>>}" that contains the B interface. It is ordered by the very
     * first interface parent.
     *
     * @param dependencyObject    The managed bean created to be used in dependency injection,
     *                            the inserted object in the search tree.
     * @param dependencyClassType The {@link Class} object of {@code dependencyObject}.
     * @param managedAnnotation   The {@link Managed} annotation of the dependency bean,
     *                            which is {@code dependencyObject}.
     *
     * @see DependencySearchTree#appendClassTypesInSearchTree(Map, Managed, Object)
     * @see DependencySearchTree#createInterfaceTreeMapping(Class) 
     * @see DependencySearchTree#createSuperclassTreeMapping(Class)
     *
     * @since 1.0
     *
     **/
    private void createTypeFamiliesInSearchTree(Class<?> dependencyClassType, Managed managedAnnotation,
                                                Object dependencyObject) {

        Map<Class<?>, Set<Class<?>>> interfacesTreeMapping = createInterfaceTreeMapping(dependencyClassType);

        /* it should add the dependency class to the root node if no interface is implemented */
        if (interfacesTreeMapping.isEmpty())
            rootNode.addChildNode(new SingletonNode(dependencyObject, managedAnnotation.name()));

        appendClassTypesInSearchTree(interfacesTreeMapping, managedAnnotation, dependencyObject);

        final Map<Class<?>, Set<Class<?>>> superclassTreeMapping
                = createSuperclassTreeMapping(dependencyClassType);

        appendClassTypesInSearchTree(superclassTreeMapping, managedAnnotation, dependencyObject);

    }

    /**
     *
     * This function takes the pre-computed hierachy map of an object and inserts the hierachy
     * to a tree structure. This is fundamental to the {@link DependencySearchTree#query(Class, QueryOptions)}
     * be able to query through the generic interfaces and find the final implementation
     * of that interface or abstract class. It uses {@link DependencySearchTree#appendToSearchTree} to it.
     *
     * @param classTypesMapping The {@link Class} object of {@code dependencyObject}.
     * @param managedAnnotation The {@link Managed} annotation of the dependency bean,
     *                          which is {@code dependencyObject}.
     * @param dependencyObject  The managed bean created to be used in dependency injection,
     *                          the inserted object in the search tree.
     *
     * @see DependencySearchTree#appendToSearchTree(Set, Managed, Object)
     *
     * @since 1.0
     *
     **/
    private void appendClassTypesInSearchTree(Map<Class<?>, Set<Class<?>>> classTypesMapping,
                                              Managed managedAnnotation, Object dependencyObject) {

        for (Map.Entry<Class<?>, Set<Class<?>>> classEntries : classTypesMapping.entrySet())
            appendToSearchTree(classEntries.getValue(), managedAnnotation, dependencyObject);

    }

    /**
     *
     * This function maps all interfaces to an ordered set of interface families.
     * Let's say we have the following scenario:
     * <pre>
     *
     *                                   X
     *                                  / \
     *                                 A   B
     *                                /
     *                               C
     * <pre>
     * <i>where.: X, A, B and C are interfaces</i>
     * <br>
     * And our dependency object implements the "C" interface, then we will have
     * a map entry exactly like this:
     *
     *      Key   = Class<C>
     *      Value = Set<Class> which the elements are [ X, A, C ]
     *
     * The {@link Map<Class,Set>} return is going to be used to insert the hierachy
     * data to the dependency search tree.
     *
     * @param dependencyClassType The {@link Class} type object of {@code dependencyObject}.
     *
     * @since 1.0
     *
     **/
    private Map<Class<?>, Set<Class<?>>> createInterfaceTreeMapping(Class<?> dependencyClassType) {

        Map<Class<?>, Set<Class<?>>> interfaceTreeMapping = new LinkedHashMap<>();

        for (Class<?> interfaceClassType : dependencyClassType.getInterfaces()) {

            Set<Class<?>> subInterfacesSet = new LinkedHashSet<>();
            generateSubInterfacePathToSet(interfaceClassType, subInterfacesSet);
            subInterfacesSet.add(interfaceClassType);

            interfaceTreeMapping.put(interfaceClassType, subInterfacesSet);
        }

        return interfaceTreeMapping;
    }

    /**
     *
     * This function is used by {@link DependencySearchTree#createInterfaceTreeMapping(Class)} to
     * recursively go through all extended interfaces of the interface passed in {@code interfaceClassType}.
     * The function will run and insert in a post-order.
     *
     * @param interfaceClassType The {@link Class} type object of the iterated interface.
     *
     * @see DependencySearchTree#createInterfaceTreeMapping(Class)
     *
     * @since 1.0
     *
     **/
    private void generateSubInterfacePathToSet(Class<?> interfaceClassType, Set<Class<?>> subInterfacesSet) {

        for (Class<?> childInterfaceClassType : interfaceClassType.getInterfaces()) {
            generateSubInterfacePathToSet(childInterfaceClassType, subInterfacesSet);
            subInterfacesSet.add(childInterfaceClassType);
        }

    }

    /**
     *
     * This function acts the same as {@link DependencySearchTree#createInterfaceTreeMapping(Class)}
     * but instead of processing the hierarchy of all interfaces, the function will process
     * the hierarchy of the dependency superclass. The {@link Map<Class,Set>} return is
     * going to be used to insert the hierarchy data to the dependency search tree.
     *
     *
     * @param dependencyClassType The {@link Class} type object of {@code dependencyObject}.
     *
     * @see DependencySearchTree#createInterfaceTreeMapping(Class) 
     *
     * @since 1.0
     *
     **/
    private Map<Class<?>, Set<Class<?>>> createSuperclassTreeMapping(Class<?> dependencyClassType) {

        Map<Class<?>, Set<Class<?>>> superclassTreeMapping = new LinkedHashMap<>();

        Set<Class<?>> subSuperclassesSet = new LinkedHashSet<>();
        generateSuperclassPathToSet(dependencyClassType, subSuperclassesSet);

        superclassTreeMapping.put(dependencyClassType, subSuperclassesSet);

        return superclassTreeMapping;
    }


    /**
     *
     * This function is used by {@link DependencySearchTree#createSuperclassTreeMapping(Class)} to
     * recursively go through all extended superclasses of the class passed in
     * {@code dependencySuperclassClassType}. The function will run and insert to the set
     * in a post-order.
     *
     * @param dependencySuperclassClassType The {@link Class} type object of the dependency
     *                                      superclass
     *
     * @see DependencySearchTree#createInterfaceTreeMapping(Class)
     *
     * @since 1.0
     *
     **/
    private void generateSuperclassPathToSet(Class<?> dependencySuperclassClassType, Set<Class<?>> subSuperclassesSet) {

        Class<?> superclassClassType = dependencySuperclassClassType.getSuperclass();

        /*
         * If superclassClassType is equals to Object.class, it means it is
         * the root of all java objects, and no longer need to go recursively.
        */
        if (!superclassClassType.equals(Object.class)) {
            generateSuperclassPathToSet(superclassClassType, subSuperclassesSet);
            subSuperclassesSet.add(superclassClassType);
        }

    }

    /**
     *
     * Given an ordered set of classes types, this function will insert recursively
     * the class type of dependency object superclass and its interfaces {@link JavaTypeNode}.
     * The last element inserted it is the {@link SingletonNode} of {@code dependencyObject}.
     *
     * @param orderedClassType  The {@link Class} object of {@code dependencyObject}.
     * @param managedAnnotation The {@link Managed} annotation of the dependency bean,
     *                          which is {@code dependencyObject}.
     * @param dependencyObject  The managed bean created to be used in dependency injection,
     *                          the inserted object in the search tree.
     *
     * @see DependencySearchTree#createInterfaceTreeMapping(Class)
     *
     * @since 1.0
     *
     **/
    private void appendToSearchTree(Set<Class<?>> orderedClassType, Managed managedAnnotation,
                                    Object dependencyObject) {

        AbstractNode parentNode = rootNode;

        for (Class<?> classType : orderedClassType)
            parentNode = findOrCreateJavaTypeNode(classType, parentNode);

        /* adds the dependency object to the end of the tree */
        String classNameAlias = coallesceClassName(managedAnnotation, dependencyObject);
        parentNode.addChildNode(new SingletonNode(dependencyObject, classNameAlias));

    }

    /**
     *
     * This function is used internally by {@link DependencySearchTree#appendToSearchTree(Set, Managed, Object)}
     * to find the {@link JavaTypeNode} relative to {@code parentNode} that has the same
     * class type as the parameter {@code classType}. Returns null if no {@link JavaTypeNode}
     * correspondent node exists in the tree.
     *
     * @param classType  The {@link Class} type used to find the {@link JavaTypeNode} in the tree.
     * @param parentNode The scanned node used to find the correspondent {@link JavaTypeNode}.
     *
     * @see DependencySearchTree#findOrCreateJavaTypeNode(Class, AbstractNode)
     * @see DependencySearchTree#appendToSearchTree(Set, Managed, Object)
     *
     * @since 1.0
     *
     **/
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

    /**
     *
     * This function is used internally by {@link DependencySearchTree#appendToSearchTree(Set, Managed, Object)}
     * to find the {@link JavaTypeNode} relative to {@code parentNode} that has the same
     * class type as the parameter {@code classType}. If there is no correspondent
     * {@link JavaTypeNode}, a new node is created as a child of {@code parentNode}.
     * The function should never return a null value.
     *
     * @param classType  The {@link Class} type used to find the {@link JavaTypeNode} in the tree.
     * @param parentNode The scanned node used to find the correspondent {@link JavaTypeNode}.
     *
     * @see DependencySearchTree#findJavaTypeNode(Class, AbstractNode)
     * @see DependencySearchTree#appendToSearchTree(Set, Managed, Object)
     *
     * @since 1.0
     *
     **/
    private JavaTypeNode findOrCreateJavaTypeNode(Class<?> classType, final AbstractNode parentNode) {
        JavaTypeNode javaTypeNode = findJavaTypeNode(classType, parentNode);
        if (javaTypeNode == null) {
            javaTypeNode = new JavaTypeNode(classType);
            parentNode.addChildNode(javaTypeNode);
        }
        return javaTypeNode;
    }

    /**
     *
     * Given a {@code classType} and {@code queryOptions}, this function should
     * return a correspondent singleton instance to the specified class type.
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
    @SuppressWarnings({"Unchecked"})
    public <T> T query(Class<? extends T> classType, QueryOptions queryOptions) {

        SingletonNode singletonNode = querySingletonNode(classType, queryOptions);
        assert singletonNode != null;
        return (T) singletonNode.getNodeSingletonInstance();

    }

    /**
     *
     * This function is used to search internally through the tree hierarchy
     * based on criteria specified by the use on {@link DependencySearchTree#query(Class, QueryOptions)}.
     * It is the core implementation of the exposed query method.
     * 
     * @param classType    The class type to be searched in the tree.
     * @param queryOptions Optional configuration to the search.
     *
     * @return A child or itself instance of the class type passed in {code classType}.
     *         If no correspondent was found, the function will return null.
     *         
     * @see DependencySearchTree#query(Class, QueryOptions)
     *
     * @since 1.0
     *
     **/
    private SingletonNode querySingletonNode(Class<?> classType, QueryOptions queryOptions) {

        Set<SingletonNode> matchResultSet = querySingletonsByType(classType);
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

    /**
     *
     * This function will return a Set of SingletonNode that its instances are compatible
     * to the class type passed in {@code classType}.
     *
     * @param classType    The class type to be searched in the tree.
     *
     * @return A non-null {@link Set} of SingletonNode.
     *
     * @since 1.0
     *
     **/
    private Set<SingletonNode> querySingletonsByType(Class<?> classType) {

        final Set<SingletonNode> matchResultSet = new HashSet<>();
        queryRecursivelySingletons(classType, rootNode, matchResultSet);

        return matchResultSet;

    }

    /**
     *
     * This function is used to query recursively through the tree hierarchy to
     * collect all {@link SingletonNode}'s that is compatible to {@code classType}.
     *
     * @param classType      The class type to be searched in the tree.
     * @param node           The parent or root node to be searched.
     * @param matchResultSet The pre instantiated {@link Set} to be used as the list
     *                       of the collected {@link SingletonNode}
     *
     * @return A non-null {@link Set} of SingletonNode.
     *
     * @since 1.0
     *
     **/
    private void queryRecursivelySingletons(Class<?> classType, AbstractNode node, Set<SingletonNode> matchResultSet) {

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

    /**
     *
     * Used to retrieve a valid non-null name for the dependency object SingletonNode.
     *
     * @param managedAnnotation The {@link Managed} annotation of the dependency bean,
     *                          which is {@code dependencyObject}.
     * @param dependencyObject  The managed bean created to be used in dependency injection,
     *                          the inserted object in the search tree.
     *
     * @since 1.0
     *
     **/
    private String coallesceClassName(Managed managedAnnotation, Object dependencyObject) {
        return !StrUtil.isNullOrBlank(managedAnnotation.name()) ?
                managedAnnotation.name() : dependencyObject.getClass().getSimpleName();
    }


    /**
     *
     * The root node of the search tree. It can be casted to {@link RootNode}.
     *
     * @return The root node as a {@link AbstractNode}.
     *
     * @since 1.0
     *
     **/
    public AbstractNode getRootNode() {
        return rootNode;
    }
}

