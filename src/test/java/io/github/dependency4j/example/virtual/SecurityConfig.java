package io.github.dependency4j.example.virtual;


import io.github.dependency4j.*;

@Managed(strategy = @Strategy("VirtualTesting2"))
public class SecurityConfig {

    @Virtual
    public ISecret virtualSecretManager( DependencyManager dependencyManager ) {
        return dependencyManager.installType(SecretsManager.class, InstallationType.DEFAULT);
    }

}

