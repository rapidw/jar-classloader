package io.rapidw.utils.jarclassloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarClassLoader extends ClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(JarClassLoader.class);

    private final ConcurrentHashMap<String, byte[]> classes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, URL> resources = new ConcurrentHashMap<>();

    public JarClassLoader(ClassLoader parent) {
        super(parent);
    }

    public void addJar(JarInputStream jarInputStream) throws IOException {
        parseJar(jarInputStream);
    }

    public void addClass(String name, byte[] data) {
        logger.debug("adding class \"{}\"", name);
        classes.putIfAbsent(name, data);
    }

    public void addResource(String name, byte[] data) {
        logger.debug("adding resource \"{}\"", name);
        try {
            URL url = new URL("inputstream", "", 0, name,
                    new Utils.InputStreamURLStreamHandler(new ByteArrayInputStream(data))) ;
            resources.put(name, url);
        } catch (MalformedURLException e) {
            logger.error("add resource error", e);
        }
    }

    @Override
    protected URL findResource(String name) {
        return resources.get(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return Collections.enumeration(Collections.singleton(findResource(name)));
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (classes.containsKey(name)) {
            byte[] bytes = classes.get(name);
            return defineClass(name, bytes, 0, bytes.length);
        } else throw new ClassNotFoundException();
    }



    // ----------------------------------------------------------------------

    private void parseJar(JarInputStream jarInputStream) throws IOException {

        JarEntry entry = jarInputStream.getNextJarEntry();
        while (entry != null) {
            String name = entry.getName();
            boolean isFile = !entry.isDirectory();
            boolean isClassFile = isFile && name.endsWith(".class");
            boolean isJarFile = isFile && name.endsWith(".jar");

            if (isClassFile) {
                String className = Utils.pathToClassName(name);
                byte[] classData = Utils.readCurrentJarEntry(jarInputStream);
                addClass(className, classData);
            } else if (isJarFile) {
                byte[] jarData = Utils.readCurrentJarEntry(jarInputStream);
                ByteArrayInputStream byteStream = new ByteArrayInputStream(jarData);
                JarInputStream jarStream = new JarInputStream(byteStream);
                parseJar(jarStream);
            } else if (isFile) {
                byte[] fileData = Utils.readCurrentJarEntry(jarInputStream);
                addResource(name, fileData);
            }
            entry = jarInputStream.getNextJarEntry();
        }
        jarInputStream.close();
    }

}
