package io.github.dependency4j;

import io.github.dependency4j.example.virtual.ISecret;
import io.github.dependency4j.example.virtual.SecretsManager;
import io.github.dependency4j.util.D4JUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AbstractVirtualFeatureTest {

    @Test
    @DisplayName("Abstract Virtual Singleton Nodes creation [Issue: #7]")
    public void initialVirtualSingletonNodesTest()
    {
        DependencyManager dependencyManager = DependencyManager.builder()
                .strategy("VirtualTesting")
                .includeDependencyManagerAsDependency()
                .installPackage("io.github.dependency4j.example")
                .prepare(this)
                .getDependencyManager();

        ISecret secret = dependencyManager.query(ISecret.class);

        Assertions.assertNotNull(secret);
        Assertions.assertEquals(secret.getSecretKey(), "virtualSecret");
    }

    @Virtual
    public ISecret virtualSecretManager() {
        return new SecretsManager("virtualSecret");
    }
}
