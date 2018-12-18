package com.xy.parser.maven.enhance;

import org.junit.Test;

public class MavenLocalPomExtracterTest {

    @Test
    public void test(){
        MavenLocalPomExtracter localPomExtracter = new MavenLocalPomExtracter("E:\\.m2\\repository","D:\\cert");
        localPomExtracter.extract();
    }

}