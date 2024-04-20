package com.dependency4j.example.service;

import com.dependency4j.Managed;
import com.dependency4j.example.controller.StagingHomeController;
import com.dependency4j.example.controller.ProductionHomeController;

@Managed(disposable = false)
public class MessagingServiceImpl implements IMessagingService {

    @Override
    public String getMessageByClassType(Class<?> classType) {

        if (classType.equals(StagingHomeController.class))
            return "Hello from Staging!";

        if (classType.equals(ProductionHomeController.class))
            return "Hello from Production!";

        return "Unknown class type?";
    }
}
