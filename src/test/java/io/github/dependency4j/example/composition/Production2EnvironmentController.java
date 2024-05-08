package io.github.dependency4j.example.composition;

import io.github.dependency4j.Strategy;

@TestingPrototype(testStrategy = @Strategy("QA_Prototype2"))
public class Production2EnvironmentController implements CompositionEnvironment {

    @Override
    public String helloComposition() {
        return "QAPrototypeEnvController";
    }

}
