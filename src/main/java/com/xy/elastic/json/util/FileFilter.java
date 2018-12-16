package com.xy.elastic.json.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FileFilter {
    private List<File> fileList = new LinkedList<File>();
    private String suffix = ".java";

    public FileFilter(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public List<File> getFileList() {
        return fileList;
    }

    public void setFileList(List<File> fileList) {
        this.fileList = fileList;
    }

    public void render(File root) throws IOException {
        List<File> fileList = new ArrayList<>();
        if(root.isDirectory()){
            File[] files = root.listFiles();
            for(File f:files){
                if(f.isDirectory()){
                    fileList.add(f);
                }else {
                    runFile(f);
                }
            }
        }else {
            runFile(root);
        }
        while (!fileList.isEmpty()){
            File file = fileList.remove(0);
            for (File f : file.listFiles()) {
                if(f.isDirectory()){
                    fileList.add(f);
                }else{
                    runFile(f);
                }
            }
        }
    }

    public void iteratorFile(File root) throws IOException {
        if(root.isDirectory()){
            File[] childrens = root.listFiles();
            for (File children:childrens){
                iteratorFile(children);
            }
        }else if (root.isFile()){
            runFile(root);
        }
    }

    public void runFile(File file) throws IOException {
        if(file.getPath().endsWith(suffix)){
            fileList.add(file);
        }
    }
}
