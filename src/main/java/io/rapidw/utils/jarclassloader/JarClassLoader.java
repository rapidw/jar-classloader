/**
 * Copyright 2023 Rapidw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
