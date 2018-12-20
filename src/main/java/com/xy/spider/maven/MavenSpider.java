package com.xy.spider.maven;

import com.xy.jdbc.DBUtil;
import com.xy.parser.util.PomHttpUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class MavenSpider {

    private static String basePath = "org/springframework/cloud/";


    public static void main(String[] args) {
        iteratePath(basePath);
    }

    private static void iteratePath(String basePath) {
        String page = sendGet(PomHttpUtils.MAVEN_CENTER_REMOTES[0]+basePath);
        Document doc = Jsoup.parse(page);   //得到document对象
        Elements elements = doc.select("#contents");
        if(elements==null){
            return;
        }
        Element feedlist = elements.get(0); // 获取父级元素
        Elements alist = feedlist.select("a");
        List<String> hrefs = alist.eachAttr("href");
        for(int i=1;i<hrefs.size();i++){
            String h = hrefs.get(i);
            if(h.endsWith(".pom")){
                String fullPomName = basePath+h;
                boolean flag = DBUtil.exist(fullPomName);
                if(!flag){
                    DBUtil.insert(fullPomName,h);
                }
            }else if(h.endsWith("/")){
                iteratePath(basePath+h);
            }
        }
    }


    public static String sendGet(String url) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String content = null;
        try {
            HttpGet get = new HttpGet(url);
            response = httpclient.execute(get);
            HttpEntity entity = response.getEntity();
            content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return content;
    }
}
