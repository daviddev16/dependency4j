package io.github.dependency4j;

import io.github.dependency4j.node.JavaTypeNode;
import io.github.dependency4j.node.RootNode;
import io.github.dependency4j.node.SingletonNode;
import io.github.dependency4j.node.VirtualSingletonNode;
import io.github.dependency4j.util.Checks;
import io.github.dependency4j.util.ReflectionUtil;
import io.github.dependency4j.util.StrUtil;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * <b>DependencySearchTree</b> is a specific Tree structure responsible for storing data of
 * java class and interface hierarchy. The tree is used for searching and matching class types
 * to their respective singleton instances.
 *
 * @author daviddev16
 * @version 1.0.8
 *
 **/
public final class DependencySearchTree {

    private final RootNode rootNode;

    public DependencySearchTree() {
        rootNode = new RootNode();
    }

    /**
     *
     * This function inserts the wrapped class type of the {@link TypeInformationHolder} into
     * the tree structure, ensuring compatibility by verifying if the class type is annotated
     * with {@link Managed}.
     *
     * @param typeInformationHolder The {@link TypeInformationHolder} wraps the dependency
     *                              class type that will receive a new {@link SingletonNode}.
     *
     * @throws NullPointerException if the dependencyClassType passed is null or the class type
     *                              is not annotated with {@link Managed}.
     *
     * @since 1.0.8
     *
     **/
    public void insert(TypeInformationHolder typeInformationHolder) {
        Checks.nonNull(typeInformationHolder, "typeInformationHolder must be specified.");
        createTypeFamiliesInSearchTree(typeInformationHolder);
    }

    /**
     *
     * This function inserts the dependency class type into the tree structure and propagates
     * the {@code dependencyInstance} to the singleton nodes.
     *
     * @param dependencyClassType The managed class type that is going to be used in dependency
     *                            injection. Will be inserted to the dependency search tree.
     * @param nodeInstance The node instance to be propagated.
     *
     * @throws NullPointerException if the dependencyClassType passed is null or the class type
     *                              is not annotated with {@link Managed}.
     *
     * @since 1.0
     *
     **/
    public void insertPropagation(final Class<?> dependencyClassType, Object nodeInstance) {
        TypeInformationHolder typeInformationHolder = TypeInformationHolderFactory
                .createTypeInformation(dependencyClassType);

        insert(typeInformationHolder);
        propagateSingletonInstanceToNodes(dependencyClassType, nodeInstance);
    }

    /**
     *
     * This function is responsible for mapping recursively each interface implemented
     * inside the class and its superclasses. It first maps all interfaces class type
     * to a map where the key is the interface and the value is a set of "sub" interfaces.
     * {@link DependencySearchTree#appendClassTypesInSearchTree} will be responsible for
     * taking this Sets of interfaces types and add to the search tree.
     * <p>
     * If we have an interface A which extends B, if A is implemented by a {@link Managed}
     * class, the map will look like a key equals to "{@code Class<A>}" and value
     * "{@code Set<Class<?>>}" that contains the B interface. It is ordered by the very
     * first interface parent.
     * <p>
     * This is a convenient function used by {@link #insert(TypeInformationHolder)}
     * function.
     *
     * @param typeInformationHolder The {@link TypeInformationHolder} wraps the dependency
     *                              class type that will receive a new {@link SingletonNode}.
     *
     * @see DependencySearchTree#createInterfaceTreeMapping(Class)
     * @see DependencySearchTree#createSuperclassTreeMapping(Class)
     *
     * @since 1.0
     *
     **/
    private void createTypeFamiliesInSearchTree(TypeInformationHolder typeInformationHolder) {
        createTypeFamiliesInSearchTree(new SingletonNode(typeInformationHolder));
    }

    /**
     *
     * This function is responsible for mapping recursively each interface implemented
     * inside the class and its superclasses. It first maps all interfaces class type
     * to a map where the key is the interface and the value is a set of "sub" interfaces.
     * {@link DependencySearchTree#appendClassTypesInSearchTree} will be responsible for
     * taking this Sets of interfaces types and add to the search tree.
     * <p>
     * If we have an interface A which extends B, if A is implemented by a {@link Managed}
     * class, the map will look like a key equals to "{@code Class<A>}" and value
     * "{@code Set<Class<?>>}" that contains the B interface. It is ordered by the very
     * first interface parent.
     *
     * @param singletonNode The SingletonNode created from {@code dependencyClassType}.
     *
     * @see DependencySearchTree#createInterfaceTreeMapping(Class)
     * @see DependencySearchTree#createSuperclassTreeMapping(Class)
     *
     * @since 1.0
     *
     **/
    private void createTypeFamiliesInSearchTree(SingletonNode singletonNode) {
        TypeInformationHolder typeInformationHolder =
                singletonNode.getTypeInformationHolder();

        Class<?> dependencyClassType = typeInformationHolder.getWrappedClassType();

        Map<Class<?>, Set<Class<?>>> interfacesTreeMapping =
                createInterfaceTreeMapping(dependencyClassType);

        /* it should add the dependency class to the root node if no interface is implemented */
        if (interfacesTreeMapping.isEmpty())
            rootNode.addChildNode(singletonNode);

        appendClassTypesInSearchTree(interfacesTreeMapping, typeInformationHolder);

        if (!dependencyClassType.isInterface()) {

            final Map<Class<?>, Set<Class<?>>> superclassTreeMapping
                    = createSuperclassTreeMapping(dependencyClassType);

            appendClassTypesInSearchTree(superclassTreeMapping, typeInformationHolder);
        }
        createVirtualSingletonsInSearchTree(singletonNode);
    }

    /**
     *
     * This function takes the pre-computed hierarchy map of an class type and inserts the hierarchy
     * to a tree structure. This is fundamental to the {@link DependencySearchTree#query(Class, QueryOptions)}
     * be able to query through the generic interfaces and find the final implementation
     * of that interface or abstract class. It uses {@link DependencySearchTree#appendToSearchTree}
     * to it.
     *
     * @param classTypesMapping The sub-interfaces & superclasses mapping of the
     *                          dependency class type.
     * @param typeInformationHolder The {@link TypeInformationHolder} wraps the dependency
     *                              class type that will receive a new {@link SingletonNode}.
     *
     * @see DependencySearchTree#appendToSearchTree(Set, TypeInformationHolder)
     *
     * @since 1.0
     *
     **/
    private void appendClassTypesInSearchTree(Map<Class<?>, Set<Class<?>>> classTypesMapping,
                                              TypeInformationHolder typeInformationHolder)
    {
        for (Map.Entry<Class<?>, Set<Class<?>>> classEntries : classTypesMapping.entrySet())
            appendToSearchTree(classEntries.getValue(), typeInformationHolder);
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
     * @param dependencyClassType The managed class type to be inserted to the search tree.
     *
     * @since 1.0
     *
     **/
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
    private void generateSubInterfacePathSet(Class<?> interfaceClassType, Set<Class<?>> subInterfacesSet)
    {
        for (Class<?> childInterfaceClassType : interfaceClassType.getInterfaces()) {
            generateSubInterfacePathSet(childInterfaceClassType, subInterfacesSet);
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
     * @param dependencyClassType The managed class type to be inserted to the search tree.
     *
     * @see DependencySearchTree#createInterfaceTreeMapping(Class)
     *
     * @since 1.0
     *
     **/
    private Map<Class<?>, Set<Class<?>>> createSuperclassTreeMapping(Class<?> dependencyClassType) {
        Map<Class<?>, Set<Class<?>>> superclassTreeMapping = new LinkedHashMap<>();
        Set<Class<?>> subSuperclassesSet = new LinkedHashSet<>();
        generateSuperclassPathSet(dependencyClassType, subSuperclassesSet);
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

    /**
     *
     * Given an ordered set of classes types, this function will insert recursively
     * the class type of superclass and its interfaces as a {@link JavaTypeNode}. The
     * last element inserted it is the {@link SingletonNode} of {@code dependencyObject}.
     *
     * @param orderedClassType  The {@link Class} object of {@code dependencyObject}.
     * @param typeInformationHolder The {@link TypeInformationHolder} wraps the dependency
     *                              class type that will receive a new {@link SingletonNode}.
     *
     * @see DependencySearchTree#createInterfaceTreeMapping(Class)
     *
     * @since 1.0
     *
     **/
    private void appendToSearchTree(Set<Class<?>> orderedClassType, TypeInformationHolder typeInformationHolder) {
        AbstractNode parentNode = rootNode;

        for (Class<?> classType : orderedClassType)
            parentNode = findOrCreateJavaTypeNode(classType, parentNode);

        parentNode.addChildNode(new SingletonNode(typeInformationHolder));
    }

    /**
     *
     * Given a parent {@link SingletonNode}, this function will scan for all virtual methods
     * and add them to the search tree. If a virtual method is found, a {@link VirtualSingletonNode}
     * is created with its {@link VirtualSingletonNode#getParentSingletionNode()} set to the
     * {@code parentSingletonNode} parameter.
     *
     * @see DependencySearchTree#createInterfaceTreeMapping(Class)
     *
     * @since 1.0.4
     *
     **/
    private void createVirtualSingletonsInSearchTree(SingletonNode parentSingletonNode) {
        Class<?> dependencyClassType = parentSingletonNode.getNodeClassType();
        ReflectionUtil.consumeAllVirtualMethodsFromClassType(dependencyClassType, (virtualMethod) -> {
            Class<?> virtualMethodClassType = virtualMethod.getReturnType();

            TypeInformationHolder typeInformationHolder = TypeInformationHolderFactory
                    .createTypeInformation(virtualMethodClassType);

            createTypeFamiliesInSearchTree(new VirtualSingletonNode(typeInformationHolder,
                    parentSingletonNode, virtualMethod));
        });

    }

    /**
     *
     * This function is used internally by
     * {@link DependencySearchTree#appendToSearchTree(Set, TypeInformationHolder)} to find
     * the {@link JavaTypeNode} relative to {@code parentNode} that has the same class type
     * as the parameter {@code classType}. Returns null if no {@link JavaTypeNode} correspondent
     * node exists in the tree.
     *
     * @param classType  The {@link Class} type used to find the {@link JavaTypeNode} in the tree.
     * @param parentNode The scanned node used to find the correspondent {@link JavaTypeNode}.
     *
     * @see DependencySearchTree#findOrCreateJavaTypeNode(Class, AbstractNode)
     * @see DependencySearchTree#appendToSearchTree(Set, TypeInformationHolder)
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
     * This function is used internally by
     * {@link DependencySearchTree#appendToSearchTree(Set, TypeInformationHolder)} to find the
     * {@link JavaTypeNode} relative to {@code parentNode} that has the same class type as the
     * parameter {@code classType}. If there is no correspondent {@link JavaTypeNode}, a new node
     * is created as a child of {@code parentNode}. The function should never return a null value.
     *
     * @param classType  The {@link Class} type used to find the {@link JavaTypeNode} in the tree.
     * @param parentNode The scanned node used to find the correspondent {@link JavaTypeNode}.
     *
     * @see DependencySearchTree#findJavaTypeNode(Class, AbstractNode)
     * @see DependencySearchTree#appendToSearchTree(Set, TypeInformationHolder)
     *
     * @since 1.0
     *
     **/
    private JavaTypeNode findOrCreateJavaTypeNode(Class<?> classType,
                                                  final AbstractNode parentNode) {

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
    @SuppressWarnings({"Unchecked"})
    public <T> T query(Class<? extends T> classType, QueryOptions queryOptions) {
        SingletonNode singletonNode = querySingletonNode(classType, queryOptions);
        return (singletonNode != null) ? (T) singletonNode.getNodeInstance() : null;
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
    public SingletonNode querySingletonNode(Class<?> classType, QueryOptions queryOptions) {
        final String filteredClassName     = queryOptions.filteredClassName();
        final boolean hasFilteredClassName = !StrUtil.isNullOrBlank(filteredClassName);

        List<SingletonNode> matchResultSet = querySingletonsByType(classType);

        if (matchResultSet.isEmpty())
            return null;

        if (!hasFilteredClassName)
            return matchResultSet.get(0);

        SingletonNode lastMatchedSingletonNode = null;

        for (SingletonNode singletonNode : matchResultSet) {
            lastMatchedSingletonNode = singletonNode;
            if (singletonNode.getNodeName().equals(filteredClassName))
                return singletonNode;
        }

        if (!queryOptions.retrieveAnyways())
            return null;

        return lastMatchedSingletonNode;
    }

    /**
     *
     * This function will return a Set of SingletonNode that its instances are compatible
     * to the class type passed in {@code classType}.
     *
     * @param classType The class type to be searched in the tree.
     *
     * @return A non-null {@link Set} of SingletonNode.
     *
     * @since 1.0
     *
     **/
    public List<SingletonNode> querySingletonsByType(Class<?> classType) {
        final List<SingletonNode> matchResultList = new ArrayList<>();
        queryRecursivelySingletons(classType, rootNode, matchResultList);
        return matchResultList;
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
     * @since 1.0
     *
     **/
    private void queryRecursivelySingletons(Class<?> classType,
                                            AbstractNode node, List<SingletonNode> matchResultSet) {
        for (AbstractNode childNode : node.children()) {
            if (childNode instanceof JavaTypeNode javaTypeNode) {

                if (!javaTypeNode.getNodeClassType().isAssignableFrom(classType))
                    continue;

                queryRecursivelySingletons(classType, javaTypeNode, matchResultSet);
            }
            else if (childNode instanceof SingletonNode singletonNode) {
                final Class<?> singletonClassType = singletonNode.getNodeClassType();

                if (classType.isAssignableFrom(singletonClassType))
                    matchResultSet.add(singletonNode);
            }
        }
    }

    /**
     *
     * Used to propagate an instance object to all {@link SingletonNode}'s where their
     * {@link SingletonNode#getNodeClassType()} is equals to the {@code classType}.
     *
     * @param classType   The class type to be searched in the tree.
     * @param nodeInstance The node instance to be propagated.
     *
     * @since 1.0
     *
     **/
    public void propagateSingletonInstanceToNodes(Class<?> classType, Object nodeInstance) {
        Checks.nonNull(nodeInstance, "It is not allowed to propagate a null value through nodes.");
        querySingletonsByType(classType)
                .forEach(singletonNode -> singletonNode.setNodeInstance(nodeInstance));
    }

    /**
     * Retrive all singleton instances inserted to the tree.
     *
     * @since 1.0.6
     *
     **/
    public Set<Object> queryAllInstances() {
        final List<SingletonNode> matchResultList = new ArrayList<>();
        queryRecursivelySingletons(Object.class, rootNode, matchResultList);
        return matchResultList.stream()
                .filter(SingletonNode::hasSingletonInstance)
                .map(SingletonNode::getNodeInstance)
                .collect(Collectors.toSet());
    }

    public AbstractNode getRootNode() {
        return rootNode;
    }
}

