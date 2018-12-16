package com.xy.parser.npm;

import com.alibaba.fastjson.JSON;
import com.xy.parser.npm.item.NpmDependency;
import com.xy.parser.npm.util.HttpUtils;

import java.util.*;


public class NpmParser {
    private String NPM_CENTER_REMOTE = "https://registry.npmjs.cf";  //npm远程API查询地址
    private String NPM_CENTER_LOCAL = "";   //TODO
    private List<NpmDependency> npmDependencyList = new ArrayList<>();
    private Set<NpmDependency> npmDependencySet = new HashSet<>();

    public List<NpmDependency> getNpmDependencyList() {
        return npmDependencyList;
    }

    public void setNpmDependencyList(List<NpmDependency> npmDependencyList) {
        this.npmDependencyList = npmDependencyList;
    }

    public Set<NpmDependency> getNpmDependencySet() {
        return npmDependencySet;
    }

    public void setNpmDependencySet(Set<NpmDependency> npmDependencySet) {
        this.npmDependencySet = npmDependencySet;
    }

    public void parse(String name, String version) {
        npmDependencyList = iterateChildren(name,version);
    }

    private List<NpmDependency> iterateChildren(String name_,String version_) {
        String json = HttpUtils.getJsonContent(NPM_CENTER_REMOTE+"/"+name_+"/"+version_);
        Map maps = (Map)JSON.parse(json);
        Map mapList = (Map) maps.get("dependencies");
        List<NpmDependency> dependencies = new ArrayList<NpmDependency>();
        if(mapList!=null) {
            for (Object key : mapList.keySet()) {
                String name = key.toString();
                String version = mapList.get(name).toString();
                NpmDependency npm = new NpmDependency(name, version);
                if (version.charAt(0) == '~' || version.charAt(0) == '^' || version.charAt(0) == '*') {
                    version = version.substring(1);
                }
                npm.setDependencies(iterateChildren(name, version));
                dependencies.add(npm);
                if(!npmDependencySet.contains(npm)){
                    npmDependencySet.add(npm);
                }
            }
        }
        return dependencies;
    }
}
