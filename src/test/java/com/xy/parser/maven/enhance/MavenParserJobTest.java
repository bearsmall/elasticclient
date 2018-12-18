package com.xy.parser.maven.enhance;

import org.junit.Test;

import static org.junit.Assert.*;

public class MavenParserJobTest {

    @Test
    public void start() throws InterruptedException {
        MavenParserJob.start("D:\\Users\\bearsmalll\\IdeaProjects");
    }
}