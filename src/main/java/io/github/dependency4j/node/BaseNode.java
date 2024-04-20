package io.github.dependency4j.node;

import io.github.dependency4j.AbstractNode;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * <b>BaseNode</b> as the name says, it is a base implementation of a
 * {@link AbstractNode} for the tree nodes. Provides a common {@link Set}
 * instance that can be used by the subclasses of {@link BaseNode} to
 * store their children.
 *
 * @see SingletonNode
 * @see JavaTypeNode
 * @see RootNode
 *
 * @author daviddev16
 *
 **/
public abstract class BaseNode implements AbstractNode {

    private final Set<AbstractNode> childrenSet;

    public BaseNode() {
        this.childrenSet = new LinkedHashSet<>();
    }

    /**
     *
     * {@inheritDoc}
     * <p>
     * Adds the {@code abstractNode} to the {@link Set} implementation
     * of {@link BaseNode}
     *
     **/
    @Override
    public void addChildNode(AbstractNode abstractNode) {
        childrenSet.add(abstractNode);
    }

    /**
     *
     * {@inheritDoc}
     * <p>
     * Returns the {@link Set} instance implemented inside of {@link BaseNode}
     * constructor.
     *
     **/
    @Override
    public Set<AbstractNode> children() {
        return childrenSet;
    }

}
