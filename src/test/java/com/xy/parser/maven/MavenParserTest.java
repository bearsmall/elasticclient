package com.xy.parser.maven;

import com.xy.parser.maven.item.JavaDependency;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;


public class MavenParserTest {

    @Test
    public void test1() throws IOException {
        Long start = System.currentTimeMillis();
        MavenParser mavenParser = new MavenParser();
        mavenParser.parse("D:\\Users\\pom.xml");
        Long end = System.currentTimeMillis();
        System.out.println(end-start);
        List<JavaDependency> javaDependencyList = mavenParser.getJavaDependencyTree();
        Set<JavaDependency> javaDependencySet = mavenParser.getJavaDependencySet();
        System.out.println(javaDependencyList);
        System.out.println(javaDependencySet);
    }

}