package com.dependency4j.util;

import com.dependency4j.AbstractNode;
import com.dependency4j.DependencySearchTree;

public final class D4JUtil {

    public static void printDependencySearchTree(DependencySearchTree dependencySearchTree) {
        Checks.nonNull(dependencySearchTree, "dependencySearchTree must not be null.");
        printDependencySearchTree(dependencySearchTree.getRootNode(), "");
    }

    private static void printDependencySearchTree(AbstractNode node, String escape) {
        System.out.println(escape + "- " + node.getPrettyString());
        for (AbstractNode childNode : node.children())
            printDependencySearchTree(childNode, escape + "  ");
    }


}
