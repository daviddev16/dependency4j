package io.github.dependency4j;

import io.github.dependency4j.example.controller.Home;
import io.github.dependency4j.example.controller.IHomeController;
import io.github.dependency4j.example.controller.StagingHomeController;
import io.github.dependency4j.example.other.DynamicComponent;
import io.github.dependency4j.example.service.IMessagingService;

import io.github.dependency4j.example.service.MessagingServiceImpl;
import io.github.dependency4j.util.D4JUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Base core DI & IoC test with no strategy")
public @Managed class DependencyManagerTest {

    private final DependencyManager dependencyManager;

    private @Pull IMessagingService messagingService;

    public DependencyManagerTest()
    {
        dependencyManager = DependencyManager.builder()
                .strategy("Testing")
                .installPackage("io.github.dependency4j.example")
                .prepare(this)
                .getDependencyManager();
    }

    @Test
    @DisplayName("Initial IoC Interface Matching Test")
    public void initialInverseOfControlInterfaceMatchingTesting()
    {
        IHomeController homeController = dependencyManager
                .getDependencySearchTree()
                .query(IHomeController.class, QueryOptions.none());

        assertNotNull(homeController);
        assertEquals(homeController.getClass(), StagingHomeController.class);


        assertEquals("Staging", homeController.environmentName());
        assertEquals("Hello from Staging!", homeController.helloMessage());
    }

    @Test
    public void testMessageServiceMatching() {

        IMessagingService messagingService
                = dependencyManager.query(IMessagingService.class);

        Assertions.assertNotNull(messagingService);
        Assertions.assertEquals(MessagingServiceImpl.class, messagingService.getClass());
    }

    @Test
    @DisplayName("Initial IoC Superclass Matching Test")
    public void initialInverseOfControlSuperclassMatchingTesting()
    {
        Home home = dependencyManager
                .getDependencySearchTree()
                .query(Home.class, QueryOptions.none());

        assertNotNull(home);
        assertEquals(home.getClass(), StagingHomeController.class);
        assertEquals("StagingHome", home.getHomeName());

    }

    @Test
    @DisplayName("Initial Dependency Injection Test")
    public void initialDependencyInjectionTesting()
    {
        IHomeController homeController = dependencyManager
                .getDependencySearchTree()
                .query(IHomeController.class, QueryOptions.none());

        assertNotNull(homeController);
        assertEquals("Staging", homeController.environmentName());

        assertEquals(homeController.helloMessage(),
                messagingService.getMessageByClassType(StagingHomeController.class));
    }

    @Test
    @DisplayName("Initial default installType Test")
    public void initialDefaultInstallTypeTest()
    {
        DynamicComponent dynamicComponent = dependencyManager.installType(DynamicComponent.class);
        Assertions.assertNotNull(dynamicComponent);
        Assertions.assertEquals(dynamicComponent.value(), "DynamicComponent");
    }

    @Test
    @DisplayName("Initial standalone installType Test")
    public void initialStandaloneInstallTypeTest()
    {
        DynamicComponent dynamicComponent = dependencyManager
                .installType(DynamicComponent.class, InstallationType.STANDALONE);

        Assertions.assertNotNull(dynamicComponent);
        Assertions.assertEquals(dynamicComponent.value(), "DynamicComponent");
    }

}
