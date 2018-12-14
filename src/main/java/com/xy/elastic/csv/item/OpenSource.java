package com.xy.elastic.csv.item;

public class OpenSource {
    String cnvd;            //CNVD编号
    String cve;             //CVE编号
    String core;            //漏洞核心
    String date;            //发布日期
    String level;           //漏洞危险等级
    String version;         //受影响的软件及版本号
    String description;     //软件/组件基本描述
    String href;            //连接
    String current;        //漏洞目前状况
    String patch;          //漏洞补丁
    Integer type;           //软件类型（1：开源工具、2：开源基础软件、3：开源组件

    public OpenSource(String cnvd, String cve, String core, String date, String level, String version, String description, String href, String current, String patch, Integer type) {
        this.cnvd = cnvd;
        this.cve = cve;
        this.core = core;
        this.date = date;
        this.level = level;
        this.version = version;
        this.description = description;
        this.href = href;
        this.current = current;
        this.patch = patch;
        this.type = type;
    }

    public String getCnvd() {
        return cnvd;
    }

    public void setCnvd(String cnvd) {
        this.cnvd = cnvd;
    }

    public String getCve() {
        return cve;
    }

    public void setCve(String cve) {
        this.cve = cve;
    }

    public String getCore() {
        return core;
    }

    public void setCore(String core) {
        this.core = core;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getPatch() {
        return patch;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "OpenSource{" +
                "cnvd='" + cnvd + '\'' +
                ", cve='" + cve + '\'' +
                ", core='" + core + '\'' +
                ", date='" + date + '\'' +
                ", level='" + level + '\'' +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                ", href='" + href + '\'' +
                ", current='" + current + '\'' +
                ", patch='" + patch + '\'' +
                ", type=" + type +
                '}';
    }
}
