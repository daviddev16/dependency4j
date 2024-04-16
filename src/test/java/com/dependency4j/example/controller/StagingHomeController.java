package com.dependency4j.example.controller;

import com.dependency4j.Managed;
import com.dependency4j.Pull;
import com.dependency4j.Strategy;
import com.dependency4j.example.service.IMessagingService;

@Managed(
        strategy = @Strategy({"Staging"})
)
public class StagingHomeController extends Home {

    private final IMessagingService messagingService;

    public @Pull StagingHomeController(IMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @Override
    public String helloMessage() {
        return messagingService.getMessageByClassType(StagingHomeController.class);
    }

    @Override
    public String environmentName() {
        return "Staging";
    }

    @Override
    public String getHomeName() {
        return "StagingHome";
    }
}
