package com.xy.parser.maven.enhance;

import com.xy.parser.maven.MavenParser;

import java.io.File;

public class MavenParserJob {

    public static void start(String base) throws InterruptedException {
        while (true){
            runTask(base);
            Thread.sleep(1000*60*60);
        }
    }

    private static void runTask(String base) {
        iterateTask(new File(base));
    }

    private static void iterateTask(File file){
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(File f: files){
                iterateTask(f);
            }
        }else if(file.isFile()){
            String filePath = file.getAbsolutePath();
            if(filePath.endsWith("pom.xml")||filePath.endsWith("pom")) {
                System.out.println(filePath);
                new MavenParser().parse(filePath);
            }
        }
    }
}