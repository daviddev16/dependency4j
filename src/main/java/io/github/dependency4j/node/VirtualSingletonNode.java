package io.github.dependency4j.node;

import java.lang.reflect.Method;

public class VirtualSingletonNode extends SingletonNode {

    private final SingletonNode parentSingletionNode;
    private final Method virtualMethod;

    public VirtualSingletonNode(Class<?> classType,
                                SingletonNode parentSingletionNode, Method virtualMethod) {
        super(classType);
        this.parentSingletionNode = parentSingletionNode;
        this.virtualMethod = virtualMethod;
    }

    public Method getVirtualMethod() {
        return virtualMethod;
    }

    public SingletonNode getParentSingletionNode() {
        return parentSingletionNode;
    }
}
