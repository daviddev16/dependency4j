package example;

import com.dependency4j.Managed;
import com.dependency4j.Pull;
import com.dependency4j.Strategy;

@Managed(disposable = false, strategy = @Strategy({"Production"}))
public class HomeProductionController implements IHomeController {

    private IService genericService;

    @Pull
    public HomeProductionController(IService service) {
        genericService = service;
    }

    @Override
    public String helloMessage() {
        genericService.loadService();
        return "Hello from production!";
    }

}
