package io.github.dependency4j.node;

import io.github.dependency4j.*;

/**
 *
 * <b>RootNode</b> represents the very first node of {@link DependencySearchTree}.
 * It is the root node of the tree.
 *
 * @author daviddev16
 *
 * @since 1.0.0
 *
 **/
public final class RootNode extends BaseNode {

    /**
     *
     * {@inheritDoc}
     * <p>
     * This function will always return {@link NodeType#ROOT}.
     *
     * @since 1.0.0
     *
     **/
    @Override
    public NodeType getNodeType() {
        return NodeType.ROOT;
    }

}
