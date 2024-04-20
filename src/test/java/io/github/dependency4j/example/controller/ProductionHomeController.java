package io.github.dependency4j.example.controller;

import io.github.dependency4j.Managed;
import io.github.dependency4j.Pull;
import io.github.dependency4j.Strategy;
import io.github.dependency4j.example.service.IMessagingService;

@Managed(
        strategy = @Strategy({"Production"})
)
public class ProductionHomeController extends Home {

    private final IMessagingService messagingService;

    public @Pull ProductionHomeController(IMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @Override
    public String helloMessage() {
        return messagingService
                .getMessageByClassType(ProductionHomeController.class);
    }

    @Override
    public String environmentName() {
        return "Production";
    }

    @Override
    public String getHomeName() {
        return "ProductionHome";
    }
}
