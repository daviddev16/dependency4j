package com.dependency4j;

import java.util.Collections;
import java.util.Set;

public class SingletonNode implements AbstractNode {

    private final Object nodeSingletonInstance;
    private final String nodeName;

    public SingletonNode(Object nodeSingletonInstance, String nodeName) {
        this.nodeSingletonInstance = nodeSingletonInstance;
        this.nodeName = nodeName;
    }

    public Class<?> getNodeSingletonClassType() {
        return (nodeSingletonInstance != null) ?
                nodeSingletonInstance.getClass() : null;
    }

    @Override
    public void addChildNode(AbstractNode abstractNode) {
        throw new IllegalStateException("SingletonNode cannot have child.");
    }

    public Object getNodeSingletonInstance() {
        return nodeSingletonInstance;
    }

    @Override
    public Set<? extends AbstractNode> children() {
        return Collections.emptySet();
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.IMPLEMENTATION;
    }

    public String getNodeName() {
        return nodeName;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SingletonNode otherSingletonNode) {
            return this.getNodeSingletonClassType()
                    .equals(otherSingletonNode.getNodeSingletonClassType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getNodeSingletonClassType().hashCode();
    }
}
