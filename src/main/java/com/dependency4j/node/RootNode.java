package com.dependency4j.node;

import com.dependency4j.*;

/**
 *
 * <b>RootNode</b> represents the very first node of {@link DependencySearchTree}.
 * It is the root node of the tree.
 *
 * @author daviddev16
 *
 **/
public final class RootNode extends BaseNode {

    /**
     *
     * {@inheritDoc}
     * <p>
     * This function will always return {@link NodeType#ROOT}.
     **/
    @Override
    public NodeType getNodeType() {
        return NodeType.ROOT;
    }

}
