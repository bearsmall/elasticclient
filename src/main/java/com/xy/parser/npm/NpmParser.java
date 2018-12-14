package com.xy.parser.npm;

import com.alibaba.fastjson.JSON;
import com.xy.parser.npm.item.NpmDependency;
import com.xy.parser.npm.util.HttpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class NpmParser {


    public static void main(String[] args) {
        List<NpmDependency> dependencies = iterateChildren("mysql","2.16.0");
        System.out.println(dependencies);
    }

    private static List<NpmDependency> iterateChildren(String name_,String version_) {
        String json = HttpUtil.getJsonContent("https://registry.npmjs.cf/"+name_+"/"+version_);
        Map maps = (Map)JSON.parse(json);
        Map mapList = (Map) maps.get("dependencies");
        List<NpmDependency> dependencies = new ArrayList<NpmDependency>();
        if(mapList!=null) {
            for (Object key : mapList.keySet()) {
                String name = key.toString();
                String version = mapList.get(name).toString();
                NpmDependency npm = new NpmDependency(name, version);
                if (version.charAt(0) == '~' || version.charAt(0) == '^' || version.charAt(0) == '*') {
                    npm.setDependencies(iterateChildren(name, version.substring(1)));
                } else {
                    npm.setDependencies(iterateChildren(name, version));
                }
                dependencies.add(npm);
            }
        }
        return dependencies;
    }
}
