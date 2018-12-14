package com.xy.parser.npm.item;

import java.util.List;

public class NpmDependency {
    private String name;
    private String version;
    private List<NpmDependency> dependencies;

    public NpmDependency(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<NpmDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<NpmDependency> dependencies) {
        this.dependencies = dependencies;
    }
}
