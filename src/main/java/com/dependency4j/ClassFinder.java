package com.dependency4j;

import com.dependency4j.util.Checks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

public class ClassFinder {

    public static Set<Class<?>> scanPackage(String parentPackageName)
            throws IOException, ClassNotFoundException {
        return scanPackage(parentPackageName, true);
    }

    public static Set<Class<?>> scanPackage(String parentPackageName, boolean scanSubPackages)
            throws IOException, ClassNotFoundException {

        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        return scanPackage(systemClassLoader, parentPackageName, scanSubPackages);
    }

    public static Set<Class<?>> scanPackage(ClassLoader classLoader, String parentPackageName,
                                            boolean scanSubPackages)
            throws IOException, ClassNotFoundException {

        Set<Class<?>> classSet = new HashSet<>();
        scanPackagesRecursively(classLoader, parentPackageName, classSet, scanSubPackages);
        return classSet;
    }

    private static void scanPackagesRecursively(ClassLoader classLoader, String parentPackageName,
                                                Set<Class<?>> classSet, boolean scanSubPackages)
            throws IOException, ClassNotFoundException {

        Checks.nonNull(parentPackageName, "The parent package name can not be null.");
        Checks.nonNull(classLoader, "Could not scan \"" + parentPackageName + "\" with a null classLoader.");
        parentPackageName = parentPackageName.replaceAll("[.]", "/");

        try (InputStream pkgInputStream = classLoader.getResourceAsStream(parentPackageName)) {

            Checks.nonNull(pkgInputStream, "Could not load \"" + parentPackageName + "\" InputStream.");

            BufferedReader reader = new BufferedReader(new InputStreamReader(pkgInputStream));
            String packageLine;

            while ((packageLine = reader.readLine()) != null) {

                if (packageLine.endsWith(".class"))
                    classSet.add(getClassOf(packageLine, parentPackageName));

                else if (scanSubPackages)
                    scanPackagesRecursively(classLoader,
                            format("%s.%s", parentPackageName, packageLine), classSet, true);

            }

            reader.close();
        }
    }

    private static Class<?> getClassOf(String className, String packageName) throws ClassNotFoundException {
            return Class.forName(packageName.replaceAll("/", ".") + "."
                    + className.substring(0, className.lastIndexOf('.')));
    }

}


