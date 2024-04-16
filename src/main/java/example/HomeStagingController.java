package example;

import com.dependency4j.Managed;
import com.dependency4j.Pull;
import com.dependency4j.Strategy;

@Managed(strategy = @Strategy({"Staging"}))
public class HomeStagingController implements IHomeController {

    private IService service;

    @Pull
    public HomeStagingController(IService service) {
        this.service = service;
    }

    @Override
    public String helloMessage() {
        service.loadService();
        return "Hello from staging";
    }

}
