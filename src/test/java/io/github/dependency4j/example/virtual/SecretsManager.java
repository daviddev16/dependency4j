package io.github.dependency4j.example.virtual;


public class SecretsManager implements ISecret {

    private final String secretKey;

    public SecretsManager(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String getSecretKey() {
        return secretKey;
    }
}
