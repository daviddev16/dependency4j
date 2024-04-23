package io.github.dependency4j;

import io.github.dependency4j.example.controller.IHomeController;
import io.github.dependency4j.example.controller.ProductionHomeController;
import io.github.dependency4j.example.service.IMessagingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class StrategyTest {

    public static final String TEST_EXAMPLE_PACKAGE = "io.github.dependency4j.example";

    @Test
    @DisplayName("DI and IoC based on Production Strategy")
    public void initialInverseOfControlInterfaceMatchingTesting()
    {
        final DependencyManager dependencyManager = DependencyManager.builder()
                .strategy("Production")
                .installPackage(TEST_EXAMPLE_PACKAGE)
                .getDependencyManager();

        IHomeController productionController =
                dependencyManager.query(IHomeController.class);

        IMessagingService messagingService
                = dependencyManager.query(IMessagingService.class);

        Class<?> productionHomeControllerClass = ProductionHomeController.class;

        Assertions.assertEquals(productionController.getClass(), productionHomeControllerClass);
        Assertions.assertEquals(productionController.environmentName(), "Production");
        Assertions.assertEquals(productionController.helloMessage(),
                messagingService.getMessageByClassType(productionHomeControllerClass));

    }

}
