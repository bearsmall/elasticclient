package com.xy.parser.maven;

import com.xy.parser.maven.item.JavaDependency;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MavenParser {
    private String MAVEN_CENTER_REMOTE = "http://central.maven.org/maven2/";
    private String MAVEN_CENTER_LOCAL = "D:\\cert\\";

    public static void main(String[] args) throws IOException, XmlPullParserException {
        List<JavaDependency> javaDependencyList = new MavenParser().getDependencyTree("D:\\cert\\pom.xml");
        System.out.println(javaDependencyList);
    }

    public List<JavaDependency> getDependencyTree(String pomPath) throws IOException{
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        List<JavaDependency> javaDependencies = null;
        try {
            model = reader.read(new FileReader(pomPath));
            javaDependencies = new ArrayList<JavaDependency>();
            for(Dependency dependency:model.getDependencies()){
                JavaDependency javaDependency = new JavaDependency(dependency.getGroupId(),dependency.getArtifactId(),dependency.getVersion()==null?model.getParent().getVersion():dependency.getVersion());
                javaDependency.setChildren(iterateChildren(javaDependency));
                javaDependencies.add(javaDependency);
            }
        } catch (XmlPullParserException e) {
            System.out.println(e);
        }
        return javaDependencies;
    }

    private List<JavaDependency> iterateChildren(JavaDependency javaDependency) throws IOException{
        String pomPath = getPomPath(javaDependency);
        String pomName = getPomName(javaDependency);
        File localFile = new File(MAVEN_CENTER_LOCAL+pomPath+pomName);
        if(!localFile.exists()) {
            URL url = new URL(MAVEN_CENTER_REMOTE + pomPath + pomName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
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
            }catch (Exception e){
                System.out.println(e);
                return null;
            }
        }
        System.out.println("info:"+pomName+" download success");
        return getDependencyTree(localFile.getAbsolutePath());
    }
    /**
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    private String getPomPath(JavaDependency javaDependency) {
        return javaDependency.getGroupId().replace('.','/')+"/"+javaDependency.getArtifactId().replace('.','/')+"/"+javaDependency.getVersion()+"/";
    }

    private String getPomName(JavaDependency javaDependency) {
        return javaDependency.getArtifactId().replace('.','/')+"-"+javaDependency.getVersion()+".pom";
    }

}
