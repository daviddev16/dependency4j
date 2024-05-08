package io.github.dependency4j.node;

import io.github.dependency4j.TypeInformationHolder;

import java.lang.reflect.Method;

public class VirtualSingletonNode extends SingletonNode {

    private final SingletonNode parentSingletionNode;
    private final Method virtualMethod;

    public VirtualSingletonNode(TypeInformationHolder typeInformationHolder,
                                SingletonNode parentSingletionNode, Method virtualMethod) {
        super(typeInformationHolder);
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
