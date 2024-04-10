package com.dependency4j;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class BaseNode implements AbstractNode {

    private final Set<AbstractNode> childrenSet;

    public BaseNode() {
        this.childrenSet = new LinkedHashSet<>();
    }

    @Override
    public void addChildNode(AbstractNode abstractNode) {
        childrenSet.add(abstractNode);
    }

    @Override
    public Set<AbstractNode> children() {
        return childrenSet;
    }

}
