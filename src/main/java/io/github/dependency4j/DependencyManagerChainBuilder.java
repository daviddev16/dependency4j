package io.github.dependency4j;

import io.github.dependency4j.util.Checks;

import java.util.function.Consumer;

public final class DependencyManagerChainBuilder {

    private final DependencyManager dependencyManager;

    public DependencyManagerChainBuilder() {
        this(new DependencyManager());
    }

    public DependencyManagerChainBuilder(DependencyManager dependencyManager) {
        Checks.nonNull(dependencyManager, "dependencyManager must not be null.");
        this.dependencyManager = dependencyManager;
    }

    public DependencyManagerChainBuilder strategy(String... strategies) {
        Checks.nonNull(strategies, "strategies must not be null.");
        dependencyManager.addStrategy(strategies);
        return this;
    }

    public DependencyManagerChainBuilder install(String packageName) {
        dependencyManager.install(packageName);
        return this;
    }

    public DependencyManagerChainBuilder prepare(Object object) {
        Checks.nonNull(object, "object must not be null.");
        dependencyManager.installSingleInstance(object);
        return this;
    }

    public DependencyManagerChainBuilder consume(Consumer<DependencyManager> dependencyManagerConsumer) {
        Checks.nonNull(dependencyManagerConsumer, "dependencyManagerConsumer must not be null.");
        dependencyManagerConsumer.accept(dependencyManager);
        return this;
    }

    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

}