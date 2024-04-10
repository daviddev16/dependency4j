package com.dependency4j;


public final class JavaTypeNode extends BaseNode {

    private final Class<?> nodeClassType;

    public JavaTypeNode(Class<?> nodeClassType) {
        this.nodeClassType = nodeClassType;
    }

    @Override
    public NodeType getNodeType() {
        return nodeClassType.isInterface() ?
                NodeType.INTERFACE : NodeType.SUPERCLASS;
    }

    public Class<?> getNodeClassType() {
        return nodeClassType;
    }

}
