package com.xy.parser.maven;

import com.xy.parser.maven.item.JavaDependency;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MavenParser {
    private String MAVEN_CENTER_REMOTE = "http://central.maven.org/maven2/";         //MAVEN中央仓库远程地址
    private String MAVEN_CENTER_LOCAL = "D:\\cert\\";                                  //MAVEN中央本地POM文件存储路径（缓存）
    private String pomPath;                                                              //pom.xml文件路径
    private Set<JavaDependency> javaDependencySet = new HashSet<>();                    //解析结果树（省略根节点）
    private List<JavaDependency> javaDependencyTree = new ArrayList<>();                        //解析结果集合
    private int deepDefault = 8;                                                        //递归最大深度

    public MavenParser() {
    }

    public MavenParser(String pomPath, int deep) {
        this.pomPath = pomPath;
        this.deepDefault = deep;
    }

    public MavenParser(String pomPath) {
        this.pomPath = pomPath;
    }

    public String getPomPath() {
        return pomPath;
    }

    public void setPomPath(String pomPath) {
        this.pomPath = pomPath;
    }

    public int getDeepDefault() {
        return deepDefault;
    }

    public void setDeepDefault(int deepDefault) {
        this.deepDefault = deepDefault;
    }

    public Set<JavaDependency> getJavaDependencySet() {
        return javaDependencySet;
    }

    public void setJavaDependencySet(Set<JavaDependency> javaDependencySet) {
        this.javaDependencySet = javaDependencySet;
    }

    public List<JavaDependency> getJavaDependencyTree() {
        return javaDependencyTree;
    }

    public void setJavaDependencyTree(List<JavaDependency> javaDependencyTree) {
        this.javaDependencyTree = javaDependencyTree;
    }


    public void parse(String pomPath){
        try {
            javaDependencyTree = getDependencyTree(pomPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parse(Set exclusionSet, String pomPath, int deep){
        try {
            javaDependencyTree = getDependencyTree(exclusionSet,pomPath,deep);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<JavaDependency> getDependencyTree(String pomPath) throws IOException{
        return getDependencyTree(null,pomPath,deepDefault);
    }

    /**
     * 获得pom.xml文件的依赖树【递归以获取深层级依赖】
     * @param exclusionSet 不包含的组件集合（字符形存储格式：groupId$artifactId）
     * @param pomPath  pom.xml文件本地路径
     * @param deep  距离触底还剩的深度/距离
     * @return 依赖树
     * @throws IOException
     */
    private List<JavaDependency> getDependencyTree(Set exclusionSet, String pomPath, int deep) throws IOException{
        if(deep<=0){
            return null;
        }
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        List<JavaDependency> javaDependencies = null;
        try {
            model = reader.read(new FileReader(pomPath));
            javaDependencies = new ArrayList<JavaDependency>();
            for(Dependency dependency:model.getDependencies()){
                if("true".equals(dependency.getOptional())){
                    continue;
                }
                if(dependency.getScope()==null||dependency.getScope().equalsIgnoreCase("compile")) {
                    extractTrueParams(model,dependency);
                    String groupId = dependency.getGroupId().trim();
                    String artifactId = dependency.getArtifactId().trim();
                    String version = dependency.getVersion();
                    if(exclusionSet!=null&&exclusionSet.contains(groupId+"$"+artifactId)){
                        continue;
                    }
                    if(version==null){
                        System.out.println("empty version!");
                    }
                    JavaDependency javaDependency = new JavaDependency(groupId,artifactId,version);
                    if (!javaDependencySet.contains(javaDependency)) {
                        javaDependencySet.add(javaDependency);
                        List<Exclusion> exclusionList = dependency.getExclusions();
                        Set<String> set =null;
                        if(exclusionList!=null){
                            set = new HashSet<>();
                            for(Exclusion exclusion:exclusionList){
                                set.add(exclusion.getGroupId()+"$"+exclusion.getArtifactId());
                            }
                        }
                        javaDependency.setChildren(iterateChildren(set,javaDependency, deep));
                        javaDependencies.add(javaDependency);
                    }
                }
            }
        } catch (XmlPullParserException e) {
            System.out.println(e);
        }
        return javaDependencies;
    }

    /**
     * 填充maven中${}形式的参数
     * @param model pom文件所对应的mavenModel对象
     * @param dependency 当前解析的dependency对象
     */
    private void extractTrueParams(Model model, Dependency dependency) {
        String groupId = dependency.getGroupId().trim();
        String artifactId = dependency.getArtifactId().trim();
        String version = dependency.getVersion();
        if(artifactId.equals("${project.artifactId}")){
            artifactId = model.getArtifactId();
            if(artifactId == null){
                artifactId = model.getParent().getArtifactId();
            }
        }
        if(groupId.equals("${project.groupId}")){
            groupId = model.getGroupId();
            if(groupId == null){
                groupId = model.getParent().getGroupId();
            }
        }
        if(version==null||version.equals("${project.version}")){
            version = model.getVersion();
            if(version == null){
                version = model.getParent().getVersion();
            }
        }
        if(groupId.startsWith("${")){
            groupId = model.getProperties().getProperty(groupId.substring(2,groupId.length()-1).trim());
        }
        if(artifactId.startsWith("${")){
            artifactId = model.getProperties().getProperty(artifactId.substring(2,artifactId.length()-1).trim());
        }
        if(version.startsWith("${")){
            version = model.getProperties().getProperty(version.substring(2,version.length()-1).trim());
        }
        dependency.setArtifactId(artifactId);
        dependency.setGroupId(groupId);
        dependency.setVersion(version);
    }

    /**
     * 获取当前dependency的子依赖
     * @param exclusionSet  不包含在内的组件集合（字符形存储格式：groupId$artifactId）
     * @param javaDependency  当前的Dependency解析对象（用来拼接路径）
     * @param deep  距离触底还剩的深度/距离
     * @return 当前dependency的子依赖树
     * @throws IOException
     */
    private List<JavaDependency> iterateChildren(Set exclusionSet,JavaDependency javaDependency,int deep) throws IOException{
        String pomPath = getPomPath(javaDependency);
        String pomName = getPomName(javaDependency);
        File localFile = new File(MAVEN_CENTER_LOCAL+pomPath+pomName);
        if(!localFile.exists()) {
            URL url = new URL(MAVEN_CENTER_REMOTE + pomPath + pomName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(100);
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
            System.out.println("info:"+pomName+" download success");
        }
        //递归解析子层级依赖树
       return getDependencyTree(exclusionSet,localFile.getAbsolutePath(),deep-1);
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

    /**
     * 拼接当前Dependency对应的pom文件的maven形式路径（groupId/artifactId/version）
     * @param javaDependency
     * @return
     */
    private String getPomPath(JavaDependency javaDependency) {
        return javaDependency.getGroupId().replace('.','/')+"/"+javaDependency.getArtifactId().replace('.','/')+"/"+javaDependency.getVersion()+"/";
    }

    /**
     * 拼接当前Dependency对应的pom文件的maven形式文件名（artifactId-version.pom）
     * @param javaDependency
     * @return
     */
    private String getPomName(JavaDependency javaDependency) {
        return javaDependency.getArtifactId().replace('.','/')+"-"+javaDependency.getVersion()+".pom";
    }

}
