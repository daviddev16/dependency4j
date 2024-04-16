package com.dependency4j.node;

import com.dependency4j.AbstractNode;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * <b>BaseNode</b> as the name says, it is a base implementation
 * of a node in the tree.
 * Provides a common {@link Set} instance
 * that can be used by the subclasses of {@link BaseNode}.
 *
 * @see SingletonNode
 * @see JavaTypeNode
 * @see RootNode
 *
 * @author David Duarte Pinheiro
 * @version 1.0
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
