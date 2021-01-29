package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.extract.target.TargetExtractService;
import com.qianlima.offline.bean.ConstantBean;
import com.qianlima.offline.util.ContentSolr;
import com.qianlima.offline.util.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class ZhongTaiBiaoDiWuService {


    private static final String UPDATA_SQL_01 = "INSERT INTO han_biaodiwu (contentid, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATA_SQL_02 = "INSERT INTO han_biaodiwu_copy (contentid, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String CHECK_SQL = "select status from phpcms_content where contentid = ?";


    @Autowired
    private ContentSolr contentSolr;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    public void getSolrAllField() throws IOException {

        ExecutorService executorService = Executors.newFixedThreadPool(80);
        List<Future> futureList = new ArrayList<>();

        List<String> ids = LogUtils.readRule("smf");

//        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT contentid FROM loiloi_biaodiwu GROUP BY contentid");

        for (String id : ids) {
            futureList.add(executorService.submit(() -> {
                try {
                    getAllZhongTaiBiaoDIWu(id);
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
    public void getSolrAllField2(String fileName) throws IOException {

        ExecutorService executorService = Executors.newFixedThreadPool(80);
        List<Future> futureList = new ArrayList<>();

        //List<String> ids = LogUtils.readRule(fileName);
        //List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT contentid FROM han_contentid");
        //List<Map<String, Object>> mapDatas = bdJdbcTemplate.queryForList("SELECT contentid FROM han_biaodiwu_copy");

        //总标的物id
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT content_id FROM han_data");
        //已运行的标的物id
        List<Map<String, Object>> hanIds = bdJdbcTemplate.queryForList("SELECT contentid FROM han_biaodiwu");


        List<String> list = new ArrayList<>();
        Set<String> hanList = new HashSet<>();

        for (Map<String, Object> map : hanIds) {
            hanList.add(map.get("contentid").toString());
        }

        for (Map<String, Object> mapData : mapList) {
            if (!hanList.contains(mapData.get("content_id").toString())){
                list.add(mapData.get("content_id").toString());
            }
        }

//        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT contentid FROM loiloi_biaodiwu GROUP BY contentid");

        for (String id : list) {
            futureList.add(executorService.submit(() -> {
                try {
                    getAllZhongTaiBiaoDIWu(id);
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
        System.out.println("--------------标的物查询结束--------------");
    }


    public void getAllZhongTaiBiaoDIWu(String contentId) throws Exception{

        List<Map<String, Object>> contentList = gwJdbcTemplate.queryForList(ConstantBean.SELECT_ITEM_CONTENT_BY_CONTENTID, contentId);
        if (contentList == null && contentList.size() == 0){
            return;
        }
        String content = contentList.get(0).get("content").toString();
        String target = "";
        if (StringUtils.isNotBlank(content)){
            try{
                target = TargetExtractService.getTargetResult("http://47.104.4.12:5001/to_json_v3/", content);
            } catch (Exception e){
                log.error("contentId:{}==========", contentId);
            }

            if (StringUtils.isNotBlank(target)){
                JSONObject targetObject = JSONObject.parseObject(target);
                if (targetObject.containsKey("targetDetails")){
                    JSONArray targetDetails = (JSONArray) targetObject.get("targetDetails");
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
                        bdJdbcTemplate.update(UPDATA_SQL_02, contentId, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit);
//                        bdJdbcTemplate.update("UPDATE loiloi_biaodiwu SET code = ? WHERE content_id = ? ", 1, contentId);
                        log.info("contentId:{} =========== 标的物解析表数据处理成功！！！ ",contentId);
                    }
                }
            }else {
                log.info("contentId:{} =========== 不存在标的物解析数据！ ",contentId);
                return;
            }
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
