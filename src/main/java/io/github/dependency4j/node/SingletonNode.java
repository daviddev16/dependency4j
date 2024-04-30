package io.github.dependency4j.node;

import io.github.dependency4j.*;
import io.github.dependency4j.util.ReflectionUtil;
import io.github.dependency4j.util.StrUtil;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 *
 * <b>SingletonNode</b> represents the node that holds the object instance.
 * The object instance is also called the dependency object.
 *
 * @author daviddev16
 *
 **/
public class SingletonNode implements AbstractNode {

    private Object nodeSingletonInstance;
    private final String nodeName;
    private final Class<?> classType;

    public SingletonNode(Class<?> classType) {
        this.classType = classType;
        this.nodeName = ReflectionUtil.coalesceClassName(classType);
    }

    /**
     *
     * SingletonNode does not have children. This function should not be
     * called.
     *
     * @throws IllegalStateException when the function is called.
     *
     **/
    @Override
    public void addChildNode(AbstractNode abstractNode) {
        throw new IllegalStateException("SingletonNode cannot have child.");
    }

    /**
     *
     * Sets the instance value of the {@link SingletonNode}.
     *
     **/
    public void setNodeInstance(Object nodeSingletonInstance) {
        this.nodeSingletonInstance = nodeSingletonInstance;
    }

    /**
     *
     * Returns the wrapped class type of {@link SingletonNode}.
     *
     **/
    public Class<?> getNodeClassType() {
        return classType;
    }

    /**
     * Returns the instance object of {@link SingletonNode}.
     * */
    public Object getNodeInstance() {
        return nodeSingletonInstance;
    }

    /**
     *
     * {@inheritDoc}
     * <p>
     * This function will always return {@link NodeType#IMPLEMENTATION}.
     **/
    @Override
    public NodeType getNodeType() {
        return NodeType.IMPLEMENTATION;
    }

    /**
     *
     * This function returns the node name of this {@link SingletonNode}.
     * It is used when filtered name is used in the {@link QueryOptions}
     * passed in the query of {@link DependencySearchTree}.
     *
     **/
    public String getNodeName() {
        return nodeName;
    }

    public boolean hasSingletonInstance() {
        return nodeSingletonInstance != null;
    }

    /**
     *
     * {@inheritDoc}
     *
     **/
    @Override
    public Set<? extends AbstractNode> children() {
        return Collections.emptySet();
    }

    /**
     *
     * {@inheritDoc}
     * <p>
     * {@link SingletonNode}'s hashCode is equals to {@code classType.hashCode() ^ 7}.
     * if {@link  SingletonNode#classType} is null, then its hashCode will be equals to
     * {@code Objects.hashCode(this)}.
     *
     **/
    @Override
    public int hashCode() {
        return (classType == null) ?
                Objects.hashCode(this) : classType.hashCode() ^ 7;
    }

    /**
     *
     * When obj is a {@link SingletonNode}, then this function will return true if
     * the {@link SingletonNode#classType} is equals to the current {@code classType}
     * value of this object.
     *
     **/
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        else if (obj instanceof SingletonNode singletonNode) {
            return singletonNode.getNodeClassType().equals(this.classType);
        }
        return false;
    }
}
