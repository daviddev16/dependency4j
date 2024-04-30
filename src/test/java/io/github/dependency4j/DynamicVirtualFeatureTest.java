package io.github.dependency4j;

import io.github.dependency4j.example.virtual.SecretsManager;
import io.github.dependency4j.example.virtual.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DynamicVirtualFeatureTest {

    @Test
    @DisplayName("Dynamic Virtual Singleton Nodes creation test")
    public void initialVirtualSingletonNodesTest()
    {
        DependencyManager dependencyManager = DependencyManager.builder()
                .strategy("VirtualTesting2")
                .includeDependencyManagerAsDependency()
                .installPackage("io.github.dependency4j.example")
                .prepare(this)
                .getDependencyManager();

        SecretsManager secretsManager = dependencyManager.query(SecretsManager.class);

        Assertions.assertNotNull(secretsManager);
        Assertions.assertEquals(secretsManager.getSecretKey(), "default123");

        UserService userService = dependencyManager.query(UserService.class);
        Assertions.assertNotNull(userService);
        Assertions.assertNotNull(userService.getSecret());
        Assertions.assertEquals(userService.getSecret().getSecretKey(), "default123");

    }


}
