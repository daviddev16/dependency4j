package com.dependency4j.example.injection.method;

import com.dependency4j.Managed;

@Managed(disposable = false)
public  class JavaVersionConfig {

    private final String javaVersion;

    public JavaVersionConfig() {
        this.javaVersion = System.getProperty("java.version");
    }

    public String getVersion() {
        return javaVersion;
    }
}
