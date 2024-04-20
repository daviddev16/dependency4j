package com.dependency4j;

import com.dependency4j.example.injection.method.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dirty and Clean method injection")
public class MethodInjectionTest {

    private final String EXPECTED_JAVA_VERSION = System.getProperty("java.version");

    @Test
    @DisplayName("Test injection on Dirty Method Parameter type")
    void testInjectionOnNonDirtyMethod() {

        DependencyManager dependencyManager = DependencyManager
                .builder()
                .strategy("dirtyStrategy")
                .install("com.dependency4j.example.injection.method")
                .getDependencyManager();

        Environment environment = dependencyManager.query(Environment.class);

        assertEquals(environment.getClass(), DirtyEnvironment.class);

        JavaVersionConfig javaVersionConfig = environment.getJavaVersionConfig();

        assertNotNull(javaVersionConfig);

        assertEquals(environment.getNonManagedType(), NonManagedType.DUMMY);
        assertEquals(EXPECTED_JAVA_VERSION, javaVersionConfig.getVersion());

    }

    @Test
    @DisplayName("Test injection on Clean Method Parameter type")
    void testInjectionOnCleanMethod() {

        DependencyManager dependencyManager = DependencyManager
                .builder()
                .strategy("cleanStrategy")
                .install("com.dependency4j.example.injection.method")
                .getDependencyManager();

        Environment environment = dependencyManager.query(Environment.class);

        assertEquals(environment.getClass(), CleanEnvironment.class);

        JavaVersionConfig javaVersionConfig = environment.getJavaVersionConfig();

        assertNull(environment.getNonManagedType());
        assertNotNull(javaVersionConfig);
        assertEquals(EXPECTED_JAVA_VERSION, javaVersionConfig.getVersion());

    }
}
