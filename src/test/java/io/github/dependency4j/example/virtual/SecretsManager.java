package io.github.dependency4j.example.virtual;


public class SecretsManager {

    private final String secretKey;

    public SecretsManager(String secretKey) {
        System.out.println("teste");
        this.secretKey = secretKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
