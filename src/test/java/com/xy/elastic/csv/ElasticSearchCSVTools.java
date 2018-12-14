package com.xy.elastic.csv;
import com.csvreader.CsvReader;
import com.xy.elastic.csv.item.OpenSource;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticSearchCSVTools {
    private String HOST = "10.103.245.220";      //ES所在HOST
    private Integer PORT = 9300;                //ES端口（9300是客户端端口，9200是HTTP端口）
    private String indexName = "snyk";          //索引名
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
    public void index() throws Exception {
        String openSourceTool = ElasticSearchCSVTools.class.getClassLoader().getResource("openSourceTool.csv").getPath();
        String openSourceBaseSoft = ElasticSearchCSVTools.class.getClassLoader().getResource("openSourceBaseSoft.csv").getPath();
        String openSourceComponent = ElasticSearchCSVTools.class.getClassLoader().getResource("openSourceComponent.csv").getPath();

        indexOpenSource(openSourceTool,1);//开源工具
        indexOpenSource(openSourceBaseSoft,2);//开源基础软件
        indexOpenSource(openSourceComponent,3);//开源组件
    }

    public void indexOpenSource(String filePath,Integer type) throws Exception {
        List<OpenSource> openSourceList = readOpenSource(filePath,type);
        for (OpenSource openSource :openSourceList) {
            Map<String, Object> infoMap = new HashMap<String, Object>();
            infoMap.put("cnvd", openSource.getCnvd());
            infoMap.put("cve", openSource.getCve());
            infoMap.put("core", openSource.getCore());
            infoMap.put("date", openSource.getDate());
            infoMap.put("level", openSource.getLevel());
            infoMap.put("version", openSource.getVersion());
            infoMap.put("description", openSource.getDescription());
            infoMap.put("href", openSource.getHref());
            infoMap.put("current", openSource.getCurrent());
            infoMap.put("patch", openSource.getPatch());
            infoMap.put("type", openSource.getType());
            IndexResponse indexResponse = client.prepareIndex(indexName, indexType).setSource(infoMap).execute().actionGet();
            System.out.println("id:" + indexResponse.getId());
        }
        System.out.println("finished!");
    }


    public List<OpenSource> readOpenSource(String csvFilePath, Integer type) throws IOException {
        ArrayList<String[]> csvFileList = new ArrayList<String[]>();
        CsvReader csvReader = new CsvReader(csvFilePath,',', Charset.forName("utf-8"));
        csvReader.readHeaders();
        while (csvReader.readRecord()){
            csvFileList.add(csvReader.getValues());
        }
        csvReader.close();
        ArrayList<OpenSource> openSourceArrayList = new ArrayList<OpenSource>();

        for(int row=0;row<csvFileList.size();row++){
            String cnvd = csvFileList.get(row)[0];
            String cve = csvFileList.get(row)[1];
            String core = csvFileList.get(row)[2];
            String date = csvFileList.get(row)[3];
            String level = csvFileList.get(row)[4];
            String version = csvFileList.get(row)[5];
            String description = csvFileList.get(row)[6];
            String href = csvFileList.get(row)[7];
            String current = csvFileList.get(row)[8];
            String reason = csvFileList.get(row)[9];
            OpenSource openSource = new OpenSource(cnvd,cve,core,date,level,version,description,href,current,reason,type);
            openSourceArrayList.add(openSource);
        }
        return openSourceArrayList;
    }
}