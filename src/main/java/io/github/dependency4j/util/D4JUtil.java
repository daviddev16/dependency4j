package io.github.dependency4j.util;

import io.github.dependency4j.AbstractNode;
import io.github.dependency4j.DependencySearchTree;
import io.github.dependency4j.Managed;

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
