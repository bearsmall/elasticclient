package com.xy.parser.npm.item;

import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NpmDependency that = (NpmDependency) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }
}
