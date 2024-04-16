package com.dependency4j;

import com.dependency4j.example.controller.IHomeController;
import com.dependency4j.example.service.IMessagingService;
import com.dependency4j.example.controller.StagingHomeController;
import org.junit.Assert;
import org.junit.Test;

public class InverseOfControlTest {

    private final DependencyManager dependencyManager;

    private IMessagingService messagingService;

    public InverseOfControlTest()
    {
        dependencyManager = new DependencyManager();
        dependencyManager.install("com.dependency4j.example");
        dependencyManager.installSingleInstance(this);
    }

    @Test
    public void inverseOfControlTesting()
    {
        IHomeController homeController = dependencyManager
                .getDependencySearchTree()
                .query(IHomeController.class, QueryOptions.none());

        Assert.assertNotNull(homeController);
        Assert.assertEquals(homeController.getClass(), StagingHomeController.class);

        Assert.assertEquals("Staging", homeController.environmentName());
        Assert.assertEquals("Hello from Staging!", homeController.helloMessage());

    }

    @Test
    public void dependencyInjectionTesting()
    {

        IHomeController homeController = dependencyManager
                .getDependencySearchTree()
                .query(IHomeController.class, QueryOptions.none());

        Assert.assertNotNull(homeController);

        Assert.assertEquals("Staging", homeController.environmentName());

        Assert.assertEquals(homeController.helloMessage(),
                messagingService.getMessageByClassType(StagingHomeController.class));
    }

    @Pull
    public void setMessagingService(IMessagingService messagingService) {
        this.messagingService = messagingService;
    }

}
