package com.xy.parser.maven;

import com.xy.parser.maven.item.JavaDependency;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class MavenParserTest {

    @Test
    public void test1() throws IOException {
        MavenParser mavenParser = new MavenParser();
        mavenParser.parse("D:\\cert\\pom.xml");
        List<JavaDependency> javaDependencyList = mavenParser.getJavaDependencyTree();
        Set<JavaDependency> javaDependencySet = mavenParser.getJavaDependencySet();
        System.out.println(javaDependencyList);
        System.out.println(javaDependencySet);
    }

}