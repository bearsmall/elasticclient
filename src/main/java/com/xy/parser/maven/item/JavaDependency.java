package com.xy.parser.maven.item;

import java.util.List;

public class JavaDependency {
    private String groupId;
    private String artifactId;
    private String version;

    private JavaDependency parent;
    private List<JavaDependency> children;

    public JavaDependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public JavaDependency getParent() {
        return parent;
    }

    public void setParent(JavaDependency parent) {
        this.parent = parent;
    }

    public List<JavaDependency> getChildren() {
        return children;
    }

    public void setChildren(List<JavaDependency> children) {
        this.children = children;
    }
}
