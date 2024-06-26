package io.github.dependency4j.example.injection.method;

import io.github.dependency4j.Managed;
import io.github.dependency4j.Pull;
import io.github.dependency4j.Strategy;

@Managed(strategy = @Strategy({"cleanStrategy"}))
public class CleanEnvironment implements Environment {

    private JavaVersionConfig javaVersionConfig;
    private NonManagedType nonManagedType;

    public CleanEnvironment() {}

    public @Pull void setJavaVersionConfig(JavaVersionConfig javaVersionConfig)
    {
        this.javaVersionConfig = javaVersionConfig;
    }

    public @Pull void setNonManagedType(NonManagedType nonManagedType) {
        this.nonManagedType = nonManagedType;
    }

    @Override
    public JavaVersionConfig getJavaVersionConfig() {
        return javaVersionConfig;
    }

    public NonManagedType getNonManagedType() {
        return nonManagedType;
    }
}
