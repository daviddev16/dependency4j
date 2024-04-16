package com.dependency4j.example.service;

import com.dependency4j.Managed;
import com.dependency4j.example.controller.StagingHomeController;

public @Managed class MessagingService implements IMessagingService {

    @Override
    public String getMessageByClassType(Class<?> classType) {

        if (classType.equals(StagingHomeController.class))
            return "Hello from Staging!";

        return "Unknown class type?";

    }

}
