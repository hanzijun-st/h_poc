package com.qianlima.offline.service;


import com.qianlima.offline.bean.Area;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.middleground.BaiLianZhongTaiService;
import com.qianlima.offline.middleground.NewZhongTaiService;
import com.qianlima.offline.middleground.NotBaiLianZhongTaiService;
import com.qianlima.offline.middleground.ZhongTaiService;
import com.qianlima.offline.rule02.NewRuleUtils;
import com.qianlima.offline.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class PocTestService {

    @Autowired
    private ContentSolr contentSolr;

    @Autowired
    private IctContentSolr ictContentSolr;

    @Autowired
    private QYHYContentSolr qyhyContentSolr;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    private NotBaiLianZhongTaiService notBaiLianZhongTaiService;

    @Autowired
    private NewZhongTaiService newZhongTaiService;

    @Autowired
    private ZhongTaiBiaoDiWuService zhongTaiBiaoDiWuService;

    @Autowired
    private ZhongTaiService zhongTaiService;

    @Autowired
    private BaiLianZhongTaiService baiLianZhongTaiService;


    HashMap<Integer, Area> areaMap = new HashMap<>();

    //地区
    @PostConstruct
    public void init() {
        List<Map<String, Object>> maps = gwJdbcTemplate.queryForList("SELECT * FROM phpcms_area");
        for (Map<String, Object> map : maps) {
            Area area = new Area();
            area.setAreaid(Integer.valueOf(map.get("areaid").toString()));
            area.setName(map.get("name").toString());
            area.setParentid(map.get("parentid").toString());
            area.setArrparentid(map.get("arrparentid").toString());
            areaMap.put(Integer.valueOf(map.get("areaid").toString()), area);
        }
    }

    String ids[] = {"182001854","182296542","191670686","181982147","191595470","192746693","183769738","191670418","191670593"};

    //调取中台数据
    public void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            String contentInfo = resultMap.get("content").toString();
            String content = processAboutContent(contentInfo);
            if (StringUtils.isNotBlank(content)) {
                resultMap.put("content", content);
            }
            newZhongTaiService.saveIntoMysql(resultMap);
        }
    }

    // 只去除<a>标签所有信息
    public static String processAboutContent(String content) {
        Document document = Jsoup.parse(content);
        Elements elements = document.select("a[href]");
        Integer elementSize = elements.size();
        for (Integer i = 0; i < elementSize; i++) {
            Element element = elements.get(i);
            if (element == null || document.select("a[href]") == null || document.select("a[href]").size() == 0) {
                break;
            }
            if (StringUtils.isNotBlank(element.attr("href"))) {
                if (element.is("a")) {
                    element.remove();
                }
            }
        }
        return document.body().html();
    }


    //调取中台数据 多个关键词后追加
    public void getDataFromZhongTaiAndSave3(NoticeMQ noticeMQ) throws Exception {

        String[] ccp1 = {"IOT","PAD","存储","多云","互联网+","机房","基站","集群","可视化","区块链","视联网","视频","手机","数仓","数据","数字","天网","天眼","通信","通讯","网络","物联网","系统","信号","信息化","云捕","云端","云防","云呼","云计算","云盘","云网","云效","云眼","云政","云资源","智慧","智库","智能"};
        List<String> ccp2 = LogUtils.readRule("smf");
        List<String> blacks = LogUtils.readRule("moneyFile");

        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";
            String content = resultMap.get("content") != null ? resultMap.get("content").toString() : "";
            content = title + "&" + content;
            content = content.toUpperCase();
            String titlekeyword = "";
            String contentkeyword = "";
            boolean flag = true;
            for (String black : blacks) {
                if(StringUtils.isNotBlank(title) && title.contains(black)){
                    flag = false;
                    break;
                }
            }
            if (flag){
                for (String cp1 : ccp1) {
                    if (title.toUpperCase().contains(cp1.toUpperCase())) {
                        titlekeyword += (cp1 + "、");
                    }
                }
                for (String cp2 : ccp2) {
                    if (content.contains(cp2.toUpperCase())) {
                        contentkeyword += (cp2 + "、");
                    }
                }
            }
            if (StringUtils.isNotBlank(titlekeyword)) {
                titlekeyword = titlekeyword.substring(0, titlekeyword.length() - 1);
                resultMap.put("keyword", titlekeyword);
            }
            if (StringUtils.isNotBlank(contentkeyword)) {
                contentkeyword = contentkeyword.substring(0, contentkeyword.length() - 1);
                resultMap.put("keyword_term", contentkeyword);
            }
            newZhongTaiService.saveIntoMysql(resultMap);

        }
    }

    /**
     * 判断字符串是否是金额
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        if(StringUtils.isBlank(str)){
            return false;
        }
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,5})?$"); // 判断小数点后2位的数字的正则表达式
        java.util.regex.Matcher match = pattern.matcher(str);
        if (match.matches() == false) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkAmount(String amount) {
        //金额为空 或（金额纯数字的& 金额大于等于1000万）
        if (StringUtils.isBlank(amount) ||
                (MathUtil.match(amount) && new BigDecimal(amount).compareTo(new BigDecimal("400000")) >= 0)) {
            return true;
        }
        return false;
    }

    //调取中台数据———— //预算金额 OR 中标金额字段值≥1000万
    public void getDataFromZhongTaiAndSave4(NoticeMQ noticeMQ){
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        //预算金额 ≥40万
        if (checkAmount(noticeMQ.getBudget()) && noticeMQ.getBudget() != null && noticeMQ.getBudget() != "" ) {
            Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
            if (resultMap != null) {
                String contentInfo = resultMap.get("content").toString();
                String content = processAboutContent(contentInfo);
                if (StringUtils.isNotBlank(content)) {
                    resultMap.put("content", content);
                }
                newZhongTaiService.saveIntoMysql(resultMap);
            }
        }
    }

    //获取乙方宝标的物块
    public String getYFBBiaoDiWu(String content) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            HttpPost post = new HttpPost("http://47.93.191.54:5110/z");
            //创建参数列表
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("text", content));
            post.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
            //url格式编码
            post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            //设置超时时间为60秒
            post.setConfig(requestConfig);
            //执行请求
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String entity = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                return entity;
            }
        } catch (Exception e) {
            log.error("结果细分判断出错:{}", e);
            throw new RuntimeException("乙方宝标的物出错");
        }
        return null;
    }

    //调取中台数据 二次 进行对比数据
    public void getDataFromZhongTaiAndSave2(NoticeMQ noticeMQ){

        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            String contentInfo = resultMap.get("content").toString();
            String content = processAboutContent(contentInfo);
            if (StringUtils.isNotBlank(content)) {
                resultMap.put("content", content);
            }
            String zhao_biao_unit = resultMap.get("agent_unit").toString();
            //精确用.equals(aa)  模糊用.contains(aa)
            if ((zhao_biao_unit.contains("华能招标") || zhao_biao_unit.contains("华阳新庄")) && zhao_biao_unit != null && zhao_biao_unit != ""){
                newZhongTaiService.saveIntoMysql(resultMap);
            }
        }
    }

    //调取中台数据 并 匹配行业标签
    public void getDataFromZhongTaiAndSave6(NoticeMQ noticeMQ) {
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> map = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (map != null) {
            String zhaobiaounit = map.get("zhao_biao_unit") != null ? map.get("zhao_biao_unit").toString() : "";
            String zhaobiaoindustry = NewRuleUtils.getIndustry(zhaobiaounit);
            String[] zhaobiaosplit = zhaobiaoindustry.split("-");
            if ("医疗单位".equals(zhaobiaosplit[0])){
                newZhongTaiService.saveIntoMysql(map);
            }
        }
    }

    //根据contentid导出标准字段(从solr里面查询)
    public void getSolrAllField() throws IOException {

        ExecutorService executorService = Executors.newFixedThreadPool(80);
        List<Future> futureList = new ArrayList<>();
            futureList.add(executorService.submit(() -> {

                List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT content_id,title,content FROM jyf_data WHERE task_id is null ");
                for (Map<String, Object> map : maps) {

                        String[] aaa = {"SMT 印刷机", "锡膏印刷机", "SMT钢网", "PCB钢网"};

                        String title = map.get("title") != null ? map.get("title").toString() : "";
                        String content = map.get("content") != null ? map.get("content").toString() : "";
                        String contentid = map.get("content_id") != null ? map.get("content_id").toString() : "";

                        content = title + "&" + content;
                        content = content.toUpperCase();

                        String keyword = "";
                        for (String aa : aaa) {
                            if (content.contains(aa)) {
                                keyword = aa;
                            }else{
                                log.info("contentid：{} 数据异常!!!!!!!，关键词：{}",contentid,keyword);
                            }
                        }
                        if (StringUtils.isNotBlank(keyword)) {
                            bdJdbcTemplate.update("UPDATE jyf_data set keyword = ? WHERE content_id = ?",keyword,contentid);
                            log.info("contentid：{} 数据处理成功!!!!!!!，关键词：{}",contentid,keyword);
                        }
                }

            }));
        for (Future future : futureList) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();

    }

    //根据contentid导出标准字段(从中台查询)
    public void getSolrAllField2() throws IOException {

        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();

        List<String> ids = LogUtils.readRule("smf");

//        String[] ids = {"162678727","166847491","167211162","168279882","168293019","168427129","168444716","169750260","170039748","170041464","170154325","170154604","171067651","171133273","171147871","171153558","171381666","171382400","171943961","171944347","172004632","172011637","172151540","172675667","172768742","172768795","172827166","173111644","173603943","173612955","175226042","175226071","175257898","175258179","175870598","176171006","176364017","176416179","176477004","176477043","176478811","176589420","178091818","178091838","178101302","178103071","180668516","180669510","181140982","181161634","184074128","184078409","186254900","186254914","195757118","200090432","200091446","200104373","200104389","200104422","200122505","200122737","200122750","200127955","200127970","200127972","200127978","200299198"};

        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());

        ExecutorService executorService = Executors.newFixedThreadPool(80);
        List<Future> futureList = new ArrayList<>();

        for (String id : ids) {

            NoticeMQ noticeMQ = new NoticeMQ();
            noticeMQ.setContentid(Long.valueOf(id));

            futureList.add(executorService.submit(() -> {
                try {
                    getDataFromZhongTaiAndSave(noticeMQ);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        for (Future future : futureList) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();

    }

    public void getSolrAllField3() throws IOException {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        List<String> ids = LogUtils.readRule("smf");

        for (String id : ids) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("id:\"" + id + "\"", "", null);
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //通世达
    public void getTongShiDaSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();


        String[] aaa = {"智慧大厅","智能设备","机器人","智能化","无人值守","7X24小时"};


        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201126] AND progid:3 AND catid:[* TO 100] AND (title:\"" + aa + "\")", key, 64);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList(aaa));

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //江苏圣威
    public void getShengWeiDaSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();


        String[] aaa = {"环境整治","环境综合整治","保安服务","保洁服务","物业服务","保安保洁","安保服务","秩序维护","市容环境","市容管理","保安外包服务","市容市貌","交通管制","安保保洁","环境卫生整治","垃圾治理","渣土管控","保安采购","安保外包服务","治安工作","保安劳务外包","安保采购","市容整治","保安人员项目","交通秩序"};
        String[] bbb = {"特勤"};


        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20171110 TO 20201123] AND (city:1310 OR city:1311 OR city:1312 OR city:1315) AND progid:3 AND catid:[* TO 100] AND (allcontent:\"" + aa + "\")", key, 857);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (String bb : bbb) {
            futureList1.add(executorService1.submit(() -> {
                String key = bb ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20171110 TO 20201123] AND (city:1310 OR city:1311 OR city:1312 OR city:1315) AND progid:3 AND catid:[* TO 100] AND (title:\"" + bb + "\")", key, 857);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList(aaa));
        arrayList.addAll(Arrays.asList(bbb));

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //江苏火禾
    public void getHuoHeSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();


        String[] aaa = {"工程测量","竣工测量","地理信息系统","地形测量","控制测量","工程测绘","管线探测","地形测绘","权籍调查","摄影测量","地形图测绘","航空摄影","水准测量","地形图测量","地下管线测量","地图编制","天地图","地籍测量","建筑测量","地理国情","管线测量","大地测量","地理信息测绘","房屋测绘","水深测量","管道探测","遥感工程测量","地貌测绘","地图测绘","多测合一","地勘测绘","土地测量","道路测量","管道测量","扫海测量","似大地水准面","重力测量","矿山测量","测绘项目","测绘服务","基础测绘","地理信息服务","时空大数据","遥感"};
        String[] bbb = {"测绘"};
        String[] ccc = {"测绘"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20191101 TO 20201109] AND province:15 AND progid:3 AND catid:[* TO 100] AND (allcontent:\"" + aa + "\")", key, 1);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (String bb : bbb) {
            futureList1.add(executorService1.submit(() -> {
                String key = bb ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20191101 TO 20201109] AND province:15 AND progid:3 AND catid:[* TO 100] AND (title:\"" + bb + "\")", key, 1);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (String cc : ccc) {
            futureList1.add(executorService1.submit(() -> {
                String key = cc ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20191101 TO 20201109] AND province:15 AND progid:3 AND catid:[* TO 100] AND (blZhongBiaoUnit:\"" + cc + "\")", key, 1);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList(aaa));
        for (String bb : bbb) {
            arrayList.add(bb);
        }
        for (String cc : ccc) {
            arrayList.add(cc);
        }

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //威盛信息
    public void getWeiShengSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"智慧城市", "智慧园区", "智慧校园", "智慧社区"};
        String[] bbb = {"水表","电表"};
        String[] bb1 = {"采购","更换","改造"};
        String[] bb2 = {"储能"};
        String[] bb3 = {"采购","改造"};
        String[] bb4 = {"智慧"};
        String[] bb5 = {"城市"};

        String[] ccc = {"采购","询价","竞价","耗材","备品","备件","零件","物资","五金"};
        String[] ddd = {"智能水表","计量水表","家用水表","电子水表","机械水表","冷水表","热水表","预付费水表","磁卡水表","大口径水表","插卡水表","自来水表","自来水水表","IC卡水表","新水表","无线网络水表","饮水安全水表","用水表","不锈钢304水表","电子远传水表","电磁水表","容积式水表","速度式水表","无线远传水表","旋翼式水表","螺翼式水表","单流束水表","多流束水表","小口径水表","民用水表","工业用水表","水平安装水表","立式安装水表","立式表","热水水表","冷水水表","普通水表","高压水表","湿式水表","干式水表","液封水表","节水水表","可拆卸式水表","TM卡水表","定量水表","代码数据交换式水表","射频卡水表","标准水表","远传水表","旋转活塞式水表","抄表水表","工业仪表","居民水表","二级水表","工程水表","水表采购","水表更换","计费水表","水表改造","预存卡式水表","流量仪表"};
        String[] eee = {"智能电表","三相电表","家用电表","单相电表","三相四线电表","电度表","单相电度表","插卡电表","电子电表","预付费电表","互感器电表","插卡式电表","数字电表","脉冲电表","水电表","机械电表","电能表","宿舍电表","焦罐车电表","远程电表","电线电表","有功电表","IC卡电表","电表采购","电表更换","钳形电表","远抄电表","用电表","专用电表","刷卡电表","充值电表","多用户电表","预付费电能表","智能电能表","三相电能表","单相电子表","三相电子表","有功电能表","无功电能表","无功电表","标准电能表","标准电表","三相四线电能表","三相三线电能表","三相三线电表","多功能电表","多功能电能表","电子式电能表","电子式电表","单相电能表","机械式电能表","静止式电能表","固态式电能表","整体式电能表","分体式电能表","最大需量表","复费率分时电能表","损耗电能表","安装式电能表","安装式电表","精密级电表能","机械电能表","长寿命电能表","宽量程电能表"};
        String[] fff = {"光伏电站","储能变流器","全钒液流电池","光热发电","太阳能光伏","钠硫电池","超级电容器","光伏并网发电","锂空气电池","分布式发电","飞轮电池","小型风力发电","动力电池","锂硫电池","分布式光伏","太阳能发电","储能电站","风力发电场","储能用蓄电池","储能电池","储能光伏","家庭储能","锂离子电池","储能器","电感器储能","电容器储能","电池储能","重力势能储能","储能电机","储能电动机","储能电源","储能式直流电源","分布式储能装置"};


        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190901 TO 20190931] AND progid:[0 TO 3] AND catid:[* TO 100] AND (allcontent:\"" + aa + "\")", key, 11);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (String bb : bbb) {
            for (String b1 : bb1) {
                futureList1.add(executorService1.submit(() -> {
                    String key = bb + "&" + b1;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190901 TO 20190931] AND progid:[0 TO 3] AND catid:[* TO 100] AND (title:\"" + bb + "\" AND title:\"" + b1 + "\")", key, 11);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    list1.add(data);
                                    data.setKeyword(key);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
        }

        for (String b2 : bb2) {
            for (String b3 : bb3) {
                futureList1.add(executorService1.submit(() -> {
                    String key = b2 + "&" + b3;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190901 TO 20190931] AND progid:[0 TO 3] AND catid:[* TO 100] AND (title:\"" + b2 + "\" AND title:\"" + b3 + "\")", key, 11);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    list1.add(data);
                                    data.setKeyword(key);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
        }

        for (String b4 : bb4) {
            for (String b5 : bb5) {
                futureList1.add(executorService1.submit(() -> {
                    String key = b4 + "&" + b5;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190901 TO 20190931] AND progid:[0 TO 3] AND catid:[* TO 100] AND (title:\"" + b4 + "\" AND title:\"" + b5 + "\")", key, 11);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    list1.add(data);
                                    data.setKeyword(key);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
        }

        for (String cc : ccc) {
            for (String dd : ddd) {
                futureList1.add(executorService1.submit(() -> {
                    String key = cc + "&" + dd;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190901 TO 20190931] AND progid:[0 TO 3] AND catid:[* TO 100] AND (title:\"" + cc + "\" AND allcontent:\"" + dd + "\")", key, 11);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    list1.add(data);
                                    data.setKeyword(key);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
        }

        for (String cc : ccc) {
            for (String ee : eee) {
                futureList1.add(executorService1.submit(() -> {
                    String key = cc + "&" + ee;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190901 TO 20190931] AND progid:[0 TO 3] AND catid:[* TO 100] AND (title:\"" + cc + "\" AND allcontent:\"" + ee + "\")", key, 11);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    list1.add(data);
                                    data.setKeyword(key);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
        }

        for (String cc : ccc) {
            for (String ff : fff) {
                futureList1.add(executorService1.submit(() -> {
                    String key = cc + "&" + ff;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190901 TO 20190931] AND progid:[0 TO 3] AND catid:[* TO 100] AND (title:\"" + cc + "\" AND allcontent:\"" + ff + "\")", key, 11);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    list1.add(data);
                                    data.setKeyword(key);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList(aaa));
        for (String bb : bbb) {
            for (String b1 : bb1) {
                String key = bb + "&" + b1;
                arrayList.add(key);
            }
        }
        for (String b2 : bb2) {
            for (String b3 : bb3) {
                String key = b2 + "&" + b3;
                arrayList.add(key);
            }
        }
        for (String b4 : bb4) {
            for (String b5 : bb5) {
                String key = b4 + "&" + b5;
                arrayList.add(key);
            }
        }
        for (String cc : ccc) {
            for (String dd : ddd) {
                String key = cc + "&" + dd;
                arrayList.add(key);
            }
        }
        for (String cc : ccc) {
            for (String ee : eee) {
                String key = cc + "&" + ee;
                arrayList.add(key);
            }
        }
        for (String cc : ccc) {
            for (String ff : fff) {
                String key = cc + "&" + ff;
                arrayList.add(key);
            }
        }

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //乔治白
    public void getQiaoZhiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"服装","工作服","工装","工作制服","工作装","职业服","职业装","制服","工服","行服","司服","衬衫","衬衣","院服","统一着装","大衣","常服","馆服","公务置装"};
        String[] bbb = {"海澜之家股份","希努尔男装股份","大杨创世服饰","江苏阳光集团有限公司","江苏红豆实业","圣凯诺服饰","宁波雅戈尔服饰","培罗成集团","山东舒朗服装服饰股份","耶莉娅服装集团","山东南山智尚","上海宝鸟服饰","罗蒙集团股份","福建柒牌集团","澳洋集团","利郎(中国)","利郎（中国）","利郎中国","法派服饰股份","法派集团","北京红都集团有限公司","温州庄吉服饰","乔顿服饰股份","三五零二职业装","才子服饰股份","山东如意科技","浙江乔治白服饰","如意毛纺集团","江苏澳洋纺织","江苏虎豹集团有限","青岛红领集团","青岛红领投资","江苏虎豹服饰","依文服饰股份","上海海螺（集团）","上海海螺(集团)","上海海螺集团","江苏鹿港科技有限","上海开开实业","上海开开制衣","宁波杉杉股份","杉杉集团有限"};
        String[] blacks = {"校服","学生服","检察制服","警用制服","税务制服","审判制服","海关查验服","法警服","设备","零件","结构件","钢筋","防水卷材","工具","工程胎","建工装饰","电工装备","垫板","模具","施工装备","施工装修","量具","钢板","纳米管","支撑杆","制动器","包壁板","弯曲压缩","螺母","焊工","吊挂","拉紧带","欧式强力环","载荷","焊接","备件","零速转弯","电梯","继电器","转接头","配件","打印机","五金","紧固件","配电柜","交换机","服装市场","服装厂","服装档","立棍","喷砂","开关","装置","摩擦焊","成型鼓","阶形螺钉","万向轮","清水泵","元件","电焊线","密炼机","相机","变频器","喷涂","接头","胶管","加工件","电视","安全席","炼胶","钢管","焊接线","无人机","蝶阀","穿孔针","翻页笔","电缆","特种胎","百页车","机械生产","毛坯件","铺轨","密封圈","全钢","点火剂","三角胶","贴合鼓","电极","灯标","施工","风扇","洗地车","赛思特","变送器","电源","触摸屏","工控机","连杆","工装采购处","电控柜","清洗","编制服务","印制服务","改制服务","复印纸","硒鼓","防制服务","铝合金窗","提示语","标牌","门牌定制","资料定制","货架定制","公章刻制","实验室定制","笔记本","音响系统","窗帘定制","热水器","电脑","摄制服务","资产清查","个性化定制","洗衣皂","场景定制","课程录制","系统建设","控制服务","投影仪","工作服务","扫把","活扳手","卷筒纸","毛巾","无线鼠标","记事本","润滑油","垃圾袋","出行服务","运行服务","执行服务","发行服务","用工服务","加工服务","社工服务","运输服务","人工服务","厨工服务","清洁工服务","职工服务","医院服务","学院服务","大院服务","服务器","住院服务","卫生院服务","出入院服务","维修保养","绿化改造","道路拓宽","信息化软件","软件扩容","绿化养护","GPU卡","机房建设","教学软件","软件升级","法院服务","医共体建设","装修工程","扩建工程","分析系统","器具","急件","圆球型","滤芯","装载机","井盖","雨水篦","化粪池","隔油池","应力测试","步行板","水泥","电池","天线转接","加工装备","导管","结构板","瓷砖","装配式","测试工装","钢材","转运工装","试验工装","公司服务","场馆服务","博物馆服务","新馆服务","档案馆服务","进馆服务","图书馆服务","印刷服务","恒常服务","日常服务","修缮工程","锅炉房","土建工程","连廊改造","录制服务","刻制服务","改造工程","造价咨询","银行服务","通行服务","环境卫生","美育服装","舞蹈服装","失败公告","技工学校","护工服务","服装专业","执法制式服装","合同公示","志愿者服装","环卫","比赛服装","参赛服装","服装工程","军用","巡特警","军训服装","行政执法","服装租赁","环卫服装","服装职业","职业教育","森林公安","服装批发","剧院服务","演出服装","民族服装","健身服装","演出服","服装加工","职业技术","警用","学生工作服","户外服装","衬衣面料","运动员服装","消防制服","洗衣液","执法制服","劳动监察","艺术学院","城管制服","保安服装","国旗护卫","环保工作","舞蹈队服装","交通警察","联防队员","服装面料","装饰工程","固废处置","戒毒管理","环卫工人","炊事服装","法院制服","礼仪服装","节目服装","舞美服装","体育服装","电工服务","矿用工作服","活动服装","酸碱工作服","福利院服","学院服装","行服务","环卫工服","安保工作服","职业中专","戒毒所","环卫工作服","食堂","服装研制","监狱","训练服装","学生","服装行业","服装制作","森林消防","体育老师服装","应急物资","职业学院","特勤服装","服装管理","应急服装","劳保服装","运动服装","快警","突击队","城管队员","卫生保洁","车间作业","协管员服装","服装造型","巡防队","园林绿化","消防救援","特困","养护","市政","辅警","执法","制式服装","印刻服务","定制服务","路灯","执法部门制服","订制服务","试制服务","交通制服","检察机关制服","拟制服务","检查制服","安检制服","检察官制服","民警制服","窗帘","印刷品","分制服务","检疫制服","销轴","套管","模板","辅料","螺桩","造型线","检测棒","机床","集装箱","工装公司","耗材","手板","电气类"};

        //需求二 规则二
//        for (String aa : aaa) {
//            futureList1.add(executorService1.submit(() -> {
//                String key = aa ;
//                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20180101 TO 20201211] AND (province:2 OR province:24 OR province:5 OR province:30 OR province:316 OR province:31 OR province:1 OR province:16 OR province:13 OR province:10 OR province:23 OR province:22 OR province:7 OR province:20 OR province:4 OR province:19 OR province:11) AND (progid:3 OR progid:5) AND zhaoBiaoUnit:* AND catid:[* TO 100] AND title:\"" + aa + "\" ", key, 2);
//                log.info(key.trim() + "————" + mqEntities.size());
//                if (!mqEntities.isEmpty()) {
//                    for (NoticeMQ data : mqEntities) {
//                        if (data.getTitle() != null && (data.getZhaoBiaoUnit() != null && data.getZhaoBiaoUnit() != "")) {
//                            boolean flag = true;
//                            for (String black : blacks) {
//                                if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
//                                    flag = false;
//                                    break;
//                                }
//                            }
//                            if (flag) {
//                                list1.add(data);
//                                data.setKeyword(key);
//                                if (!dataMap.containsKey(data.getContentid().toString())) {
//                                    list.add(data);
//                                    dataMap.put(data.getContentid().toString(), "0");
//                                }
//                            }
//                        }
//                    }
//                }
//            }));
//        }
//
        //需求二 规则一
        for (String bb : bbb) {
            futureList1.add(executorService1.submit(() -> {
                String key = bb ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20180101 TO 20201211] AND (province:2 OR province:24 OR province:5 OR province:30 OR province:316 OR province:31 OR province:1 OR province:16 OR province:13 OR province:10 OR province:23 OR province:22 OR province:7 OR province:20 OR province:4 OR province:19 OR province:11) AND (progid:3 OR progid:5) AND catid:[* TO 100] AND zhaoBiaoUnit:* AND blZhongBiaoUnit:\"" + bb + "\" ", key, 1);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null && data.getZhongBiaoUnit() != null && data.getZhongBiaoUnit() != "" && data.getZhaoBiaoUnit() != null && data.getZhaoBiaoUnit() != "") {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

//        //需求一
//        for (String aa : aaa) {
//            futureList1.add(executorService1.submit(() -> {
//                String key = aa ;
//                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201211] AND (province:2 OR province:24 OR province:5 OR province:30 OR province:316 OR province:31 OR province:1 OR province:16 OR province:13 OR province:10 OR province:23 OR province:22 OR province:7 OR province:20 OR province:4 OR province:19 OR province:11) AND progid:[0 TO 2] AND zhaoBiaoUnit:* AND catid:[* TO 100] AND title:\"" + aa + "\" ", key, 5);
//                log.info(key.trim() + "————" + mqEntities.size());
//                if (!mqEntities.isEmpty()) {
//                    for (NoticeMQ data : mqEntities) {
//                        if (data.getTitle() != null && data.getZhaoBiaoUnit() != null && data.getZhaoBiaoUnit() != "") {
//                            boolean flag = true;
//                            for (String black : blacks) {
//                                if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
//                                    flag = false;
//                                    break;
//                                }
//                            }
//                            if (flag) {
//                                list1.add(data);
//                                data.setKeyword(key);
//                                if (!dataMap.containsKey(data.getContentid().toString())) {
//                                    list.add(data);
//                                    dataMap.put(data.getContentid().toString(), "0");
//                                }
//                            }
//                        }
//                    }
//                }
//            }));
//        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
//        for (String aa : aaa) {
//            String key = aa ;
//            arrayList.add(key);
//        }
        for (String bb : bbb) {
            String key = bb ;
            arrayList.add(key);
        }
        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //凌立健康
    public void getLingLiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"住培系统","住院医师规范化培训","住院医师规培","医学教学用假人","临床技能中心管理系统","住培管理","临床诊疗思维训练","医学考试系统","规培考试","临床护理思维训练","医学在线考试","医学在线教学","医生规范化培养","规培管理系统","规培教学","虚拟诊疗系统","医学教育管理系统","规培医生管理","住院医师管理","住培信息化平台","住培医师","住培软件","住院医师信息管理","全科医生培训","科教管理系统","住培考试","医教云信息一体化","住院医师规范化","护理技能训练","医师培训","医学教学","医学题库","医生资格考试","临床技能培训","住培医师信息化","临床思维训练系统","临床教学系统","临床技能培训考核系统","临床技能中心培训考试系统","临床技能中心管理软件","临床思维培训","临床技能管理系统","临床技能培训管理系统","临床实践中心信息化建设","三维虚拟教学系统","临床考试系统","临床技能培训中心信息化","医生培训平台","药学实践平台","医师实践技能考试系统","医院考试系统","临床思维系统","临床思维综合训练","临床思维教学训练","临床思维诊疗系统","医生培训设备","医生转岗培训","医学考试软件","医学考试考务系统","医学考试题库管理","医师资格考试技能系统","医师资格考试","远程医学培训","医学理论在线考试","医师资格考试信息化管理","医师规培信息管理","医师规培管理软件","护理技能训练系统","临床考试管理系统","医学在线理论考试","临床医学题库","医学教育题库","临床辨证思维考核","护理思维培训","规培教学管理系统","医师考试系统","住陪在线考试"};
        String[] bbb = {"OSCE","医学假人模型","医用人体模型","人体模型","假人模型","医学模型","医用模型","教学模型","护理模型","人体穴位模型","复苏模型","角膜模型","仿真模型","腹穿模型","插管模型","气道管理模型","急救培训模型","医学教学模型","医院教学模型","教学培训模型","护理实践教学模型","急救教学模型","解剖教学模型","口腔教学模型","医用教学模型","临床技能教学模型","内科教学模型","医疗教学模型","临床教学模拟模型","临床教学模型","医学综合模型","复苏婴儿人体模型","心肺复苏人体模型","医学系列模型","急诊培训训练模型","触诊训练模型","医护技能训练模型","医院模拟训练模型","心肺复苏训练模型","创伤综合训练模型","注射器训练模型","腹腔镜训练模型","检查训练模型","技能训练模型","梗塞训练模型","手臂训练模型","活检训练模型","训练模型","临床技能实训模型","临床技能实训基地模型","临床技能训练中心模型","临床技能训练模型","超声诊断模型","医生培训模型","穿刺模型","穿刺训练模型","手臂模型","检查模型","注射模型","护理训练模型","医疗模型","急救训练模型","穿刺引流模型","包扎模型","气胸处理模型","躯干训练模型","穿刺插管模型","病变检查模型","医学模拟假人","医用模拟假人","模拟病人","模拟假人","医用教学模拟人","模拟人","模拟器械","模拟肺","心肺复苏模拟人","医院教学模拟设备","心肺复苏模拟设备","临床技能模拟培训设备","医学模拟教学设备","穿刺模拟人","护理模拟人","仿真模拟人","穿刺训练模拟人","护理实训模具","临床技能培训模具","医疗模具","医学模具","医用模具","橡胶假人","仿真病人"};
        String[] blacks = {"设备","器械","器材","师资培训","科教培训平台","印刷服务","物资采购","督导评估","考核","事务管理","混凝土","大楼项目","暖通工程","招聘","叠合板","办公用品","物业","保安","保洁","招生","调研","办公家具","使用权","人才需求","招录","注射器","清洁"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190101 TO 20201231] AND progid:3 AND allcontent:\"" + aa + "\" ", key, 1);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            for (String black : blacks) {
                                if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (String bb : bbb) {
            futureList1.add(executorService1.submit(() -> {
                String key = bb ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190101 TO 20201231] AND progid:3 AND allcontent:\"" + bb + "\" ", key, 2);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList(aaa));
        arrayList.addAll(Arrays.asList(bbb));


        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> {
                        getDataFromZhongTaiAndSave(content);
                }));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //昂楷科技
    public void getAngKaiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"协议供货","入围公告"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201101 TO 20201130] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND (title:\"" + aa + "\")", key, 22);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList(aaa));

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //程力重工
    public void getChengLiSolrAllField(String date) throws IOException {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        List<String> aaa = LogUtils.readRule("smf");

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190101 TO 20201214] AND province:8 AND (progid:3 OR progid:5) AND catid:[* TO 100] AND (allcontent:\"" + aa + "\")", key, 1234);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            arrayList.add(aa);
        }

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //金海屿
    public void getJinHaiYuSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"采购","购买","购置","一批物资","定购","购入","代买","购进","采买","新购","成交","中标"};
        String[] bbb = {"消防器材","消防设备","消防装置","消防水带","消防斧","消防产品","消防服","消火栓","消防水炮","灭火器","消防箱","报警按钮","报警器","火灾报警控制器","火灾探测器","多功能报警器","消防水箱","消防水枪","水带接扣","灭火器材","消防器","灭火毯","氧气瓶"};
        String[] ccc = {"消防"};
        String[] cc1 = {"设备","器材","装置","物资","配件","设施","器件","装备"};
        String[] ddd = {"礼品"};
        String[] eee = {"慰问品","慰问物资","纪念品","随手礼","伴手礼","纪念礼品","纪念物"};

        for (String aa : aaa) {
            for (String bb : bbb) {
                futureList1.add(executorService1.submit(() -> {
                    String key = aa + "&" + bb;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201215] AND (province:2 OR province:24 OR province:31 OR province:15 OR province:30 OR province:1 OR province:3 OR province:16 OR province:21 OR province:10 OR province:12 OR province:13 OR province:5 OR province:8) AND progid:[0 TO 2] AND catid:[* TO 100] AND zhaoBiaoUnit:* AND title:\"" + aa + "\" AND allcontent:\"" + bb + "\" ", key, 22);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null && data.getZhaoBiaoUnit() != "" && data.getZhaoBiaoUnit() != null) {
                                boolean flag = true;
                                if (flag) {
                                    String zhaoBiaoUnit = data.getZhaoBiaoUnit();
                                    if (StringUtils.isNotBlank(zhaoBiaoUnit)){
                                        String industry = NewRuleUtils.getIndustry(zhaoBiaoUnit);
                                        if (StringUtils.isNotBlank(industry)){
                                            String[] split = industry.split("-");
                                            if (split.length == 2){
                                                if ("政府机构".equals(split[0])){
                                                    data.setKeyword(key);
                                                    list1.add(data);
                                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                                        list.add(data);
                                                        dataMap.put(data.getContentid().toString(), "0");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }));
            }
        }

        for (String cc : ccc) {
            for (String c1 : cc1) {
                futureList1.add(executorService1.submit(() -> {
                    String key = cc + "&" + c1;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201215] AND (province:2 OR province:24 OR province:31 OR province:15 OR province:30 OR province:1 OR province:3 OR province:16 OR province:21 OR province:10 OR province:12 OR province:13 OR province:5 OR province:8) AND progid:[0 TO 2] AND catid:[* TO 100] AND zhaoBiaoUnit:* AND title:\"" + cc + "\" AND title:\"" + c1 + "\" ", key, 22);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null && data.getZhaoBiaoUnit() != "" && data.getZhaoBiaoUnit() != null) {
                                boolean flag = true;
                                if (flag) {
                                    String zhaoBiaoUnit = data.getZhaoBiaoUnit();
                                    if (StringUtils.isNotBlank(zhaoBiaoUnit)){
                                        String industry = NewRuleUtils.getIndustry(zhaoBiaoUnit);
                                        if (StringUtils.isNotBlank(industry)){
                                            String[] split = industry.split("-");
                                            if (split.length == 2){
                                                if ("政府机构".equals(split[0])){
                                                    data.setKeyword(key);
                                                    list1.add(data);
                                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                                        list.add(data);
                                                        dataMap.put(data.getContentid().toString(), "0");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }));
            }
        }

        for (String dd : ddd) {
            futureList1.add(executorService1.submit(() -> {
                String key = dd;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201215] AND (province:2 OR province:24 OR province:31 OR province:15 OR province:30 OR province:1 OR province:3 OR province:16 OR province:21 OR province:10 OR province:12 OR province:13 OR province:5 OR province:8) AND progid:[0 TO 2] AND catid:[* TO 100] AND zhaoBiaoUnit:* AND title:\"" + dd + "\"", key, 22);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null && data.getZhaoBiaoUnit() != "" && data.getZhaoBiaoUnit() != null) {
                            boolean flag = true;
                            if (flag) {
                                String zhaoBiaoUnit = data.getZhaoBiaoUnit();
                                if (StringUtils.isNotBlank(zhaoBiaoUnit)){
                                    String industry = NewRuleUtils.getIndustry(zhaoBiaoUnit);
                                    if (StringUtils.isNotBlank(industry)){
                                        String[] split = industry.split("-");
                                        if (split.length == 2){
                                            if ("政府机构".equals(split[0])){
                                                data.setKeyword(key);
                                                list1.add(data);
                                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                                    list.add(data);
                                                    dataMap.put(data.getContentid().toString(), "0");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (String ee : eee) {
            futureList1.add(executorService1.submit(() -> {
                String key = ee;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201215] AND (province:2 OR province:24 OR province:31 OR province:15 OR province:30 OR province:1 OR province:3 OR province:16 OR province:21 OR province:10 OR province:12 OR province:13 OR province:5 OR province:8) AND progid:[0 TO 2] AND catid:[* TO 100] AND zhaoBiaoUnit:* AND allcontent:\"" + ee + "\"", key, 22);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null && data.getZhaoBiaoUnit() != "" && data.getZhaoBiaoUnit() != null) {
                            boolean flag = true;
                            if (flag) {
                                String zhaoBiaoUnit = data.getZhaoBiaoUnit();
                                if (StringUtils.isNotBlank(zhaoBiaoUnit)){
                                    String industry = NewRuleUtils.getIndustry(zhaoBiaoUnit);
                                    if (StringUtils.isNotBlank(industry)){
                                        String[] split = industry.split("-");
                                        if (split.length == 2){
                                            if ("政府机构".equals(split[0])){
                                                data.setKeyword(key);
                                                list1.add(data);
                                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                                    list.add(data);
                                                    dataMap.put(data.getContentid().toString(), "0");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            for (String bb : bbb) {
                String key = aa + "&" + bb;
                arrayList.add(key);
            }
        }
        for (String cc : ccc) {
            for (String c1 : cc1) {
                String key = cc + "&" + c1;
                arrayList.add(key);
            }
        }
        arrayList.addAll(Arrays.asList(ddd));
        arrayList.addAll(Arrays.asList(eee));

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave4(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //南京莱斯
    public void getLaiSiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"垃圾","绿化","沼气","污水","危废","污泥","淤泥","清扫","清运","收运","废弃","保洁","厨余","环保","环卫","管养","病死畜","餐厨垃圾","餐厨垃圾处理","畜禽粪便","焚烧填埋","干垃圾","工业垃圾","工业危废","公厕维护","公园保洁","公园绿化","管道清疏","河道保洁","黑臭水体","环境治理","环卫一体化","回收再利用","建筑垃圾","静脉产业园","可回收垃圾","垃圾处理","垃圾分类","垃圾收集","垃圾收运","垃圾填埋","垃圾资源化","农业废弃物","其它垃圾","清扫保洁","洒水降尘","渗滤液","渗滤液处置","生活垃圾","生活垃圾处理","生物质","湿垃圾","市政清疏","数字环卫清尘","水域打捞保洁","土壤修复","违法处置","污泥处置","无害化","医疗废弃物","医疗垃圾","有害垃圾","园林废弃物","智慧环卫","中转站","环境整治","城市垃圾","废液","垃圾处置","工业废气","有机废气","VOC治理"};
        String[] blacks = {"拍卖","运营","标识","印刷","防水PVC","垃圾桶","垃圾车","垃圾箱","环卫车","后勤管理","餐厨车","供餐","餐饮","厨具","奖品","调试布袋","车辆","餐厨用具","餐厅","食堂","故障","储柜","安置点","编制","风险评估","测量服务","密集柜","装载机","防尘网","叉车","脱水机","药剂","罐体","所得税","垃圾袋","租赁","报告书","保证金","财务","九阳豆浆机","搅拌器","风机修复","家具","白蚁防治","消防验收","井盖","路灯","保险","用车","垃圾收纳箱","阻火器","项目监理","处理站设备采购","第三方监测服务","车间施工","开关柜","建筑装饰","化验室设备","专家论证","大屏幕设备","土建工程施工","电视","流标","废标","中止","地质勘察","处理设备","成套设备","配套设备","维修","保养","运维","更换","厨余车","建筑工程","垃圾运输车","工程发包","设备采购","扩建工程","压缩车","垃圾压缩车","专用运输车","采集分析","建安工程","装修","设备购置","厂房建设","分析仪表","废液回收机","生产用车","废液泵","废液分配槽","阀门","废液储存柜","土建安装施工","废液处置设备","在线监测设备","废液喷枪","废液回收仪","废液柜","控制设备","试剂瓶","安装","油漆","在线监测监控系统","存放间","材料采购","用品","用料","劳务承包","作业费用","需求论证","违停抓拍系统","基础设施项目","转移支付","清运车","勘察服务","烟囱","危废暂存柜","厂房工程","施工图","日常巡逻","围栏","土地勘测","牌照","整体拆除","装饰","托管运营","柴油发电机","空调维修","生活垃圾处理费征收中心","生活垃圾处理厂","生活垃圾处理服务中心","生活垃圾处理场","废弃物收集袋","垃圾收集容器","垃圾收集站","垃圾收集点","垃圾收集房","经营服务","软件测评","采购货物","修缮","防暑降温","防火门","控制柜","密封片","减速机","广西皖维生物质科技有限公司","塑料包","包装桶","网络设备","电建公司","生物质能发电有限公司","清扫车","保洁包材","环卫用车"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201211 TO 20201213] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND title:\"" + aa + "\" ", key, 5);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null && (data.getZhaoBiaoUnit() != null && data.getZhaoBiaoUnit() != "")) {
                            boolean flag = true;
                            for (String black : blacks) {
                                if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            String key = aa ;
            arrayList.add(key);
        }

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //速臻科技
    public void getSuZhenSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        futureList1.add(executorService1.submit(() -> {
            List<NoticeMQ> mqEntities = ictContentSolr.companyResultsBaoXian("yyyymmdd:[20201001 TO 20201215] AND progid:[31 TO 37] AND (province:11 OR province:17 OR province:14) ", "", 5);
            if (!mqEntities.isEmpty()) {
                for (NoticeMQ data : mqEntities) {
                    if (data.getTitle() != null && (data.getZhaoBiaoUnit() != null && data.getZhaoBiaoUnit() != "")) {
                        boolean flag = true;
                        if (flag) {
                            list1.add(data);
                            if (!dataMap.containsKey(data.getContentid().toString())) {
                                list.add(data);
                                dataMap.put(data.getContentid().toString(), "0");
                            }
                        }
                    }
                }
            }
        }));


        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //象辑知源
    public void getXiangJiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"电力","电网","风电","水电","火电","电场","电厂","发电","国电","供电","煤电","核电","电能","电站","热电","电建","华电","电务","粤电","生物电","输电","变电","配电","高压电","电压","电流","失电","停电","疾电","用电"};
        String[] bbb = {"天气","气流","气候","气象","气温","雨量","雨水","雨晴","人工降雨","自然灾害","降雪","降雨","冰雹","风暴","强风","气压","湿度","风向","风力","乌云","雾霾","大雾","雾雪","大雨","暴雨","暴雪","雷电","闪电","雨夹雪","雷暴","冰霜","霜降","大气","臭氧","洪涝","阵风","旋风","焚风","龙卷风","台风","暴风","水汽凝结","雨雪","雷雨","雪崩","雪霜","对流雨","锋面雨","地形雨","台风雨","气旋雨","小雨","中雨","多云","阴天","雷阵雨","大雪","小雪","中雪","霜冻","低压槽","高压脊","冷锋","暖锋","冷空气","级风","微风","大风","狂风","飓风","冷气团","暖气团"};

        for (String aa : aaa) {
            for (String bb : bbb) {
                futureList1.add(executorService1.submit(() -> {
                    String key = aa + "&" + bb;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201101 TO 20201130] AND progid:3 AND catid:[* TO 100] AND title:\"" + aa + "\" AND title:\"" + bb + "\" ", key, 5);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null && (data.getZhaoBiaoUnit() != null && data.getZhaoBiaoUnit() != "")) {
                                boolean flag = true;
                                if (flag) {
                                    list1.add(data);
                                    data.setKeyword(key);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            for (String bb : bbb) {
                String key = aa + "&" + bb;
                arrayList.add(key);
            }
        }

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //北京万通
    public void getWanTongSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        futureList1.add(executorService1.submit(() -> {
            List<NoticeMQ> mqEntities = ictContentSolr.companyResultsBaoXian("yyyymmdd:[20200925 TO 20201221] AND (progid:[0 TO 3] OR progid:5)", "", null);
            if (!mqEntities.isEmpty()) {
                for (NoticeMQ data : mqEntities) {
                    if (data.getZhaoBiaoUnit() != null && data.getZhaoBiaoUnit() != "") {
                        String zhaoBiaoUnit = data.getZhaoBiaoUnit();
                        if (StringUtils.isNotBlank(zhaoBiaoUnit)){
                            String industry = NewRuleUtils.getIndustry(zhaoBiaoUnit);
                            if (StringUtils.isNotBlank(industry)){
                                String[] split = industry.split("-");
                                if (split.length == 2){
                                    if ("政府机构-检法司".equals(industry) || "政府机构-政法委".equals(industry) || "政府机构-纪委".equals(industry)){
                                        String secondLevel = split[1];
                                        data.setKeyword(split[0]);
                                        data.setF(secondLevel);
                                        list1.add(data);
                                        if (!dataMap.containsKey(data.getContentid().toString())) {
                                            list.add(data);
                                            dataMap.put(data.getContentid().toString(), "0");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }));

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //荣耀昌化网络
    public void getRongYaoSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"洗涤", "干洗", "清洗", "水洗", "洗熨"};
        String[] bbb = {"服装","工作服","工装","工作制服","工作装","职业服","职业装","制服","学生服","工服","校服","行服","司服","衬衫","衬衣","院服","统一着装","西服","西装","大衣","常服","馆服","公务置装"};

        for (String aa : aaa) {
            for (String bb : bbb) {
                futureList1.add(executorService1.submit(() -> {
                    String key = aa + "&" + bb;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20180101 TO 20201222] AND (progid:0 OR progid:3) AND (province:24 OR province:15 OR province:30 OR province:1 OR province:3 OR province:21) AND catid:[* TO 100] AND title:\"" + aa + "\" AND allcontent:\"" + bb + "\" ", key, 5);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null && (data.getZhaoBiaoUnit() != null && data.getZhaoBiaoUnit() != "")) {
                                boolean flag = true;
                                if (flag) {
                                    list1.add(data);
                                    data.setKeyword(key);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            for (String bb : bbb) {
                String key = aa + "&" + bb;
                arrayList.add(key);
            }
        }

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //羽医医疗
    public void getYuYiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"手术刀柄和刀片","皮片刀","疣体剥离刀","柳叶刀","铲刀","剃毛刀","皮屑刮刀","挑刀","锋刀","修脚刀","修甲刀","解剖刀","普通手术剪","组织剪","综合组织剪","拆线剪","石膏剪","解剖剪","纱布绷带剪","教育用手术剪","普通止血钳","小血管止血钳","蚊式止血钳","组织钳","硬质合金镶片持针钳","普通持针钳","创夹缝拆钳","皮肤轧钳","子弹钳","纱布剥离钳","海绵钳","帕巾钳","皮管钳","器械钳","小血管镊","无损伤镊","组织镊","整形镊","持针镊","简易镊","保健镊","拔毛镊","帕巾镊","敷料镊","解剖镊","止血夹","动脉瘤针","探针","推毛针","植毛针","挑针","教学用直尖针","静脉拉钩","创口钩","扁平拉钩","双头拉钩","皮肤拉钩","解剖钩","超声耦合剂","超声去脂仪","超声治疗机","超声雾化器","超声穴位治疗机","超声按摩仪","超声骨折治疗机","超声洁牙机","超声波妇科皮肤治疗仪","B型电子线阵超声诊断仪","B型机械扇扫超声诊断仪","B型伪彩色显示仪","超声听诊器","超声骨密度仪","超声骨强度仪","超声骨测量仪","心腔内超声导管换能器","穿刺超声换能器","血管内超声换能器","电子线阵换能器","机械扫描换能器","环阵换能器","凸阵扫描换能器","食管超声换能器","刀片夹持器","麻醉口罩","麻醉开口器","照明吸引器头","粉刺取出器","黑头粉刺压出器","皮肤刮匙","皮肤套刮器","皮肤刮划测检器","皮肤检查尺","皮肤组织钻孔器","开口器","卷棉子","显微喉刀","显微剪","显微枪形手术剪","显微组织剪","显微合拢器","显微枪形麦粒钳","显微喉钳","显微持针钳","显微镊","显微持针镊","显微止血夹","显微耳针","显微喉针","显微耳钩","显微喉钩","脑神经刀","肿瘤摘除钳","银夹钳","脑膜镊","脑膜钩","脑活检抽吸器","脑吸引器","胸骨刀","心脏手术剪","心内膜心肌活组织钳","心房持针钳","大隐静脉镊","胸腔镊","心室拉钩","心房拉钩","血管打洞器","血管打洞钳","血管扩张器","心内吸引头","心内吸引器","胃内手术剪","胆石钳","胆道拉钩","荷包成型器","肠剪","血管阻断钳","肾蒂钳","膀胱拉钩","尿道扩张器","椎管铲刀","丝锥","双关节棘突骨剪","颈椎咬骨钳","膝关节息肉钳","环锯","弓锯","单侧椎板拉钩","颈椎刮匙","风动开颅器","电动石膏剪","肢体延长架","超声肿瘤聚焦刀","超声立体诊断仪","超声三维诊断仪","超声三维（立体）诊断仪","多功能超声监护仪","可拆卸式脑膜刀","脑组织咬除钳","U型夹钳","垂体瘤镊","脑膜拉钩","脑膜剥离器","后颅凹牵开器","脑膜刀","动脉瘤夹钳","肿瘤夹持镊","神经钩","神经根拉钩","交感神经钩","脑刮匙","脑垂体刮匙","手摇颅骨钻","脑打针锤","脑压板","胸骨剪","心房侧壁钳","胸腔止血钳","心房止血器","胸腔组织镊","二尖瓣膜拉钩","心房打洞器","血管牵开器","左房引流管","直角剪","脾蒂钳","双头腹壁拉钩","压肠板","膀胱切除剪","骼血管阻断钳","肠钳","前列腺拉钩","肛门镜","椎管锉刀","髓腔铰刀","双关节咬骨剪","颈椎双关节咬骨钳","咬骨钳","腰椎用梯形骨凿","指锯","半月板钩","可变神经剥离子器","电池式自停颅骨钻","电动石膏锯","多功能单侧外固定支架","超声高强度聚焦肿瘤治疗系统","全数字化彩超仪","超声母亲/胎儿综合监护仪","肋骨剪","主动脉侧壁钳","胸腔组织钳","心耳止血器","肺组织镊","排气针","二尖瓣扩张器","胸骨手钻","冠状动脉吸引器","腹膜钳","阑尾拉钩","单胆石匙","双胆石匙","前列腺剪","骼静脉侧壁钳","直肠活体取样钳","肛门探针","阴茎夹","手锥","加压螺纹钉铰刀","骨剪","脊柱侧弯矫正钳","持骨钳","椎间盘手术用环锯","骨锯","下肢截断拉钩","刮匙","超声脂肪乳化仪","超声彩色多普勒","超声产科监护仪","截断刀","膝关节韧带手术剪","髓核钳","腐骨钳","C－D椎板剥离器","小园刮凿","骨钩","骨膜剥离器","电动骨钻","电动胸骨锯","膀胱肿瘤匙钳","超声眼科乳化治疗仪","血管内超声波诊断仪","胎儿监护仪","截骨刀","椎板咬骨钳","复位钳","颈椎测深凿","丁字凿","颈椎拉钩","胫骨切刀","钢丝剪","弯头平口棘突骨钳","持钉钳","颈椎直角骨凿","骨锉","颈前路深部缝合针","石膏刀","短柄吸引器","枪形咬骨钳","持板钳","椎板骨凿","弧形凿","骨牵引针","膀胱颈钳","超声手术刀","超声结肠镜（诊断仪）","超声诊断仪","超声结肠镜","双头剥离匙","冠状动脉灌注器","胃组织取样钳","气腹针","胆道探条","痔核钳","超声血管内介入治疗仪","超声内窥镜多普勒","肋骨骨膜剥离子","大隐静脉冲洗管","经颅超声多普勒","超声眼科专用诊断仪","腹壁固定牵开器","超声乳腺热疗治疗仪","超声心内显像仪","内膜剥离器","静脉撑开器","复合式扫描超声诊断仪","持棒钳","椎体骨凿","髋关节成型凿","加压螺纹钉导引针","髋关节成型凹凸钻","胫骨切割器","持钩钳","椎体前方剥离器","石膏锯","钻头","螺杆夹持钳","梯形铲","铰孔钻","撑开钳","肘关节肱骨成型骨凿","手枪式手摇骨钻","压缩钳","髓腔锉","枪形取样钳","椎管锉","骨克丝钳","骨凿","钢板弯曲钳","座导凿","钢丝钳","主动脉阻断钳","三角肺叶钳","凹凸齿止血夹","主动脉止血钳","结扎钳","主动脉游离钳","双关节肋骨咬骨钳","无损伤肺动脉止血钳","无损伤动脉止血钳","无损伤动脉导管钳","动脉侧壁钳","动脉阻断钳","静脉阻断钳","腔静脉钳","腔静脉游离钳","主肺动脉钳"};
        String[] bbb = {"采购","购买","购置","物资","定购","购入","代买","购进","采买","新购","询价"};
        String[] ccc = {"医疗设备","医疗机器","医疗仪器","医疗器材","医疗装备","医疗装置","医疗器械","医用设备","医用机器","医用仪器","医用器材","医用装备","医用装置","医用器械","医用用具","医用x光机","医用离心机","医用制氧机","医用雾化器","医用显示器","医用床","医用病床","医用吊塔","医用护理床","医用夹板","医用胶片","医用门","简易呼吸机","新生儿呼吸机","正压呼吸机","双水平呼吸机","进口呼吸机","洗胃机","全自动洗胃机","胃肠机","自动洗胃机","电子胃镜","胃肠镜","超声胃镜","胃镜","鼻胃管","心电图机","心脏除颤器","心脏起搏器","心电仪","心电图仪","胎心监护仪","心导管","中心静脉置管","中心静脉导管","超声心动图","有创呼吸机","人工呼吸机","无创呼吸机","呼吸麻醉机","外科手术器械","手术器","手术室器械","手术显微镜","输尿管镜手术","手术室设备","手术无影灯","手术灯","手术床","电动手术床","电动手术台","手术车","手术包","手术钳","输血器","血压传感器","止血器","采血器","血浆分离器","血球仪","血细胞分析仪","血球分析仪","血球计数仪","血凝仪","血气分析仪","血凝分析仪","血沉仪","血压监测仪","全自动血凝仪","血氧检测仪","血压监护仪","脉搏血氧仪","血氧仪","血型分析仪","动态血压仪","血氧饱和度仪","血压测量仪","血糖检测仪","血管钳","真空采血管","超声止血刀","电子血压计","全自动血压计","脉搏波血压计","血液透析机","血液回收机","血液透析器","血液过滤器","血液分析仪","晨检仪","治疗仪","输液泵","MR","静脉输液泵","牵引床","妇科检查床","自控镇痛泵","多功能产床","医疗床","牙科椅","治疗台","注射泵","双极电凝镊","康复理疗设备","放疗设备","牙科治疗椅","牙科x光机","数字x光机","血透机","治疗机","透析机","便携式x光机","蜡疗机","CT机","膨宫机","中药熏蒸机","动物麻醉机","排痰机","体外碎石机","造影机","超声清洗机","血管造影机","便携式b超机","病理切片机","超声波洁牙机","数字乳腺机","胶囊填充机","除颤器","临时起搏器","起搏器","手术器械","电动吸痰器","icd起搏器","穿刺器","医院病床","急救担架","刺激器","aed除颤仪","除颤仪","电除颤仪","除颤监护仪","超声波治疗仪","冲击波治疗仪","超声治疗仪","超短波治疗仪","臭氧治疗仪","超声探伤仪","tdp治疗仪","艾灸治疗仪","比浊仪","鼻内窥镜","超声内镜","鼻内镜","电耳镜","鼻氧管","病床","产床","b超探头","mr核磁共振","便携式彩超","妇科治疗仪","电刺激器","激光美容仪器","光子治疗仪","黄疸治疗仪","红外线治疗仪","光子嫩肤仪","红蓝光治疗仪","红光治疗仪","毫米波治疗仪","激光理疗仪","妇科腹腔镜","喉镜","电动护理床","喉罩","加药计量泵","微波治疗仪器","口腔器械","阴道扩张器","胰岛素注射器","疼痛治疗仪","微波治疗仪","神灯治疗仪","电针治疗仪","电灼光治疗仪","脂肪测定仪","深部热疗仪","诊断仪","肛肠治疗仪","电子内窥镜","电子阴道镜","口腔内窥镜","经皮镜","肛肠镜","经皮肾镜","电子肛门镜","电子喉镜","支气管镜","椎间孔镜","支气管纤维镜","电子肠镜","乙状结肠镜","角膜镜","角塑镜","结肠镜","阴道镜","电子镇痛泵","解剖台","止痛泵","肾镜","腔镜","管镜","耳镜","骨蜡","胃管","监护仪","呼吸机","麻醉机","细胞仪","凝血剂","黑白超","x光机","牵引机","理疗灯","导药仪","输液器","氧合器","吸痰器","持针器","康复器","超声仪","电泳仪","蜡疗仪","脉氧仪","集菌仪","氮吹仪","膨宫仪","关节镜","电切镜","内窥镜","膀胱镜","纤支镜","耳内镜","乳腔镜","食管镜","涎腺镜","肾软镜","输液管","插尿管","肾小管","氩气刀","膨宫泵","电凝刀","输液椅","膜片钳","多勒普","持针钳","氩氦刀","人造骨","氮气计","肺量仪","灌注液","胰腺镜","脑窦镜","胆道镜","大肠镜","子宫镜","直肠镜","羊水镜","磁疗机","脉象仪","舌相仪","电麻仪","血型卡","酶免仪","染色机","包埋机","采血针","采血笔","滤血器","聚髌器","节育环","人工肾","人工喉","助听器","化疗泵","灌肠机","洗肠机","矫治器","骨水泥","鼻饲管","肛门管","洗耳球","舌象仪","导尿管","生化试剂","尿液分析","黑白超声","超声附件","兽用监护","兽用超声","血液细胞","呼吸面罩","康复器械","骨密度仪","肺功能仪","透光率仪","消化内镜","麻醉喉镜","输尿管镜","膝关节镜","透射电镜","胶囊内镜","气管插管","静脉置管","食管支架","超声设备","康复设备","高频电刀","超声骨刀","医学影像","脑电图机","脑电阻仪","肌电图机","眼动图仪","心音图仪","舌音图仪","胃电图仪","椎间盘镜","视网膜镜","屈光度仪","手术电极","防打鼾器","磁疗器具","肾功能仪","心功能仪","防护眼镜","血流变仪","透析血路","血管支架","胆道支架","食道支架","胰岛素泵","血液滤网","麻醉导管","PACS","彩色多普勒","x光透视机","凯氏定氮仪","低速离心机","超速离心机","简易呼吸器","空气消毒器","雾化吸入器","精密输液器","腰椎牵引器","超声雾化器","皮肤缝合器","生物传感器","超声波仪器","超声清洗器","菌落计数器","细菌过滤器","测温传感器","颈椎牵引器","流式细胞仪","尿液分析仪","射频治疗仪","离子色谱仪","微波消解仪","胎儿监护仪","微波理疗仪","温度校验仪","核磁共振仪","恒温混匀仪","母乳分析仪","x光检测仪","余氯分析仪","拉曼光谱仪","多肽合成仪","输尿管软镜","高清内窥镜","电子显微镜","管道内窥镜","硅胶导尿管","深静脉置管","输尿管导管","三腔导尿管","经颅多普勒","腰椎牵引床","静脉推注泵","静脉留置针","颈椎牵引椅","防褥疮床垫","骨科牵引架","超声波探头","心电分析仪","心电遥测仪","心电工作站","脑地形图仪","眼震电图仪","小儿测听计","病人监护仪","血氧饱和度","分娩监护仪","脑血流图仪","方波生理仪","有创内窥镜","阴道显微镜","直肠显微镜","医用放大镜","超声手术刀","超声结肠镜","超声听诊器","超声去脂仪","超声治疗机","超声按摩仪","超声洁牙机","激光治疗仪","激光诊断仪","激光检测仪","微波手术刀","微波治疗机","短波治疗机","音频电疗机","差频电疗机","光谱治疗仪","骨科牵引器","心率反馈仪","视力训练仪","弱视治疗仪","痛阈测量仪","经络分析仪","综合电针仪","定量针麻仪","全身CT机","螺旋CT机","电动导管床","电动断层床","高压注射器","核素听诊器","半自动血栓","血糖分析仪","血液粘度计","血栓弹力仪","生化分析仪","药敏分析仪","细胞电泳仪","血气采血器","精子分析仪","血球记数板","高速离心机","激光采血机","人工心肺机","膜式氧合器","贮血滤血器","气泡去除器","血液解毒器","单采血浆机","腹水浓缩机","腹膜透析机","腹膜透析管","脑动脉瘤夹","血管吻合夹","前列腺支架","手术机器人","气动呼吸机","同步呼吸机","综合麻醉机","小儿麻醉机","流产吸引器","治疗手术床","电动洗胃机","自控洗胃机","胃脏冲洗器","手术照明灯","手术反光灯","骨科整形床","综合治疗台","电动牙钻机","涡轮牙钻机","液压牙科椅","机械牙科椅","医用洁牙机","根管治疗仪","强力吸引器","医用空压机","牙模测试仪","银汞调和器","口腔手术灯","口腔照明灯","吸氧调节器","手摇式病床","干热灭菌器","微波灭菌柜","煮沸消毒器","液氮冷疗机","医用几丁糖","皮肤缝合钉","静脉输液针","胸腔引流管","腹腔引流管","胆管引流管","支气管插管","十二指肠管","阴道洗涤器","核医学成像","骨膜剥离器","单腔导尿管","麻醉呼吸机","医用传感器","数字化手术室","生化免疫分析","超声波清洗机","实验室离心机","原子力显微镜","手持式显微镜","紫外线杀菌灯","led轨道灯","紫外线灭菌灯","等离子切割器","微量氧分析仪","超声波清洗仪","紫外线光疗仪","紫外线治疗仪","x射线检测仪","浮游菌采样器","下腔静脉滤器","电解质分析仪","肺功能检测仪","肺功能测试仪","毛细管电泳仪","多参数监护仪","光化学反应仪","骨密度检测仪","颅内压监测仪","尿沉渣分析仪","脑立体定位仪","红外线理疗仪","氦质谱检漏仪","多功能艾灸仪","纤维支气管镜","裂隙灯显微镜","气管插管喉镜","便携式显微镜","电泳涂装设备","多功能护理床","感染实时监控","数字放射成像","颅内压监护仪","单导心电图机","多导心电图机","胎儿心电图机","心电向量图机","晚电位测试仪","心电标测图仪","脑电波分析仪","视网膜电图仪","胃肠电流图仪","氧浓度测定仪","阻抗血流图仪","电磁血流量计","微电极控制器","微电极监视器","经尿道电切镜","角膜地形图仪","电弱视助视器","内窥镜冷光源","微循环显微镜","裂隙灯工作台","视野计工作台","超声骨密度仪","超声骨强度仪","超声骨测量仪","超短波治疗机","空气加压氧舱","氧气加压氧舱","场效应治疗仪","紫外线治疗机","红外线治疗机","简易防打鼾器","磁感应电疗机","X射线CT管","电动摄影平床","防散射滤线栅","荧光摄影装置","胸片摄影装置","伽玛照相机","γ射线探测仪","性腺防护器具","X射线防护椅","全自动涂片机","血小板聚集仪","红细胞变形仪","半自动酶标仪","全自动电泳仪","中高压电泳仪","电化学测氧仪","PCR扩增仪","厌氧培养装置","红白血球吸管","鼓泡式氧合器","血液透析装置","血液滤过装置","血液净化管路","血路塑料泵管","动静脉穿刺器","植入式助听器","外挂式人工喉","脑立体定向仪","早产儿培养箱","输卵管通气机","电动牙科椅","氧浓度监察仪","氧气减压装置","超声消毒设备","脏器冷藏装置","义齿基托树脂","牙本质粘合剂","牙釉质粘合剂","氧化锌水门汀","齿科铸造合金","齿科锻造合金","方丝弓矫治器","细丝弓矫治器","输卵管粘堵剂","药液过滤滤膜","空气过滤滤膜","脑积液分流管","气管切开插管","无菌医用手套","颈椎直角骨凿","治疗X射线机","心功能检测仪","栅极X射线管","床旁输液工作站","内窥镜手术器械","超高效液相色谱","有创心输出量计","运动心电功量计","心电多相分析仪","长程心电记录仪","麻醉气体监护仪","呼吸功能监护仪","呼吸功能测试仪","弥散功能测试仪","微量气体分析器","无创心输出量计","电子血压脉搏仪","动态血压监护仪","医用红外热象仪","塑形角膜接触镜","夜间视觉检查仪","前房深度测定仪","各类手术显微镜","各种手术放大镜","超声肿瘤聚焦刀","超声脂肪乳化仪","超声三维诊断仪","全数字化彩超仪","超声彩色多普勒","超声心内显像仪","经颅超声多普勒","胎儿综合监护仪","超声产科监护仪","穿刺超声换能器","电子线阵换能器","机械扫描换能器","凸阵扫描换能器","食管超声换能器","超声穴位治疗机","超声骨折治疗机","眼科激光光凝机","激光血管焊接机","激光眼科诊断仪","眼科激光扫描仪","激光血液分析仪","激光显微手术器","氦氖激光治疗机","氦镉激光治疗机","激光针灸治疗仪","后尿道电切开刀","高频眼科电凝器","高频息肉手术器","高频鼻甲电凝器","射频控温热凝器","高频腋臭治疗仪","高频痔疮治疗仪","高频妇科电熨器","微波肿瘤热疗仪","肿瘤射频热疗机","强光辐射治疗仪","高压电位治疗仪","胸背部矫正装置","肌电生物反馈仪","温度生物反馈仪","耳穴探测治疗机","X射线诊断设备","螺旋扇扫CT机","X射线像增强器","医用透视荧光屏","X射线摄影暗匣","X射线暗室设备","医用回旋加速器","医用中子治疗机","医用质子治疗机","放射治疗模拟机","闪烁分层摄影仪","放射免疫测定仪","血红蛋白测定仪","流式细胞分析仪","特定蛋白分析仪","化学发光测定仪","荧光免疫分析仪","结核杆菌分析仪","快速细菌培养仪","血气酸碱分析仪","生物芯片阅读仪","冷冻超速离心机","血型专用离心机","冷冻高速离心机","自动组织脱水机","中空纤维透析器","中空纤维滤过器","人体血液处理机","血液成分分离机","脊柱内固定器材","人工肛门封闭器","人工肝支持装置","高频喷射呼吸机","各种立式麻醉机","电动综合手术台","电动间隙牵引床","各种胃肠减压器","牙髓活力测试仪","吸排氧三通阀箱","电动多功能病床","口腔科消毒设备","电热煮沸消毒器","肝脏冷冻治疗仪","冷冻低温治疗机","低温变速降温仪","低温生物降温仪","血液制品冷藏箱","冷冻干燥血浆机","真空冷冻干燥箱","造牙粉及造牙水","氧化锌印模糊剂","透明质酸钠凝胶","聚乳酸防粘连膜","血管吻合粘合剂","双腔气囊导尿管","麻醉机用呼吸囊","椎体前方剥离器","心内希氏束电图机","心内外膜标测图仪","有创性电子血压计","心电图综合测试仪","心率变异性检测仪","综合肺功能测定仪","肺通气功能测试仪","生物电脉冲分析仪","红外线乳腺诊断仪","胰腺等电子内窥镜","直接和间接检眼镜","诊断用纤维内窥镜","观察用硬管内窥镜","体表微循环显微镜","超声内窥镜多普勒","多功能超声监护仪","血管内超声换能器","B型伪彩色显示仪","固体激光手术设备","气体激光手术设备","氮分子激光治疗仪","激光多普勒血流计","半导体激光治疗机","高频扁桃体手术器","内窥镜高频手术器","高频五官科电熨器","微波前列腺治疗仪","射频前列腺治疗仪","电化学癌症治疗机","光量子血液治疗机","特定电磁波治疗机","中低频理疗用电极","X射线深部治疗机","X射线浅部治疗机","X射线接触治疗机","X射线头部CT机","旋转阳极X射线管","放射性核素扫描仪","甲状腺功能测定仪","放射性核素透视机","全自动生化分析仪","半自动生化分析仪","多项电解质分析仪","全自动免疫分析仪","全自动血气分析仪","组织氧含量测定仪","血氧饱和度测试仪","血液透析滤过装置","多层平板型透析器","血液成份输血装置","新生儿运输培养箱","手提式氧气吸入器","手提式氧气发生器","牙根管长度测定仪","预真空蒸汽灭菌器","超声干燥脱水设备","直肠癌低温治疗仪","宫腔冷冻治疗仪","压缩式冷冻治疗仪","血液成分分离器材","数字化超声工作站","太行血液分析流水线","糖化血红蛋白分析仪","有创多导生理记录仪","实时心律分析记录仪","脑电实时分析记录仪","心脏工作站电刺激器","气囊式体外反搏装置","超声眼科乳化治疗仪","超声乳腺热疗治疗仪","血管内超声波诊断仪","超声眼科专用诊断仪","眼晶体激光乳化设备","激光荧光肿瘤诊断仪","射频消融心脏治疗仪","体内低频脉冲治疗仪","高压低频脉冲治疗机","低频电磁综合治疗机","电子穴位测定治疗仪","医用间接摄影荧光屏","电动立柱式支撑装置","医用电子直线加速器","全自动血细胞分析仪","半自动血细胞分析仪","血液流变参数测试仪","幽门螺旋杆菌测定仪","经皮血氧分压监测仪","术中自体血液回输机","滚柱式离心式输血泵","辐射式新生儿抢救台","耳鼻喉科检查治疗台","医用伽玛射线灭菌器","自动高压蒸汽灭菌器","立式压力蒸汽灭菌器","体内肿瘤低温治疗仪","放射性核素治疗装置","五分类血液细胞分析仪","三分群血液细胞分析仪","生物电脉冲频率分析仪","超声血管内介入治疗仪","复合式扫描超声诊断仪","心腔内超声导管换能器","超声波妇科皮肤治疗仪","激光肿瘤光谱诊断装置","射频消融前列腺治疗仪","探穴针麻机穴位测试仪","医用固定阳极X射线管","X射线胶片自动冲洗机","核素后装近距离治疗机","全自动凝血纤溶分析仪","自动尿液分析仪及试纸","血液净化体外循环血路","手提式压力蒸汽灭菌器","轻便型自动气体灭菌器","激光血管内照射治疗仪","X射线计算机断层摄影","全自动干化学尿液分析仪","心率失常分析仪及报警器","肺内气体分布功能测试仪","心脏血管功能综合测试仪","B型电子线阵超声诊断仪","B型机械扇扫超声诊断仪","其他激光源内照射治疗仪","角膜板层刀","X射线检查用电动胃肠床","正电子发射断层扫描装置","单光子发射断层扫描装置","放射性同位素设备准直器","X射线防护屏等防护装置","全自动多项电解质分析仪","医用冷光纤维导光手术灯","卧式圆形压力蒸汽灭菌器","卧式矩形压力蒸汽灭菌器","脉动真空压力蒸汽灭菌器","病房用高压电离灭菌设备","自动控制电热煮沸消毒器","激光视网膜传递函数测定仪","手术室用高压电离灭菌设备","辐射治疗机常规光源治疗机","全自动化学发光免疫分析仪","手术刀","皮片刀","疣体剥离刀","剃毛刀","皮屑刮刀","解剖刀","普通手术剪","组织剪","综合组织剪","拆线剪","石膏剪","解剖剪","纱布绷带剪","教育用手术剪","普通止血钳","小血管止血钳","蚊式止血钳","组织钳","硬质合金镶片持针钳","普通持针钳","创夹缝拆钳","皮肤轧钳","子弹钳","纱布剥离钳","海绵钳","帕巾钳","皮管钳","器械钳","小血管镊","无损伤镊","组织镊","整形镊","持针镊","保健镊","拔毛镊","帕巾镊","敷料镊","解剖镊","止血夹","动脉瘤针","探针","推毛针","植毛针","挑针","教学用直尖针","静脉拉钩","创口钩","扁平拉钩","双头拉钩","皮肤拉钩","解剖钩","刀片夹持器","麻醉口罩","麻醉开口器","照明吸引器头","粉刺取出器","黑头粉刺压出器","皮肤刮匙","皮肤套刮器","皮肤刮划测检器","皮肤检查尺","皮肤组织钻孔器","开口器","卷棉子","显微喉刀","显微剪","显微枪形手术剪","显微组织剪","显微枪形麦粒钳","显微喉钳","显微持针钳","显微镊","显微持针镊","显微止血夹","显微耳针","显微喉针","显微耳钩","显微喉钩","显微合拢器","脑神经刀","可拆卸式脑膜刀","脑膜刀","肿瘤摘除钳","脑组织咬除钳","银夹钳","U型夹钳","动脉瘤夹钳","脑膜镊","垂体瘤镊","肿瘤夹持镊","脑膜钩","脑膜拉钩","神经钩","神经根拉钩","交感神经钩","脑刮匙","脑垂体刮匙","脑活检抽吸器","脑膜剥离器","脑吸引器","后颅凹牵开器","手摇颅骨钻","脑打针锤","脑压板","角膜剪","眼用手术剪","眼用组织剪","晶体植入钳","环状组织钳","角膜镊","眼用镊","眼用结扎镊","眼用钩针","玻璃体切割器","眼用板铲","眼用固定环","开睑器","耳鼓膜刀","鼻粘膜刀","扁桃体刀","酒渣鼻切割刀","鼻骨凿","乳突平骨凿","上颌窦对孔凿","耳用骨凿","扁桃体剪","甲状腺剪","喉头剪","中耳剪","鼻剪","扁桃体止血钳","枪式间接喉钳","筛窦钳","耳钳","双关节鼻中隔咬骨钳","甲状腺三爪钳","鼻咽活体取样钳","喉用敷料镊","耳用膝状镊","鼻用枪状镊","扁桃体止血夹","喉部微型手术钩","耳用探针","双头鼓式探针","扁桃体拉钩","鼻腔拉钩","扁桃体吸引管","乳突吸引管","乳突牵开器","麻醉咽喉镜","支撑喉镜","耳单头刮匙","音叉","鼻镜","牙龈刀","水门调刀","粘固粉调刀","银汞雕刻刀","牙骨凿","阻生牙骨凿","牙釉凿","牙龈剪","全冠剪","拔牙钳","切断牙钳","牙槽咬骨钳","舌钳","扩大钳","残根镊","牙用镊","长镊","成形片夹","牙探针","脓肿探针","牙周袋探针","牙挺","丁字形牙挺","牙根尖挺","牙用锉","牙骨锤","牙刮匙","根管充填器","牙骨膜分离器","牙龈分离器","洁治器","刮治器","剔挖器","研光器","粘固粉充填器","银汞合金充填器","去冠器","口镜","拔髓针柄","水枪头","热气枪头","吹火管","咬合器","印模托盘","汞合金输送器","磨牙带环就位器","结扎杆","带环推子","弓丝成型器","测量器","胸骨刀","心脏手术剪","胸骨剪","肋骨剪","心内膜心肌活组织钳","心房侧壁钳","主动脉侧壁钳","主动脉阻断钳","主动脉止血钳","主动脉游离钳","无损伤肺动脉止血钳","无损伤动脉止血钳","无损伤动脉导管钳","动脉侧壁钳","动脉阻断钳","静脉阻断钳","腔静脉钳","腔静脉游离钳","主肺动脉钳","心房持针钳","胸腔止血钳","胸腔组织钳","三角肺叶钳","结扎钳","双关节肋骨咬骨钳","大隐静脉镊","心房止血器","心耳止血器","凹凸齿止血夹","胸腔镊","胸腔组织镊","肺组织镊","心房（心室）拉钩","二尖瓣膜拉钩","排气针","血管打洞钳（器）","心房打洞器","二尖瓣扩张器","血管扩张器","血管牵开器","胸骨手钻","双头剥离匙","肋骨骨膜剥离子","内膜剥离器","心内吸引器","左房引流管","冠状动脉吸引器","冠状动脉灌注器","大隐静脉冲洗管","静脉撑开器","短柄吸引器","胃内手术剪","直角剪","胆石钳","脾蒂钳","腹膜钳","胃组织取样钳","胆道拉钩","双头腹壁拉钩","阑尾拉钩","气腹针","荷包成型器","压肠板","胆石匙","胆道探条","腹壁固定牵开器","肠剪","膀胱切除剪","前列腺剪","血管阻断钳","骼血管阻断钳","骼静脉侧壁钳","肾蒂钳","肠钳","直肠活体取样钳","膀胱肿瘤匙钳","膀胱颈钳","痔核钳","膀胱拉钩","前列腺拉钩","肛门探针","尿道扩张器","肛门镜","阴茎夹","椎管铲刀","椎管锉刀","手锥","丝锥","髓腔铰刀","加压螺纹钉铰刀","截断刀","截骨刀","胫骨切刀","石膏刀","胫骨切割器","髋关节成型凹凸钻","钻头","铰孔钻","手枪式手摇骨钻","双关节棘突骨剪","双关节咬骨剪","骨剪","膝关节韧带手术剪","钢丝剪","颈椎咬骨钳","颈椎双关节咬骨钳","脊柱侧弯矫正钳","髓核钳","椎板咬骨钳","弯头平口棘突骨钳","枪形咬骨钳","膝关节息肉钳","咬骨钳","持骨钳","腐骨钳","复位钳","持钉钳","持板钳","持棒钳","持钩钳","螺杆夹持钳","撑开钳","压缩钳","枪形取样钳","骨克丝钳","钢板弯曲钳","钢丝钳","环锯","腰椎用梯形骨凿","椎间盘手术用环锯","椎板剥离器","颈椎测深凿","弓锯","指锯","骨锯","小园刮凿","丁字凿","骨锉","弧形凿","髋关节成型凿","石膏锯","梯形铲","肘关节肱骨成型骨凿","髓腔锉","椎管锉","骨凿","座导凿","单侧椎板拉钩","半月板钩","下肢截断拉钩","骨钩","颈椎拉钩","颈前路深部缝合针","骨牵引针","加压螺纹钉导引针","颈椎刮匙","可变神经剥离子器","风动开颅器","电池式自停颅骨钻","电动胸骨锯","电动骨钻","电动石膏剪","电动石膏锯","肢体延长架","多功能单侧外固定支架","碎胎刀","子宫剪","剖腹产剪","脐带剪","会阴剪","产钳","剖腹产切口钳","妇科组织钳","子宫颈活体取样钳","子宫夹持钳","妇科分离钳","胎盘钳","环形输卵管镊","子宫拉钩","阴道拉钩","断头钩","子宫探针","腹水穿刺针","碎颅器","输卵管通夜器","阴道牵开器","会阴牵开器","骨盆测量计","输精管分离钳","输精管皮外固定钳","节育环放置钳","子宫刮匙","输卵管提取板","穿刺针","玻璃注射器","辊轴取皮刀","鼻手术刀","指骨凿","颌骨夹持钳","肌腱夹持钳","肌腱穿刺钳","软骨塑型钳","皮肤镊","眼睑镊","唇夹","鼓式取皮机","烧伤植皮三用机","肌腱分离器","肌腱剥离器","筋膜套切器","皮肤疣圈断器","嘴形撑开器","电子体温计","红外耳蜗体温计","腋下体温计","皮肤体温计","液晶体温计","无创性电子血压计","立式血压计","血压表","小儿血压表","肺活量计","双简肺功能测定器","听诊器","额戴听诊器","胎音听诊器","打诊锤","脑打诊锤","多用途叩诊锤","额戴反光镜","电额灯","反光喉镜","聚光灯","反光灯","检眼灯","头灯","卡片投影仪","视力表灯","视力检查仪","遮眼器","标准视力字标","植入式心脏起搏器","体外心脏起搏器","心脏调搏器","主动脉内囊反搏器","心脏除颤起搏仪","体外震波碎石机","血流变数据处理软件","激光(血液分析仪","激光全息检测仪)数据分析软件","远程诊断","血管内造影导管","球囊扩张导管","套针外周导管","微型漂浮导管","动静脉测压导管","造影导管","球囊导管","PTCA导管","PTA导管","微导管","溶栓导管","指引导管","消融导管","追踪球囊","硬导丝","软头导丝","肾动脉导丝","微导丝","推送导丝","超滑导丝","动脉鞘","静脉血管鞘","微穿刺血管鞘","弹簧栓子","栓塞微球","铂金微栓子","封堵器","医用冰箱","血分析","灭菌器","手术刀柄","柳叶刀","铲刀","挑刀","锋刀","修脚刀","修甲刀","手术刀片","简易镊","灭菌柜","医用培养箱","凝血分析仪","生物安全柜","切开刀","无菌刀片","输氧面罩","吸入器","吸引管","引流管","导尿包","吸痰包","肠镜","电子镜","腹腔镜","宫腔镜","鼻咽喉镜","胸腔镜","肠胃镜","十二指肠镜","光学放大镜","小肠镜","脑室镜","腰间盘镜","腰椎间盘镜","椎间镜","鼻窦镜","食道镜镜","子宫镜镜","消化道镜","肾孟镜","插管镜","肠内镜","电子内镜","胆道内镜","腹腔内镜","胃肠内镜","胃内镜","膀胱内镜","输尿管内镜","电切内镜","肾内镜","宫腔内镜","阴道内镜","支气管内镜","鼻咽喉内镜","胸腔内镜","肠胃内镜","十二指肠内镜","光学放大内镜","小肠内镜","关节内镜","脑室内镜","腰间盘内镜","腰椎间盘内镜","椎间孔内镜","椎间内镜","鼻窦内镜","喉内镜","食道镜内镜","子宫镜内镜","结肠内镜","消化道内镜","肛肠内镜","肾孟内镜","大肠内镜","插管内镜","制氧机","制氮机","氧气机","氧气呼吸机","消毒机","洗板机","吸氧机","伟康呼吸机","万曼呼吸机","碎石机","乳腺机","清洗机","离心机","机械通气","光固化机","纯水机","透视机","水机","健康一体机","射线机","AED训练机","医用胶片打印机","封口机","牙科手机","磨边机","注射器","助行器","荧光检测器","移液器","消毒器","过滤器","雾化器","输血器具","试验器","生物容器","生命科学仪器","清洗器","牵引器","器械柜","喷雾器","矫正器","监测仪器","美容仪器","呼吸器","骨科医疗器械","骨科器械","干式恒温器","负压引流器","负压吸引器","电凝器","电动吸引器","麻醉蒸发器","供氧器","呼吸训练器","氧气吸入器","内窥镜图像处理器","光学放大器","洗眼器","生物阅读器","站立器","器械镊","液仪器","分液器","冲洗器","灌注器","利器盒","圈套器","质谱仪","血分析仪","心电监护仪","细胞计数仪","生化仪","筛查仪","色谱仪","溶氧仪","尿仪","密度仪","酶标仪","粒度仪","理疗仪","局放仪","检测仪","监测仪","甲烷检测仪","光疗仪","干式氮吹仪","分析仪","定氮仪","电穿孔仪","探伤仪","测氧仪","测试仪","采集仪","记录仪","黄疸仪","脑电图仪","止血仪","复苏仪","静脉显像仪","康复仪","熏蒸仪","训练仪","学位电刺激仪","验光仪","口腔扫描仪","观察扫描仪","荧光定量仪","成像仪","显微镜","微镜","塑形镜","塑形角膜镜","生物显微镜","扫描电镜","软镜","盘镜","内镜","放大镜","额镜","电镜","检查镜","医用低温保存柜","全自动核酸提取仪","医学设备","医学仪器","医学器械","医学装备","医学装置"};
        String[] ddd = {"维保","维修","保养","球管","探头","配件"};
        String[] blacks = {"耗材","救护车"};

        for (String aa : aaa) {
            for (String bb : bbb) {
                futureList1.add(executorService1.submit(() -> {
                    String key = aa + "&" + bb;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201222] AND (progid:[0 TO 3] OR progid:5) AND province:25 AND catid:[* TO 100] AND allcontent:\"" + aa + "\" AND title:\"" + bb + "\" ", key, 5);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : blacks) {
                                    if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag) {
                                    list1.add(data);
                                    data.setKeyword(key);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
        }

        for (String cc : ccc) {
            for (String dd : ddd) {
                futureList1.add(executorService1.submit(() -> {
                    String key = cc + "&" + dd;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201222] AND (progid:[0 TO 3] OR progid:5) AND province:25 AND catid:[* TO 100] AND allcontent:\"" + cc + "\" AND title:\"" + dd + "\" ", key, 6);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : blacks) {
                                    if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag) {
                                    list1.add(data);
                                    data.setKeyword(key);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            for (String bb : bbb) {
                String key = aa + "&" + bb;
                arrayList.add(key);
            }
        }
        for (String cc : ccc) {
            for (String dd : ddd) {
                String key = cc + "&" + dd;
                arrayList.add(key);
            }
        }
        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //美敦力
    public void getMeiDunLiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] bbb = {"新建","扩建","改建","改造"};

        for (String bb : bbb) {
            futureList1.add(executorService1.submit(() -> {
                String key = bb;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201224] AND (progid:3 OR progid:5) AND catid:[* TO 100] AND title:\"" + bb + "\"", key, 22);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String bb : bbb) {
            String key = bb;
            arrayList.add(key);
        }


        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave6(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //蓝谷生物
    public void getLanGuSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] bbb = {"穴位压力刺激贴","术后排气","纱布绷带","弹力绷带","石膏绷带","创口贴","手术衣","手术帽","口罩","手术垫单","手术洞巾","橡皮膏","透气胶带","肛门袋","集尿袋","引流袋","检查手套","指套","洗耳球","阴道洗涤器","气垫","肛门圈","咬合纸","合金助焊剂","齿科分离剂","氧气袋","输氧面罩","鼻氧管","医用棉签","诺氟沙星胶囊","酒石酸美托洛尔片","制霉素片","卡托普利片","地奥心血康胶囊","碘胺甲亚唑-甲氧苄啶片","复方丹参片","尼莫地平片","尼群地平片","地高辛片","双嘧达莫片","盐酸多西环素片","氨甲苯酸片","呋喃唑酮片","硝苯地平片","头孢拉定胶囊","复方利血平氨苯蝶啶片","阿莫西林胶囊","盐酸环丙沙星片","甲硝唑片","醋酸甲萘氢醌片","维生素B2片","马来酸依那普利片","丙硫氧嘧啶片","盐酸溴已新片","辛伐他汀片","枸橼酸喷托维林片","氨茶碱片","复方甘草片","双氯芬酸钠胶囊","格列本脲片","氢氯噻嗪片","呋塞米片","盐酸二甲双胍片","阿司匹林肠溶片","马来酸氯苯那敏片","雷公藤多苷片","赛庚啶片","法莫替丁片","雷尼替丁胶囊","复方地芬诺酯片","多潘立酮片","阿苯达唑片","联苯双酯滴丸","醋酸甲羟孕酮片","甲睾酮片","酚酞片","碳酸氢钠片","甲巯咪唑片","大黄碳酸氢纳片","甲状腺片","醋酸地塞米松片","泼尼松片","普乐安片","他莫昔芬片","枸橼酸他莫昔芬片","已烯雌酚片","多潘立酮混悬液","苯妥因钠片","复方甘草口服溶液","丙戊酸钠片","葡萄糖酸钙口服液","吲哚美辛栓","制霉菌素阴道泡腾片","红霉素眼膏","三九胃泰冲剂","克霉唑软膏","蒙脱石散","硝酸咪康唑乳膏","头孢拉定干糖浆","红霉素软膏","硝酸咪康唑栓","口服补液盐","甲硝唑栓","益母草冲剂","阿奇霉素颗粒","开塞露","氯霉素眼药水","氯霉素滴眼液","0.9%氯化钠注射液","葡萄糖注射液","5%葡萄糖氯化钠针","复方氯化钠钠注射液","环丙沙星针","甲硝唑针","右旋糖酐40葡萄糖注射液","甘露醇注射液","马应龙麝香痔疮膏","复方氨基酸[18AA-1]注射液","碳酸氢钠针","注射用青霉素钠","青霉素钠针","硫酸阿米卡星注射液","破伤风抗毒素注射液","注射用苯唑西林钠","垂体后叶素针","注射用乳糖酸红霉素","胰岛素注射液","注射用磷霉素钠","盐酸林可霉素注射液","缩宫素注射液","苯丙酸诺龙针","注射用氨苄西林","黄体酮注射液","黄体酮针","绒促性素针","注射用绒促性素","醋酸地塞米注射液","注射用头孢曲松钠","卡铂针","维生素D3注射液","维生素D3针","维生素K1注射液","注射用头孢呋辛钠","维生素B6注射液","维生素B6针","维生素B12注射液","维生素B12针","丝裂霉素针","维生素B1注射液","维生素C注射液","注射用环磷酰胺","葡萄糖酸钙注射液","甲氨蝶呤针","注射用哌拉西林钠","注射用头孢噻肟钠","硫酸庆大霉素注射液","氟尿嘧啶针","去乙酰毛花苷丙针","去乙酰毛花苷丙注射液","盐酸阿霉素针","盐酸多巴酚丁胺注射液","尼可刹米针","呋塞米注射液","重酒石酸去甲肾上腺素注射液","利血平注射液","盐酸肾上腺素注射液","盐酸肾上腺素针","盐酸氯胺酮注射液","甲磺酸酚妥拉明注射液","盐酸利多卡因注射液","盐酸利多卡因针","盐酸普鲁卡因注射液","盐酸多马胺注射液","盐酸布比因注射液","盐酸洛贝林注射液","盐酸异丙嗪针","盐酸异丙嗪注射液","茵栀黄注射液","清开灵注射液","胞磷胆碱针","氨茶碱针","氨茶碱注射液","硫酸镁注射液","10%氯化钠注射液","碳酸氢钠注射液","参麦注射液","甲氧氯普胺注射液","西咪替丁注射液","肌苷针","复方利血平片","50%葡萄糖注射液","亚甲蓝针","盐酸纳络酮注射液","氯化钾注射液","盐酸哌替啶针","盐酸哌替啶注射液","盐酸吗啡注射液","枸橼酸芬太尼注射液","克林霉素针","地西泮片","艾司唑仑片","己烯雌酚针","苯巴比妥片","苯巴比妥针","地西泮针","氯化琥珀胆碱针","阿普唑仑片","消旋山莨菪碱片","硫酸阿托品片","盐酸氯丙嗪注射液","盐酸氯丙嗪针","盐酸麻黄碱注射液","异丙肾上腺素针","重酒石酸间羟胺注射液","甲硫酸新斯的明注射液","硫酸阿托品注射液","硫酸阿托品针","盐酸消旋山莨菪碱注射液","呋喃妥因片","丙酸睾酮针","注射用头孢唑林钠","氯化钾片","消炎利胆片","利巴韦林颗粒","氢化可的松针","葡萄糖酸钙片","布洛芬片","八珍益母胶囊","氨甲环酸针","乳酸左氧氟沙星注射液","乳酸左氧氟沙星针","肝素纳注射液","板兰根冲剂","牛黄解毒片","知柏地黄丸","六味地黄丸","珍珠明目液","利巴韦林滴眼液","甲氧氯普胺片","垂体后叶注射液","奥美拉唑胶囊","叶酸片","环孢素胶囊","环孢素口服溶液","硫唑嘌呤片","硝酸甘油注射液","氧氟沙星滴耳油","柴胡注射液","丙泊酚注射液","硝普钠注射液","维库溴铵注射液","硫酸鱼精蛋白注射液","克拉维酸钾/羟氨苄青霉素胶囊","丹参注射液","清开灵颗粒","克林霉素磷酸酯","盐酸丁卡因","碘海醇","双氯芬酸钠肠溶片","利巴韦林冻干粉","阿奇霉素分散片","维生素C针","尿激酶针","精蛋白锌重组人胰岛素混合注射液","甲硝唑注射液","复方氨基酸注射液","注射用头孢曲松钠(罗氏)","注射用头孢曲松钠（罗氏）","甘油果糖注射液","促皮质素针","环丙沙星胶囊","盐酸苯海索片","氟哌啶醇片","注射用硫酸阿米卡星","板蓝根冲剂","补中益气丸","逍遥丸","云南白药胶囊","复方丹参滴丸","妇科千金胶囊","护肝片","季德胜蛇药","锡类散","氟康唑胶囊","盐酸氨溴索口服液","克霉唑阴道片","乳酸左氧氟沙星片","苯巴比妥钠注射液","注射用头孢曲松钠（进口）","注射用头孢曲松钠(进口)","5%葡萄糖注射液","环孢素软胶囊","茵栀黄颗粒","阿昔洛韦胶囊","注射用甲氨蝶呤","益母草胶囊","碘普罗胺注射液","阿莫西林克拉维酸钾胶囊","注射用利巴韦林","头孢呋辛酯片","注射用磷霉素","速效救心丸","异氟烷吸入剂","三九胃泰颗粒","双黄连颗粒","硫酸钡（I）型干混悬剂","硫酸钡(I)型干混悬剂","板蓝根颗粒","甲硝唑氯化钠注射液(直立软袋)","甲硝唑氯化钠注射液（直立软袋）","氯化琥珀胆碱注射液","0.9%氯化钠注射液(直立软袋)","0.9%氯化钠注射液（直立软袋）","葡萄糖氯化钠注射液(直立软袋)","葡萄糖氯化钠注射液（直立软袋）","复方氯化钠(直立软袋)","复方氯化钠（直立软袋）","葡萄糖注射液(直立软袋)","葡萄糖注射液（直立软袋）","阿奇霉素肠溶片","5%葡萄糖注射液(直立软袋)","5%葡萄糖注射液（直立软袋）","10%葡萄糖注射液(直立软袋)","10%葡萄糖注射液（直立软袋）","双氯芬酸钠肠溶缓释胶囊","复方氨基酸注射液（软袋）","复方氨基酸注射液(软袋)","阿莫西林克拉维酸钾分散片","注射用氨甲环酸","硫酸沙丁胺醇吸入气雾剂","10%葡萄糖注射液","注射用盐酸丁卡因","吲达帕胺片","注射用氢化可的松琥珀酸钠","8%葡萄糖氯化钠注射液（软双）","8%葡萄糖氯化钠注射液(软双)","熊去氧胆酸胶囊","清开灵颗粒（无糖型）","清开灵颗粒(无糖型)","一类医疗耗材","甲类医疗报销"};

        for (String bb : bbb) {
            futureList1.add(executorService1.submit(() -> {
                String key = bb;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201201 TO 20201225] AND (progid:3 OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + bb + "\"", key, 22);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String bb : bbb) {
            String key = bb;
            arrayList.add(key);
        }


        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //ASP
    public void getASPSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"过氧化氢低温灭菌器"};
        String[] bbb = {"steris","史帝瑞","倍力曼","新华","老肯","凯斯普","白象"};

        for (String aa : aaa) {
            for (String bb : bbb) {
                futureList1.add(executorService1.submit(() -> {
                    String key = aa + "&" + bb;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190101 TO 20201231] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + aa + "\" AND allcontent:\"" + bb + "\" ", key, 1);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    list1.add(data);
                                    data.setKeyword(key);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            for (String bb : bbb) {
                String key = aa + "&" + bb;
                arrayList.add(key);
            }
        }


        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //高德智感
    public void getGaoDeSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"红外热像仪", "红外夜视仪", "红外热成像", "热像仪", "红外测温仪", "在线热像仪", "红外人体测温热像仪", "红外线测温仪"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201231] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 414);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList(aaa));

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //中通管业
    public void getZhongTongSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"华能招标","华阳新庄"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20180101 TO 20201231] AND (progid:3 OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 414);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList(aaa));

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave2(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //腾讯云
    public void getTengXunYunSolrAllField(String date) throws IOException {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        List<String> blacks = LogUtils.readRule("moneyFile");

        futureList1.add(executorService1.submit(() -> {
            List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20210105 TO 20210105] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] ", "", 1);
            if (!mqEntities.isEmpty()) {
                for (NoticeMQ data : mqEntities) {
                    if (data.getTitle() != null) {
                        boolean flag = true;
//                        for (String black : blacks) {
//                            if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
//                                flag = false;
//                                break;
//                            }
//                        }
                        if (flag) {
                            list1.add(data);
                            if (!dataMap.containsKey(data.getContentid().toString())) {
                                list.add(data);
                                dataMap.put(data.getContentid().toString(), "0");
                            }
                        }
                    }
                }
            }
        }));

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> {
                    try {
                        getDataFromZhongTaiAndSave3(content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    //数据
    public void getShuJuSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        futureList1.add(executorService1.submit(() -> {
            List<NoticeMQ> mqEntities = qyhyContentSolr.companyResultsBaoXian("userIds:2 AND yyyymmdd:[20201101 TO 20201131]", "", 414);
            if (!mqEntities.isEmpty()) {
                for (NoticeMQ data : mqEntities) {
                    if (data.getTitle() != null) {
                        boolean flag = true;
                        if (flag) {
                            list1.add(data);
                            if (!dataMap.containsKey(data.getContentid().toString())) {
                                list.add(data);
                                dataMap.put(data.getContentid().toString(), "0");
                            }
                        }
                    }
                }
            }
        }));

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }
}
















