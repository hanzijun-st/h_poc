package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.util.QianlimaZTUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class BiaoDiWuQuKuaiService {

    @Value("${zt.api.singleFieldUrl}")
    private String singleFieldUrl;

    @Value("${zt.api.saveContentIdUrl}")
    private String saveContentIdUrl;

    private static final String SELECT_SQL = "SELECT id, content_id as contentid from dajia_data where code is null";

    private static final String UPDATA_SQL_01 = "INSERT INTO poc_biaodiwu2 (contentid, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATA_SQL_02 = "UPDATE dajia_data set code = ? where content_id = ?";

    private static final String CHECK_SQL = "select status from phpcms_content where contentid = ?";

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;


    public void getAllBiaoDIWu() throws Exception{
        String content = getAllnotifyZhongTai();
//        Thread.sleep(20*60*1000L);
//        getAllgetTarget();
        log.info("=============================任务完成做到最新===========================");
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

    private String getAllgetTarget(){
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            List<Future> futureList = new ArrayList<>();
            List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(SELECT_SQL);
            if (maps != null && maps.size() > 0){
                log.info("任务查出来了 total：{}", maps.size());
                for (Map<String, Object> map : maps) {
                    futureList.add(executorService.submit(() ->  getTarget(map)));
                }
                for (Future future1 : futureList) {
                    try {
                        future1.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        executorService.shutdown();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        } catch (Exception e) {
            log.error("通知任务异常 e:{}", e);
        }
        return "通知任务完成做到最新";
    }



    private String getAllnotifyZhongTai(){
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            List<Future> futureList = new ArrayList<>();
            List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(SELECT_SQL);
            if (maps != null && maps.size() > 0){
                log.info("任务查出来了 total：{}", maps.size());
                for (Map<String, Object> map : maps) {
                    futureList.add(executorService.submit(() ->  notifyZhongTai(map)));
                }
                for (Future future1 : futureList) {
                    try {
                        future1.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        executorService.shutdown();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        } catch (Exception e) {
            log.error("通知任务异常 e:{}", e);
        }
        return "通知任务完成做到最新";
    }


    private void notifyZhongTai(Map<String, Object> map){
        String contentid = map.get("contentid").toString();
        if (checkPHPContent(contentid) == false){
            return;
        }
        //入库成功后通知中台组装标的物
        Map<String, Object> stringObjectMap = QianlimaZTUtil.saveContentId(saveContentIdUrl, contentid);
        if ("0".equals(stringObjectMap.get("returnCode"))) {
            log.info("通知中台组装标的物成功，成功infoId:{}", contentid);
        } else {
            log.error("通知中台组装标的物失败，失败infoId:{},中台返回状态码:{}", contentid, stringObjectMap.get("returnCode"));
            throw new RuntimeException("通知中台组装标的物失败");
        }
    }




    private void getTarget(Map<String, Object> map){
        String contentid = map.get("contentid").toString();
        if (checkPHPContent(contentid) == false){
            return;
        }
        Map<String, Object> targetMap = QianlimaZTUtil.getSingleField(singleFieldUrl, contentid, "target");
        if (targetMap != null){
            String returnCode = targetMap.get("returnCode").toString();
            if ("0".equals(returnCode)){
                JSONObject data = (JSONObject) targetMap.get("data");
                if (data == null) {
                    log.error("该条 info_id：{}，获取标的物返回结果异常", contentid);
                    throw new RuntimeException("获取标的物返回结果异常");
                }
                if (null != data.get("has_extract")) {
                    if (data.getBoolean("has_extract")){
                        if (null != data.get("target")){
                            String target = data.get("target").toString();
                            if (StringUtils.isNotBlank(target)){
                                Map biaodiwuMap = JSON.parseObject(target, Map.class);
                                if (biaodiwuMap.containsKey("targetDetails")){
                                    JSONArray targetDetails = (JSONArray) biaodiwuMap.get("targetDetails");
                                    for (Object targetDetail : targetDetails) {
                                        String detail = targetDetail.toString();
                                        Map detailMap = JSON.parseObject(detail, Map.class);
                                        String serialNumber = ""; //标的物序号
                                        String name = ""; //名称
                                        String brand = ""; //品牌
                                        String model = ""; //型号
                                        String number = ""; //数量
                                        String numberUnit = ""; //数量单位
                                        String price = ""; //单价
                                        String priceUnit = "";  //单价单位
                                        String totalPrice = ""; //总价
                                        String totalPriceUnit = ""; //总价单位
                                        if (detailMap.containsKey("serialNumber")){
                                            serialNumber = (String) detailMap.get("serialNumber");
                                        }
                                        if (detailMap.containsKey("name")){
                                            name = (String) detailMap.get("name");
                                        }
                                        if (detailMap.containsKey("brand")){
                                            brand = (String) detailMap.get("brand");
                                        }
                                        if (detailMap.containsKey("model")){
                                            model = (String) detailMap.get("model");
                                        }
                                        if (detailMap.containsKey("number")){
                                            number = (String) detailMap.get("number");
                                        }
                                        if (detailMap.containsKey("numberUnit")){
                                            numberUnit = (String) detailMap.get("numberUnit");
                                        }
                                        if (detailMap.containsKey("price")){
                                            price = (String) detailMap.get("price");
                                        }
                                        if (detailMap.containsKey("priceUnit")){
                                            priceUnit = (String) detailMap.get("priceUnit");
                                        }

                                        if (detailMap.containsKey("totalPrice")){
                                            totalPrice = (String) detailMap.get("totalPrice");
                                        }
                                        if (detailMap.containsKey("totalPriceUnit")){
                                            totalPriceUnit = (String) detailMap.get("totalPriceUnit");
                                        }
                                        bdJdbcTemplate.update(UPDATA_SQL_01, contentid, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit);
                                    }
                                    bdJdbcTemplate.update(UPDATA_SQL_02, "标的物检索", contentid);
                                }
                            }

                        }
                    }
                }
            }
        } else {
            log.error("获取标的物返回结果异常 contentid：{}", contentid);
            throw new RuntimeException("获取标的物返回结果异常");
        }
    }


    private boolean checkPHPContent(String contentid){
        boolean flag = false;
        List<Map<String, Object>> maps = gwJdbcTemplate.queryForList(CHECK_SQL, contentid);
        if (maps != null && maps.size() > 0 ){
            for (Map<String, Object> map : maps) {
                String status = map.get("status").toString();
                if ("99".equals(status)){
                    flag = true;
                }
            }
        }
        return flag;
    }

}
