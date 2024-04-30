package io.github.dependency4j.example.virtual;

import io.github.dependency4j.Managed;
import io.github.dependency4j.Pull;
import io.github.dependency4j.Strategy;

@Managed(strategy = @Strategy("VirtualTesting2"))
public class UserService {

    private final ISecret secret;

    @Pull
    public UserService(ISecret secret) {
        this.secret = secret;
    }

    public ISecret getSecret() {
        return secret;
    }
}
