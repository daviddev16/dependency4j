package com.dependency4j;

import java.util.Set;

public interface AbstractNode {

    Set<? extends AbstractNode> children();

    void addChildNode(AbstractNode abstractNode);

    NodeType getNodeType();

    default String getPrettyString() {
        if (this instanceof JavaTypeNode javaTypeNode) {
            return String.format("%s (%s)",
                    javaTypeNode.getNodeClassType().getSimpleName(),
                    javaTypeNode.getNodeType().name());
        }
        else if (this instanceof RootNode rootNode) {
            return String.format("Root (%d)", hashCode());
        }
        else if (this instanceof SingletonNode singletonNode) {
            //return String.format("[%s] Instance of %s",
            //       "",
            //       singletonNode.getNodeSingletonInstance().getClass().getSimpleName());
            return singletonNode.getNodeSingletonInstance().getClass().getSimpleName() + " (reference)";
        }
        return toString();
    }

}
