package com.dependency4j.node;

/**
 *
 * <b>JavaTypeNode</b> is an implementation of {@link BaseNode}. It holds information
 * about the class type of the node, whether it is a class or an interface, it will
 * be stored in the tree as a {@link JavaTypeNode}. It wraps a {@link Class} reference.
 *
 * @author daviddev16
 *
 **/
public final class JavaTypeNode extends BaseNode {

    private final Class<?> nodeClassType;

    public JavaTypeNode(Class<?> nodeClassType) {
        this.nodeClassType = nodeClassType;
    }

    /**
     *
     * {@inheritDoc}
     * <p>
     * @return {@link NodeType#INTERFACE} if {@code nodeClassType} is flagged as
     *         {@link Class#isInterface()}. If it is not, than it will return
     *         {@link NodeType#SUPERCLASS}.
     **/
    @Override
    public NodeType getNodeType() {
        return nodeClassType.isInterface() ?
                NodeType.INTERFACE : NodeType.SUPERCLASS;
    }

    /**
     * This function returns a classType from an arbitrary hierarchy of an instance
     * that is managed by {@link com.dependency4j.DependencyManager}. This is used
     * for querying instances in the {@link com.dependency4j.DependencySearchTree}.
     *
     **/
    public Class<?> getNodeClassType() {
        return nodeClassType;
    }

}
