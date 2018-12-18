package com.xy.parser.maven.enhance;

import org.junit.Test;

public class MavenParserJobTest {
    private static String fromPath = "E:\\.m2\\repository";
    private static String toPath = "D:\\cert";

    @Test
    public void start() throws InterruptedException {
        MavenParserJob.start("D:\\Users\\bearsmalll\\IdeaProjects");
    }

}