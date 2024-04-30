package io.github.dependency4j.example.composition;

@ManagedInStaging
public class StagingEnvironmentController implements CompositionEnvironment {
    @Override
    public String helloComposition() {
        return "Staging";
    }
}
