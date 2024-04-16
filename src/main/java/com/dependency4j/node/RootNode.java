package com.dependency4j.node;

/**
 *
 * <b>RootNode</b> represents the very first node of the tree.
 *
 * @author David Duarte Pinheiro
 * @version 1.0
 *
 **/
public final class RootNode extends BaseNode {

    /**
     *
     * {@inheritDoc}
     *
     * */
    @Override
    public NodeType getNodeType() {
        return NodeType.ROOT;
    }

}
