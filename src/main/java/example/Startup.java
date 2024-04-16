package example;

import com.dependency4j.Managed;
import com.dependency4j.Pull;

public @Managed class Startup {

    private @Pull IHomeController homeController;

    public void startApplication() {
        System.out.println("Application has started.");
        homeController.helloMessage();
    }

}
