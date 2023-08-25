package io.rapidw.utils.jarclassloader

import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class ClassLoaderSpec extends Specification {

    def "load class from jar"() {
        given:
        def classLoader = new JarClassLoader()
        classLoader.addJar("src/test/resources/test.jar")

        when:
        def clazz = classLoader.loadClass("io.rapidw.utils.jarclassloader.TestClass")

        then:
        clazz != null
    }
}
