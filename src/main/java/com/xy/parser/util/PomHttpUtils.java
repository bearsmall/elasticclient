package com.xy.parser.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PomHttpUtils {
    public static String[] MAVEN_CENTER_REMOTES = new String[]{
            "http://central.maven.org/maven2/"
//            "http://jcenter.bintray.com/",
//            "https://oss.sonatype.org/content/repositories/releases/",
//            "http://repo.spring.io/plugins-release/",
//            "http://repo.spring.io/libs-milestone/",
//            "http://repo.hortonworks.com/content/repositories/releases/",
//            "https://maven.atlassian.com/content/repositories/atlassian-public/",
//            "https://repository.jboss.org/nexus/content/repositories/releases/"
    };         //MAVEN中央仓库远程地址
    public static String MAVEN_CENTER_LOCAL = "D:\\cert\\";                                  //MAVEN中央本地POM文件存储路径（缓存）

    /**
     * 下载pom文件
     * @param pomPath pom的maven路径
     * @param pomName pom的文件名
     * @return
     * @throws IOException
     */
    public static File downloadFile(String pomPath, String pomName) throws IOException {
        File localFile = new File(MAVEN_CENTER_LOCAL+pomPath+pomName);
        boolean flag = false;
        String remote = null;
        if(!localFile.exists()) {
            for(String REMOTEURL:MAVEN_CENTER_REMOTES){
                remote = REMOTEURL;
                URL url = new URL(REMOTEURL + pomPath + pomName);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //设置超时间为3秒
                conn.setConnectTimeout(1000);
                //防止屏蔽程序抓取而返回403错误
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                InputStream inputStream = null;
                try {
                    //得到输入流
                    inputStream = conn.getInputStream();
                    //获取自己数组
                    byte[] getData = readInputStream(inputStream);
                    //文件保存位置
                    File saveDir = new File(MAVEN_CENTER_LOCAL + pomPath);
                    if (!saveDir.exists()) {
                        saveDir.mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(localFile);
                    fos.write(getData);
                    if (fos != null) {
                        fos.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    flag = true;
                    break;
                }catch (Exception e){
                    System.out.println(e+">>"+remote);
                }
            }
            if(flag) {
                System.out.println("info:" + pomName + " download success from:"+remote);
            }
        }
        return localFile;
    }

    public static File downloadFile(String fullPomName) throws IOException {
        File localFile = new File(MAVEN_CENTER_LOCAL+fullPomName);
        boolean flag = false;
        if(!localFile.exists()) {
            for(String REMOTEURL:MAVEN_CENTER_REMOTES) {
                URL url = new URL(MAVEN_CENTER_REMOTES + fullPomName);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //设置超时间为3秒
                conn.setConnectTimeout(1000);
                //防止屏蔽程序抓取而返回403错误
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                InputStream inputStream = null;
                try {
                    //得到输入流
                    inputStream = conn.getInputStream();
                    //获取自己数组
                    byte[] getData = readInputStream(inputStream);
                    //文件保存位置
                    File saveDir = localFile.getParentFile();
                    if (!saveDir.exists()) {
                        saveDir.mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(localFile);
                    fos.write(getData);
                    if (fos != null) {
                        fos.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    flag = true;
                    break;
                } catch (Exception e) {
                    System.out.println(e);
                    return null;
                }
            }
            if(flag) {
                System.out.println("info:" + fullPomName + " download success");
            }
        }
        return localFile;
    }

    /**
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    private static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

}
