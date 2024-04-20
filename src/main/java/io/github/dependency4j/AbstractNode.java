package io.github.dependency4j;

import io.github.dependency4j.node.JavaTypeNode;
import io.github.dependency4j.node.NodeType;
import io.github.dependency4j.node.RootNode;
import io.github.dependency4j.node.SingletonNode;

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
            return String.format("%s (%s)",
                    javaTypeNode.getNodeClassType().getName(),
                    javaTypeNode.getNodeType().name());

        else if (this instanceof RootNode rootNode)
            return String.format("Root (%d)", hashCode());

        else if (this instanceof SingletonNode singletonNode) {
            return String.format("Singleton of {%s} with instance {%s}",
                    singletonNode.getNodeClassType().getName(),
                    Objects.toString(singletonNode.getNodeInstance(), "<no instance>"));
        }

        return toString();
    }
}
