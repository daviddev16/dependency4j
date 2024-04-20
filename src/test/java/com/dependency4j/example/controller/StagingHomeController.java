package com.dependency4j.example.controller;

import com.dependency4j.Managed;
import com.dependency4j.Pull;
import com.dependency4j.Strategy;
import com.dependency4j.example.service.IMessagingService;

import java.io.File;

@Managed(
        strategy = @Strategy({"Staging", "Testing"})
)
public class StagingHomeController extends Home {

    private final IMessagingService messagingService;

    public @Pull StagingHomeController(IMessagingService messagingService, File file) {
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
