package com.xy.parser.maven;

import com.xy.parser.maven.item.JavaDependency;
import com.xy.parser.util.PomHttpUtils;
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.*;

public class MavenParser {
    private List<String> pomPaths = new ArrayList<>();                                   //项目包含的pom.xml文件列表【可能不止一个pom文件】
    private Set<JavaDependency> javaDependencySet = new HashSet<>();                    //解析结果树（省略根节点）
    private Map<String,JavaDependency> itemMap = new HashMap<>();                          //JavaDependency对应关系
    private Map<String,List<JavaDependency>> javaDependencyTree = new HashMap<>();      //解析结果集合
    private int deepDefault = 8;                                                         //递归最大深度
    private Properties properties = new Properties();                                    //可变参数
    private Map<String,String> versionMap = new HashMap();                               //可变参数

    public MavenParser() {
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

    public Map<String, List<JavaDependency>> getJavaDependencyTree() {
        return javaDependencyTree;
    }

    public void setJavaDependencyTree(Map<String, List<JavaDependency>> javaDependencyTree) {
        this.javaDependencyTree = javaDependencyTree;
    }

    public void parse(String projectPath){
        iteratePomFile(new File(projectPath));
        for(String pomPath:pomPaths) {
            try {
                javaDependencyTree.put(pomPath,getDependencyTree(pomPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void parse(Set exclusionSet, String projectPath, int deep){
        iteratePomFile(new File(projectPath));
        for(String pomPath:pomPaths) {
            try {
                javaDependencyTree.put(pomPath,getDependencyTree(exclusionSet, pomPath, deep));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void iteratePomFile(File file){
        if(file==null||!file.exists()){
            return;
        }
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(File f: files){
                iteratePomFile(f);
            }
        }else if(file.isFile()){
            String fileName = file.getName();
            if(fileName.equals("pom.xml")) {
                pomPaths.add(file.getAbsolutePath());
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = null;
                try {
                    model = reader.read(new FileReader(file.getAbsoluteFile()));
                    Properties pro =  model.getProperties();
                    properties.putAll(pro);
                    DependencyManagement dependencyManagement = model.getDependencyManagement();
                    if(dependencyManagement!=null){
                        for(Dependency dependency:dependencyManagement.getDependencies()){
                            extractTrueParams(model,dependency);
                            versionMap.put(dependency.getGroupId()+"."+dependency.getArtifactId()+".version",dependency.getVersion());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private List<JavaDependency> getDependencyTree(String pomPath) throws IOException{
        return getDependencyTree(null,pomPath,deepDefault);
    }

    /**
     * 递归解析可变参数
     * @param pomPath
     * @param deep
     * @return
     */
    private Properties extractProperties(Set exclusionSet,String pomPath,int deep) {
        Properties ps = new Properties();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        try {
            model = reader.read(new FileReader(pomPath));
            Properties properties =  model.getProperties();
            if(properties!=null&&properties.size()>0&&deep>0) {
                ps.putAll(properties);
            }
            DependencyManagement dependencyManagement = model.getDependencyManagement();
            if(dependencyManagement!=null){
                for(Dependency dependency:dependencyManagement.getDependencies()){
                    versionMap.put(dependency.getGroupId()+"."+dependency.getArtifactId()+".version",dependency.getVersion());
                }
            }
            Parent parent = model.getParent();
            if(deep>0){//第一层中有Parent的情况需要特殊考虑（说明是多模块的项目）【在项目本身中包含，不在maven中央仓库中】放到最初从项目目录中提取pom文件时进行
                // 下面是非第一层中的parent标签
                if (parent != null&&parent.getGroupId()!=null&&parent.getArtifactId()!=null&&parent.getVersion()!=null) {
                    Dependency dependency = new Dependency();
                    dependency.setGroupId(parent.getGroupId());
                    dependency.setArtifactId(parent.getArtifactId());
                    dependency.setVersion(parent.getVersion());
                    extractTrueParams(model,dependency);
                    JavaDependency javaDependency = new JavaDependency(dependency.getGroupId(),dependency.getArtifactId(),dependency.getVersion());
                    String path = getPomPath(javaDependency);
                    String name = getPomName(javaDependency);
                    File localFile = PomHttpUtils.downloadFile(path, name);
                    if (localFile == null || !localFile.exists()) {
                        return null;
                    }
                    Properties pro = extractProperties(null,localFile.getAbsolutePath(),++deep);
                    if(pro!=null&&pro.size()>0) {
                        ps.putAll(pro);
                    }
                }
            }
            for(Dependency d: model.getDependencies()){
                if(d.getGroupId()!=null&&d.getArtifactId()!=null&&d.getVersion()!=null) {
                    extractTrueParams(model,d);
                    if(exclusionSet!=null&&exclusionSet.contains(d.getGroupId()+"$"+d.getArtifactId())){
                        continue;
                    }
                    JavaDependency jd = new JavaDependency(d.getGroupId(), d.getArtifactId(), d.getVersion());
                    String path = getPomPath(jd);
                    String name = getPomName(jd);
                    File localFile = PomHttpUtils.downloadFile(path, name);
                    if (localFile == null || !localFile.exists()) {
                        return null;
                    }
                    if (!javaDependencySet.contains(jd)) {
                        javaDependencySet.add(jd);
                        itemMap.put(jd.getGroupId()+"$"+jd.getArtifactId()+"$"+jd.getGroupId(),jd);
                        List<Exclusion> exclusionList = d.getExclusions();
                        Set<String> set = null;
                        if (exclusionList != null) {
                            set = new HashSet<>();
                            for (Exclusion exclusion : exclusionList) {
                                set.add(exclusion.getGroupId() + "$" + exclusion.getArtifactId());
                            }
                        }
                        Properties pro = extractProperties(set, localFile.getAbsolutePath(), ++deep);
                        if (pro != null && pro.size() > 0) {
                            ps.putAll(pro);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return ps;
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
            Properties pro =  model.getProperties();
            if(pro!=null&&pro.size()>0&&deep>0) {
                properties.putAll(pro);
            }
            DependencyManagement dependencyManagement = model.getDependencyManagement();
            if(dependencyManagement!=null){
                for(Dependency dependency:dependencyManagement.getDependencies()){
                    versionMap.put(dependency.getGroupId()+"."+dependency.getArtifactId()+".version",dependency.getVersion());
                }
            }
            Parent parent = model.getParent();
            if (parent != null&&parent.getGroupId()!=null&&parent.getArtifactId()!=null&&parent.getVersion()!=null) {
                Dependency dependency = new Dependency();
                dependency.setGroupId(parent.getGroupId());
                dependency.setArtifactId(parent.getArtifactId());
                dependency.setVersion(parent.getVersion());
                extractTrueParams(model,dependency);
                JavaDependency javaDependency = new JavaDependency(dependency.getGroupId(),dependency.getArtifactId(),dependency.getVersion());
                iterateChildren(null,javaDependency,deep);
            }
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
                        itemMap.put(javaDependency.getGroupId()+"$"+javaDependency.getArtifactId()+"$"+javaDependency.getGroupId(),javaDependency);
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
                    }else {
                        javaDependency =  itemMap.get(javaDependency.getGroupId()+"$"+javaDependency.getArtifactId()+"$"+javaDependency.getGroupId());
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
        if(version==null){
            if(properties!=null){
                version = properties.getProperty(artifactId+".version");
            }
        }
        if(artifactId.equals("javacc")){
            System.out.println("hello");
        }
        if(version==null){
            if(versionMap!=null) {
                version = versionMap.get(groupId + "." + artifactId + ".version");
            }
        }
        if(version==null||version.equals("${project.version}")){
            version = model.getVersion();
            if(version == null){
                version = model.getParent().getVersion();
            }
        }
        if(groupId.startsWith("${")){
            groupId = properties.getProperty(groupId.substring(2,groupId.length()-1).trim());
        }
        if(artifactId.startsWith("${")){
            artifactId = properties.getProperty(artifactId.substring(2,artifactId.length()-1).trim());
        }
        if(version.startsWith("${")){
            version = properties.getProperty(version.substring(2,version.length()-1).trim());
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
