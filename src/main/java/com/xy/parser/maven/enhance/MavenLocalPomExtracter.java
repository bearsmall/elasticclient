package com.xy.parser.maven.enhance;

import com.xy.parser.util.FileUtils;

import java.io.File;
import java.io.IOException;

public class MavenLocalPomExtracter {
    private String fromPath;
    private String toPath;

    public MavenLocalPomExtracter(String fromPath, String toPath) {
        this.fromPath = fromPath;
        this.toPath = toPath;
    }

    public String getFromPath() {
        return fromPath;
    }

    public void setFromPath(String fromPath) {
        this.fromPath = fromPath;
    }

    public String getToPath() {
        return toPath;
    }

    public void setToPath(String toPath) {
        this.toPath = toPath;
    }

    public void extract(){
        try {
            iterateTask(new File(fromPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void iterateTask(File file) throws IOException {
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(File f: files){
                iterateTask(f);
            }
        }else if(file.isFile()){
            String filePath = file.getAbsolutePath();
            if(filePath.endsWith("pom.xml")||filePath.endsWith("pom")) {
                String from = filePath;
                String to = toPath+filePath.substring(fromPath.length());
                File f = new File(to);
                if(!f.exists()) {
                    f.getParentFile().mkdirs();
                    FileUtils.write(from, to);
                }
            }
        }
    }
}