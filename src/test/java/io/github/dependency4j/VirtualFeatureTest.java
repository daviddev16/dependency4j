package io.github.dependency4j;

import io.github.dependency4j.example.virtual.SecretsManager;
import io.github.dependency4j.util.D4JUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class VirtualFeatureTest {

    @Test
    @DisplayName("Virtual Singleton Nodes creation test")
    public void initialVirtualSingletonNodesTest()
    {
        DependencyManager dependencyManager = DependencyManager.builder()
                .strategy("VirtualTesting")
                .includeDependencyManagerAsDependency()
                .installPackage("io.github.dependency4j.example")
                .prepare(this)
                .getDependencyManager();

        SecretsManager secretsManager = dependencyManager.query(SecretsManager.class);

        Assertions.assertNotNull(secretsManager);
        Assertions.assertEquals(secretsManager.getSecretKey(), "mySecret123");

    }

    @Virtual
    public SecretsManager virtualSecretManager() {
        return new SecretsManager("mySecret123");
    }
}
