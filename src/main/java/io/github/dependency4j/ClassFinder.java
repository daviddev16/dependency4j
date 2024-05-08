package io.github.dependency4j;

import io.github.dependency4j.exception.ScanFailedException;
import io.github.dependency4j.util.Checks;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.lang.String.*;
import static java.lang.String.format;

/**
 *
 * Java and Kotlin ClassFinder
 *
 * @since 1.0.5
 **/
public class ClassFinder {

    private static final String CLASS_SUFFIX = ".class";

    public static Set<Class<?>> scanPackages(ClassLoader classLoader, String parentPackageName) {
        Checks.nonNull(parentPackageName, "The parent package name can not be null.");
        Checks.nonNull(classLoader, "Could not scan \"" + parentPackageName + "\" with a null classLoader.");
        Set<Class<?>> classes = new HashSet<>();
        String relPath = parentPackageName.replace('.', '/');
        try {
            Enumeration<URL> resources = classLoader.getResources(relPath);
            if (!resources.hasMoreElements()) {
                String err = "Unexpected problem: No resource for {%s}";
                throw new ScanFailedException(format(err, relPath));
            } else {
                do {
                    URL resource = resources.nextElement();
                    if (resource.toString().startsWith("jar:")) {
                        classes.addAll(processJarFile(resource, parentPackageName));
                    } else {
                        File dir = new File(resource.getPath());
                        classes.addAll(processDirectory(dir, parentPackageName));
                    }
                } while (resources.hasMoreElements());
            }
            return classes;
        } catch (IOException e) {
            String err = "Unexpected error loading resources";
            throw new RuntimeException(err, e);
        }
    }

    public static List<Class<?>> processDirectory(File directory, String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        for(String file : directory.list()) {
            String cls;
            if (file.endsWith(CLASS_SUFFIX)) {
                cls = packageName + '.' + file.substring(0, file.length() - 6);
                classes.add(loadClass(cls));
            }
            File subdir = new File(directory, file);
            if (subdir.isDirectory()) {
                classes.addAll(processDirectory(subdir, packageName + '.' + file));
            }
        }
        return classes;
    }

    public static List<Class<?>> processJarFile(URL resource, String pkgname) {
        List<Class<?>> classes = new ArrayList<>();
        String relPath = pkgname.replace('.', '/');
        String resPath = resource.getPath();
        String jarPath = resPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                String entryName = entry.getName();
                String className = null;

                if (entryName.endsWith(CLASS_SUFFIX) && entryName.startsWith(relPath)
                        && entryName.length() > (relPath.length() + "/".length())) {
                    className = entryName.replace('/', '.').replace('\\', '.').replace(CLASS_SUFFIX, "");
                }

                if (className != null) {
                    classes.add(loadClass(className));
                }
            }
        } catch (IOException e) {
            String err = "Unexpected IOException reading JAR File [%s]";
            throw new ScanFailedException(format(err, jarPath), e);
        }
        return classes;
    }

    private static Class<?> loadClass(String cls) {
        try {
            return Class.forName(cls);
        }
        catch (ClassNotFoundException e) {
            throw new ScanFailedException(format("Unexpected ClassNotFoundException " +
                    "loading class [%s]", cls), e);
        }
    }

}


