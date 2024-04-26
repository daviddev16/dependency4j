package io.github.dependency4j;

import io.github.dependency4j.example.controller.IHomeController;
import io.github.dependency4j.example.controller.StagingHomeController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DependencyManager enables Primitive Default Values Test")
public class DefaultValuePrimitiveTest {

    private long testId;
    private IHomeController homeController;

    @Test
    @DisplayName("Throw exception when primitive default values disable test")
    public void shouldThrowExceptionWithPrimitiveDefaultValuesDisable()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
        {
            DependencyManager dependencyManager = DependencyManager.builder()
                    .strategy("Staging", "Testing")
                    .includeDependencyManagerAsDependency()
                    .installPackage("io.github.dependency4j.example")
                    .prepare(this)
                    .getDependencyManager();

            DefaultValuePrimitiveTest primitiveTest = dependencyManager.query(getClass());
            Assertions.assertNotNull(primitiveTest);
        });

    }

    @Test
    @DisplayName("Default Primitive values enabled test")
    public void shouldNotThrowExceptionWithPrimitiveDefaultValuesEnable()
    {
        DependencyManager dependencyManager = DependencyManager.builder()
                .strategy("Staging", "Testing")
                .enablePrimitiveDefaultValue()
                .includeDependencyManagerAsDependency()
                .installPackage("io.github.dependency4j.example")
                .prepare(this)
                .getDependencyManager();

        DefaultValuePrimitiveTest primitiveTest = dependencyManager.query(getClass());
        Assertions.assertNotNull(primitiveTest);

        Assertions.assertEquals(primitiveTest.testId, 0L);
        Assertions.assertNotNull(primitiveTest.homeController);
        Assertions.assertEquals(primitiveTest.homeController.getClass(), StagingHomeController.class);
    }

    @Pull
    public void setHomeControllerWithTestId(IHomeController homeController, long testId) {
        this.homeController = homeController;
        this.testId = testId;
    }

}
