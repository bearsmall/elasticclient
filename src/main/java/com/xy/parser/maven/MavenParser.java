package com.xy.parser.maven;

import com.xy.parser.maven.item.JavaDependency;
import com.xy.parser.util.PomHttpUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.*;

public class MavenParser {
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
                if(dependency.getScope()==null||dependency.getScope().equalsIgnoreCase("compile")||dependency.getScope().equalsIgnoreCase("runtime")) {
                    extractTrueParams(model,dependency);
                    String groupId = dependency.getGroupId();
                    String artifactId = dependency.getArtifactId();
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
        File localFile = PomHttpUtils.downloadFile(pomPath, pomName);
        if (localFile == null||!localFile.exists()){
            return null;
        }
        //递归解析子层级依赖树
       return getDependencyTree(exclusionSet,localFile.getAbsolutePath(),deep-1);
    }

    /**
     * 拼接当前Dependency对应的pom文件的maven形式路径（groupId/artifactId/version）
     * @param javaDependency
     * @return
     */
    private String getPomPath(JavaDependency javaDependency) {
        if(javaDependency.getGroupId()==null){
            return "";
        }
        return javaDependency.getGroupId().replace('.','/')+"/"+javaDependency.getArtifactId()+"/"+javaDependency.getVersion()+"/";
    }

    /**
     * 拼接当前Dependency对应的pom文件的maven形式文件名（artifactId-version.pom）
     * @param javaDependency
     * @return
     */
    private String getPomName(JavaDependency javaDependency) {
        return javaDependency.getArtifactId()+"-"+javaDependency.getVersion()+".pom";
    }

}
