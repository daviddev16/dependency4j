package io.github.dependency4j;

import io.github.dependency4j.example.composition.CompositionEnvironment;
import io.github.dependency4j.example.composition.DynamicService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Annotation Composition Test")
public class AnnotationCompositionTest {

    @Test
    @DisplayName("Annotation Composition: @ManagedInStaging Test")
    void stagingEnvironmentConfigurationTest()
    {
        DependencyManager dependencyManager = DependencyManager.builder()
                .strategy("Staging")
                .installPackage("io.github.dependency4j.example.composition")
                .getDependencyManager();

        CompositionEnvironment compositionEnvironment = dependencyManager
                .query(CompositionEnvironment.class);

        Assertions.assertNotNull(compositionEnvironment);
        Assertions.assertEquals(compositionEnvironment.helloComposition(), "Staging");
    }

    @Test
    @DisplayName("Annotation Composition: @ManagedInProduction Test 2")
    void productionEnvironmentConfigurationTest()
    {
        DependencyManager dependencyManager = DependencyManager.builder()
                .strategy("Production")
                .installPackage("io.github.dependency4j.example.composition")
                .getDependencyManager();

        CompositionEnvironment compositionEnvironment = dependencyManager
                .query(CompositionEnvironment.class);

        Assertions.assertNotNull(compositionEnvironment);
        Assertions.assertEquals(compositionEnvironment.helloComposition(), "Production");
        Assertions.assertNull(dependencyManager.query(DynamicService.class));
    }

    @Test
    @DisplayName("Annotation Composition: Mapped Property Customization")
    void productionEnvironmentConfiguration2Test()
    {
        DependencyManager dependencyManager = DependencyManager.builder()
                .strategy("QA_Prototype", "QA_Prototype2")
                .installPackage("io.github.dependency4j.example.composition")
                .getDependencyManager();

        CompositionEnvironment compositionEnvironment = dependencyManager
                .query(CompositionEnvironment.class);

        Assertions.assertNotNull(compositionEnvironment);
        Assertions.assertEquals(compositionEnvironment.helloComposition(), "QAPrototypeEnvController");
    }

}
