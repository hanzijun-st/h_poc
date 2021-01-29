package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.QYHYContentSolr;
import com.qianlima.offline.util.QianlimaZTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
public class QyhyService {

    @Autowired
    private QYHYContentSolr qyhyContentSolr;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    private KaFenCiService kaFenCiService;

    private boolean isContainFlag(String unit, String containKeys) {
        boolean containFlag = false;
        if (StringUtils.isNotBlank(containKeys)) {
            String[] containArr = containKeys.split("、");
            for (String containStr : containArr) {
                if (unit.contains(containStr)) {
                    containFlag = true;
                    break;
                }
            }
        } else {
            containFlag = true;
        }
        return containFlag;
    }

    private boolean isExcludeFlag(String unit, String excludeKeys) {
        // 当排除此条件不为空时, 判断排除词条件
        boolean excludeFlag = true;
        if (StringUtils.isNotBlank(excludeKeys)) {
            String[] excludeArr = excludeKeys.split("、");
            for (String excludeStr : excludeArr) {
                if (unit.contains(excludeStr)) {
                    excludeFlag = false;
                    break;
                }
            }
        }
        return excludeFlag;
    }

    // 阿里根据contentid 查找数据源
    public void getSolrAllField() throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList1 = new ArrayList<>();

        ArrayList<String> ids = new ArrayList<>();

        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT content_id FROM source_url");
//        List<String> ids = LogUtils.readRule("moneyFile");
        if (mapList != null && mapList.size() > 0){
            for (Map<String, Object> map : mapList) {
                ids.add(map.get("content_id").toString());
            }
        }


        for (String id : ids) {
            futureList1.add(executorService1.submit(() -> {
                String sourceUrl = QianlimaZTUtil.getFromUrl("http://datafetcher.intra.qianlima.com/dc/bidding/fromurl",id);
//                String website = "";
//                String type = "";
//                if (ZTStringUtil.isNotBlank(sourceUrl)) {
//                    if (ZTStringUtil.isNotBlank(sourceUrl)) {
//                        Map<String, String> map = JudgeUtil.judgeForMap(sourceUrl);
//                        if (map != null){
//                            website = map.get("website");
//                            type = map.get("type");
//                        }
//                    }
//                }
//                String title = "";
//                String url = "";
//                List<Map<String, Object>> maps = gwJdbcTemplate.queryForList(ConstantBean.SELECT_TIME_ONE_NOW_02, id);
//                if (maps != null && maps.size() > 0){
//                    title = maps.get(0).get("title").toString();
//                    url = maps.get(0).get("url").toString();
//                }
                log.info("处理到了contentid：{}", id);
//                bdJdbcTemplate.update("INSERT into source_url (contentid, title, url, website, sourceUrl, type) values (?,?,?,?,?,?)", id, title, url, website, sourceUrl, type);
                bdJdbcTemplate.update("UPDATE source_url SET sourceUrl = ? WHERE content_id = ?", sourceUrl , id);

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
    }

    //获取标的物
    public void getXiFenSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList1 = new ArrayList<>();
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT content_id,title,content FROM jyf_data where task_id = 302");
        for (Map<String, Object> maps : mapList) {

            Long contentid = Long.valueOf(maps.get("content_id") != null ? maps.get("content_id").toString() : "");
            String title = maps.get("title") != null ? maps.get("title").toString() : "";
            String content = maps.get("content") != null ? maps.get("content").toString() : "";

            String infoTypeUrl = "http://cusdata.qianlima.com/api/infoType";

            futureList1.add(executorService1.submit(() -> {
                getDataType(title,content,contentid,infoTypeUrl);
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

    }

    /**
     * 调用中台接口, 获取结果类型细分
     * 06-答疑公告， 07-废标公告， 08-流标公告， 09-开标公示， 10-候选人公示， 11-中标通知， 12-合同公告， 13-验收合同， 14-违规公告， 15-其他公告
     * @param title 标题
     * @param content 正文
     * @param contentid
     * @param infoTypeUrl 获取结果类型细分接口的URI
     * @return
     */
    public String getDataType(String title, String content, Long contentid, String infoTypeUrl) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            HttpPost post = new HttpPost(infoTypeUrl);
            //创建参数列表
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("title", title));
            list.add(new BasicNameValuePair("content", content));
            post.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
            //url格式编码
            post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            //设置超时时间为60秒
            post.setConfig(requestConfig);
            //执行请求
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String entity = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                JSONObject jsonObject = JSON.parseObject(entity);
                //成功
                if (jsonObject.getInteger("code") == 0) {
                    String data = jsonObject.getString("data");
                    if (StringUtils.isNotEmpty(data)) {
                        bdJdbcTemplate.update("UPDATE jyf_data SET keyword = ? WHERE content_id = ? ", data, contentid);
                        log.info("contentId:{} 获取细分信息类型成功!!! ", contentid);
                    }
                } else {
                    log.error("调结果细分服务过程报错: {}, contentid: {}", jsonObject.get("msg"), contentid);
                    throw new RuntimeException("调用结果细分服务报错");
                }
            }
        } catch (Exception e) {
            log.error("结果细分判断出错:{}", e);
            throw new RuntimeException("结果细分判断出错");
        }
        return null;
    }

    private static final String KA_SELECT_SQL_01 = "SELECT id,zhao_biao_unit FROM jyf_data ";
    private static final String KA_UPDATE_SQL_01 = "UPDATE jyf_data SET S = ? WHERE id = ? ";

    //KA自用行业
    public void getKaHangYeSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList1 = new ArrayList<>();
        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(KA_SELECT_SQL_01);
        for (Map<String, Object> map : maps) {
            futureList1.add(executorService1.submit(() -> {
                try {
                    searchingHyAllData(map);
                } catch (IOException e) {
                    e.printStackTrace();
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

    }

    //KA自用行业___根据contentid匹配行业标签
    private void searchingHyAllData(Map<String, Object> map) throws IOException {

        String id = map.get("id") != null ? map.get("id").toString() : "";
        String zhaobiaounit = map.get("zhao_biao_unit") != null ? map.get("zhao_biao_unit").toString() : "";

        HttpClient client = new DefaultHttpClient();
        HttpResponse response = null;
        // --KA自用行业
        // http://cusdata.qianlima.com/api/ka/industry?unit=上海市公安局国际机场分局
        String url = "http://cusdata.qianlima.com/api/ka/industry?unit="+zhaobiaounit+"";
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");

        response = client.execute(post);
        String ret = null;
        ret = EntityUtils.toString(response.getEntity(), "UTF-8");

        System.out.println(ret);
        JSONObject parseObject= JSON.parseObject(ret);
        JSONObject data = parseObject.getJSONObject("data");
        String firstLevel = data.getString("firstLevel");
        String secondLevel = data.getString("secondLevel");
        bdJdbcTemplate.update(KA_UPDATE_SQL_01,firstLevel, id);
        log.info("contentId:{} =========== KA自用行业数据处理成功！！！ ",id);

    }

    private static final String TENCENT_SELECT_SQL = "SELECT id,zhao_biao_unit FROM lala_data ";
    private static final String TENCENT_UPDATE_SQL = "UPDATE lala_data SET keyword = ?,code = ?,keyword_term = ? WHERE id = ? ";

    //腾讯行业标签
    public void getTengXunhySolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList1 = new ArrayList<>();
        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(TENCENT_SELECT_SQL);
        for (Map<String, Object> map : maps) {
            futureList1.add(executorService1.submit(() -> {
                try {
                    searchingTengXunHyAllData(map);
                } catch (IOException e) {
                    e.printStackTrace();
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

    }

    //腾讯行业标签___根据contentid匹配行业标签
    private void searchingTengXunHyAllData(Map<String, Object> map) throws IOException {

        String id = map.get("id") != null ? map.get("id").toString() : "";
        String zhaobiaounit = map.get("zhao_biao_unit") != null ? map.get("zhao_biao_unit").toString() : "";

        HttpClient client = new DefaultHttpClient();
        HttpResponse response = null;
        // --腾讯行业
        // http://cusdata.qianlima.com/api/tencent/industry?unit=上海市公安局国际机场分局
        String url = "http://cusdata.qianlima.com/api/tencent/industry?unit="+zhaobiaounit+"";
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");

        response = client.execute(post);
        String ret = null;
        ret = EntityUtils.toString(response.getEntity(), "UTF-8");

        System.out.println(ret);
        JSONObject parseObject= JSON.parseObject(ret);
        JSONObject data = parseObject.getJSONObject("data");
        String firstLevel = data.getString("firstLevel");
        String secondLevel = data.getString("secondLevel");
        String thirdLevel = data.getString("thirdLevel");
        bdJdbcTemplate.update(TENCENT_UPDATE_SQL,firstLevel,secondLevel,thirdLevel, id);
        log.info("contentId:{} =========== 腾讯行业标签数据处理成功！！！ ",id);

    }

    private static final String YIDONG_SELECT_SQL_01 = "SELECT content_id,content,zhao_biao_unit,keyword_term,S,yldw FROM loiloi_data ";
    private static final String YIDONG_UPDATE_SQL_01 = "UPDATE loiloi_data SET yldw = ?,jydw = ? WHERE content_id = ? ";

    //移动行业标签
    public void getYiDongHangYeSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList1 = new ArrayList<>();
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList(YIDONG_SELECT_SQL_01);
        for (Map<String, Object> maps : mapList) {

            String contentId = maps.get("content_id") != null ? maps.get("content_id").toString() : "";
            String content = maps.get("content") != null ? maps.get("content").toString() : "";
            String zhaoBiaoUnit = maps.get("zhao_biao_unit") != null ? maps.get("zhao_biao_unit").toString() : "";
            String kaFirst = maps.get("keyword_term") != null ? maps.get("keyword_term").toString() : "";
            String kaSecond = maps.get("S") != null ? maps.get("S").toString() : "";
            String yldw = maps.get("yldw") != null ? maps.get("yldw").toString() : "";

            futureList1.add(executorService1.submit(() -> {
//                try {
//                    if(content != null || content != ""){
//                        zhongTaiBiaoDiWuService.getAllZhongTaiBiaoDIWu(contentId,content);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                if(kaFirst != null && (yldw == null || yldw == "" )){
                    getYiDongIndustry(kaFirst,kaSecond,zhaoBiaoUnit,contentId);
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

    }

    private static final String SQL_KEY = "SELECT first, second, ka_first, ka_second, contain, exclude FROM yidong_industry_mapping where ka_first = ? and ka_second = ?";

    //移动行业标签___根据contentid匹配行业标签
    private void getYiDongIndustry(String kaFirst, String kaSecond, String zhaoBiaoUnit, String contentId) {

        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(SQL_KEY, kaFirst, kaSecond);

        String first = "";
        String second = "";

        if (maps != null && maps.size() > 0){
            for (Map<String, Object> resultMap : maps) {
                first = resultMap.get("first") != null ? resultMap.get("first").toString() : "";
                second = resultMap.get("second") != null ? resultMap.get("second").toString() : "";
                String contain = resultMap.get("contain") != null ? resultMap.get("contain").toString() : "";
                String exclude = resultMap.get("exclude") != null ? resultMap.get("exclude").toString() : "";
                // 当包含词条件不为空时, 判断包含词条件
                boolean containFlag = isContainFlag(zhaoBiaoUnit, contain);
                // 当排除此条件不为空时, 判断排除词条件
                boolean excludeFlag = isExcludeFlag(zhaoBiaoUnit, exclude);
                // 当即满足包含词条件, 又满足排除词条件时, 返回对应的映射腾讯行业
                if (containFlag && excludeFlag){
                    bdJdbcTemplate.update(YIDONG_UPDATE_SQL_01,first ,second , contentId);
                    log.info("contentId:{} =========== 数据处理成功！！！ ",contentId);
                }
            }
        }
    }

    //IK分词
    public void getKaFenCiSolrAllField() {

        String SQL_KEY = "SELECT id ,title FROM `jyf_data`";

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList1 = new ArrayList<>();
        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(SQL_KEY);
        for (Map<String, Object> map : maps) {
            futureList1.add(executorService1.submit(() -> {
                try {
                    searchingKaFenCiAllData(map);
                } catch (Exception e) {
                    e.printStackTrace();
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
    }

    //IK分词___标题
    private void searchingKaFenCiAllData(Map<String, Object> map) throws Exception {

        String UPDATE_SQL_01 = "UPDATE jyf_data SET keyword = ?,code = ? WHERE id = ? ";

        String id = map.get("id") != null ? map.get("id").toString() : "";
        String title = map.get("title") != null ? map.get("title").toString() : "";

        String ikMaxStr = kaFenCiService.getIkMax(title);
        String ikMinStr = kaFenCiService.getIkMin(title);
        bdJdbcTemplate.update(UPDATE_SQL_01,ikMaxStr,ikMinStr, id);
        log.info("contentId:{} =========== 数据处理成功！！！ ",id);

    }

    public void getAllBiaoDIWu() throws Exception{

        String SQL_KEY = "SELECT id ,content_id FROM `loiloi_data`";

        List<String> ids = LogUtils.readRule("smf");
        for (String infoId : ids) {
            Thread.sleep(200);
            getZhaoZhongBiaoUnitSolrAllField(infoId);
        }

        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(SQL_KEY);
        for (Map<String, Object> map : maps) {
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

    }

    //根据 contentid 获取 招标单位 中标单位
    public void getZhaoZhongBiaoUnitSolrAllField(String infoId) throws IOException {

            HttpClient client = new DefaultHttpClient();
            HttpResponse response = null;
            // http://cusdata.qianlima.com/crm/info/detail?infoId=205206062&userId=4
            String url = "http://cusdata.qianlima.com/crm/info/detail?infoId="+infoId+"&userId=4";
            HttpGet get = new HttpGet(url);
            get.setHeader("Content-Type", "application/json");
            response = client.execute(get);
            String ret = null;
            ret = EntityUtils.toString(response.getEntity(), "UTF-8");

            System.out.println(ret);
            JSONObject parseObject = JSON.parseObject(ret);
            JSONObject data = parseObject.getJSONObject("data");
            String list = data.getString("list");
            JSONArray listInfo = JSON.parseArray(list);
            String zhaoBiaoUnit = "";
            String zhongBiaoUnit = "";
            for (int i = 0; i < listInfo.size(); i++) {
                JSONArray zhao_Biao_Unit = listInfo.getJSONObject(i).getJSONArray("zhaoBiaoUnit");
                JSONArray zhong_Biao_Unit = listInfo.getJSONObject(i).getJSONArray("zhongBiaoUnit");
                for (int j = 0; j < zhao_Biao_Unit.size(); j++) {
                    zhaoBiaoUnit = (String) zhao_Biao_Unit.get(j);
                }
                for (int j = 0; j < zhong_Biao_Unit.size(); j++) {
                    zhongBiaoUnit = (String) zhong_Biao_Unit.get(j);
                }
                bdJdbcTemplate.update("INSERT INTO loiloi_data (content_id,zhao_biao_unit,zhong_biao_unit,relation_way) VALUES (?,?,?,?)",infoId,zhaoBiaoUnit,zhongBiaoUnit,"");
                log.info("contentId:{} =========== 获取招中标单位 数据处理成功！！！ ",infoId);
            }


    }
}