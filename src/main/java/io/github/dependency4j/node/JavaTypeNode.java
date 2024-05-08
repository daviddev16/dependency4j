package io.github.dependency4j.node;

import io.github.dependency4j.DependencyManager;
import io.github.dependency4j.DependencySearchTree;

/**
 *
 * <b>JavaTypeNode</b> is an implementation of {@link BaseNode}. It holds information
 * about the class type of the node, whether it is a class or an interface, it will
 * be stored in the tree as a {@link JavaTypeNode}. It wraps a {@link Class} reference.
 *
 * @author daviddev16
 *
 * @since 1.0.0
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
     *
     * @since 1.0.0
     **/
    @Override
    public NodeType getNodeType() {
        return nodeClassType.isInterface() ?
                NodeType.INTERFACE : NodeType.SUPERCLASS;
    }

    /**
     * This function returns a classType from an arbitrary hierarchy of an instance
     * that is managed by {@link DependencyManager}. This is used for querying instances
     * in the {@link DependencySearchTree}.
     *
     * @since 1.0.0
     *
     **/
    public Class<?> getNodeClassType() {
        return nodeClassType;
    }

}
