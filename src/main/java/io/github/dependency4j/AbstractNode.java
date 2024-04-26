package io.github.dependency4j;

import io.github.dependency4j.node.*;

import java.util.Objects;
import java.util.Set;

/**
 *
 * <b>AbstractNode</b> represents a generic structure to all nodes in the
 * {@link DependencySearchTree}.Each node has a Set of children and its
 * {@link NodeType}.
 *
 * @author daviddev16
 *
 **/
public interface AbstractNode {

    /**
     *
     * This function returns the {@link Set} of children. Each node has
     * an unlimited number of children.
     *
     * @since 1.0
     *
     **/
    Set<? extends AbstractNode> children();


    /**
     *
     * Adds a new child to the node. Depending on the implementation, this
     * function is equals to {@link Set#add(Object)} in {@link #children()}.
     *
     * @since 1.0
     *
     **/
    void addChildNode(AbstractNode abstractNode);

    /**
     *
     * The current node type. Use to differentiate Interfaces, Classes, the
     * root node and the instance node, also named {@link SingletonNode}.
     *
     * @since 1.0
     *
     **/
    NodeType getNodeType();

    /**
     *
     * Renders a pretty string of the current node.
     *
     * @since 1.0
     *
     **/
    default String getPrettyString() {

        if (this instanceof JavaTypeNode javaTypeNode)
            return String.format("JTN: %s",
                    javaTypeNode.getNodeClassType().getSimpleName(),
                    javaTypeNode.getNodeType().name());

        else if (this instanceof RootNode rootNode)
            return String.format("R:  %d", hashCode());

        else if (this instanceof VirtualSingletonNode virtualSingletonNode)
            return String.format("VSI: [Type: %s] [Instance: %s] [Virtualized from %s]",
                    virtualSingletonNode.getNodeClassType().getSimpleName(),
                    Objects.toString(virtualSingletonNode.getNodeInstance(), "<no instance>"),
                    virtualSingletonNode.getParentSingletionNode());

        else if (this instanceof SingletonNode singletonNode)
            return String.format("SI: [Type: %s] [Instance: %s]",
                    singletonNode.getNodeClassType().getSimpleName(),
                    namedInstance(singletonNode.getNodeInstance()));

        return toString();
    }

    private String namedInstance(Object o) {
        return (o != null) ? o.getClass().getSimpleName()+"@"+o.hashCode() : "<no instance>";
    }
}
