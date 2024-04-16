package example;

import com.dependency4j.Managed;
import com.dependency4j.Strategy;

public @Managed(strategy = @Strategy("v2")) class ServiceImplv2 implements IService {

    @Override
    public void loadService() {
        System.out.println("Serviço carregado v2");
    }

}
