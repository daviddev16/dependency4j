package io.github.dependency4j.example.composition;

@ManagedInProduction
public class ProductionEnvironmentController implements CompositionEnvironment {
    @Override
    public String helloComposition() {
        return "Production";
    }
}
