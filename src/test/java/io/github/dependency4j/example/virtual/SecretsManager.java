package io.github.dependency4j.example.virtual;


public class SecretsManager implements ISecret {

    private final String secretKey;

    public SecretsManager(String secretKey) {
        this.secretKey = secretKey;
    }

    public SecretsManager() {
        this.secretKey = "default123";
    }

    @Override
    public String getSecretKey() {
        return secretKey;
    }
}
