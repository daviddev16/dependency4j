package com.dependency4j.node;

import com.dependency4j.AbstractNode;

import java.util.Collections;
import java.util.Set;

/**
 *
 * <b>SingletonNode</b> represents the node that holds the object
 * instance. The object instance is also called the dependency object.
 *
 * @author David Duarte Pinheiro
 * @version 1.0
 *
 **/
public class SingletonNode implements AbstractNode {

    private Object nodeSingletonInstance;
    private final String nodeName;
    private Class<?> classType;

    public SingletonNode(Class<?> classType, String nodeName) {
        this. classType = classType;
        this.nodeName = nodeName;
    }

    @Override
    public void addChildNode(AbstractNode abstractNode) {
        throw new IllegalStateException("SingletonNode cannot have child.");
    }

    public Class<?> getNodeSingletonClassType() {
        return classType;
    }

    public Object getNodeSingletonInstance() {
        return nodeSingletonInstance;
    }

    public void setNodeSingletonInstance(Object nodeSingletonInstance) {
        this.nodeSingletonInstance = nodeSingletonInstance;
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
