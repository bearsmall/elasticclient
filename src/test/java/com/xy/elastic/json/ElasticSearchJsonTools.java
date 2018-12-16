package com.xy.elastic.json;
import com.alibaba.fastjson.JSONObject;
import com.xy.elastic.json.util.FileFilter;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ElasticSearchJsonTools {
    private String HOST = "10.103.245.220";      //ES所在HOST
    private Integer PORT = 9300;                //ES端口（9300是客户端端口，9200是HTTP端口）
    private String indexName = "cve";          //索引名
    private String indexType = "info";          //索引名
    TransportClient client;

    @Before
    @SuppressWarnings({ "unchecked" })
    public void before() throws UnknownHostException {
        Settings esSettings = Settings.builder()
                .put("cluster.name", "elasticsearch") //设置ES实例的名称
                .put("client.transport.sniff", true) //自动嗅探整个集群的状态，把集群中其他ES节点的ip添加到本地的客户端列表中
                .build();
        client = new PreBuiltTransportClient(Settings.EMPTY);//初始化client较老版本发生了变化，此方法有几个重载方法，初始化插件等。
        //此步骤添加IP，至少一个，其实一个就够了，因为添加了自动嗅探配置
        client.addTransportAddress(new TransportAddress(InetAddress.getByName(HOST), PORT));
    }

    @Test
    public void index() throws IOException {
        File root = new File("E:\\code\\CVE");
        Long start = System.currentTimeMillis();
        FileFilter fileFilter = new FileFilter(".json");
        fileFilter.render(root);
        List<File> fileList = fileFilter.getFileList();
        ExecutorService executorService = new ThreadPoolExecutor(4,8,60, TimeUnit.SECONDS,new LinkedBlockingDeque<>());
        int size = fileList.size();
        final CountDownLatch countDownLatch = new CountDownLatch(size);
        AtomicInteger atomicInteger = new AtomicInteger(0);
        for (File file:fileList){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Map cveMap = (Map) JSONObject.parse(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                        IndexResponse indexResponse = client.prepareIndex(indexName, indexType).setSource(cveMap).execute().actionGet();
                        System.out.println("id:" + indexResponse.getId());
                        atomicInteger.addAndGet((int) file.length());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Long end = System.currentTimeMillis();
        System.out.println(end-start);
        System.out.println("Integer:"+atomicInteger);
    }
}