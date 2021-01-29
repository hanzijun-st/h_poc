package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.extract.target.TargetExtractService;
import com.qianlima.offline.bean.ConstantBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class ZhongTaiBiaoDiWuServiceForOne {

    private static final String CHECK_SQL = "select status from phpcms_content where contentid = ?";

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;


    public void getSolrAllField() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(64);
        List<Future> futureList1 = new ArrayList<>();

        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT content_id FROM lala_data where code is null or  code = ''  ");
        for (Map<String, Object> maps : mapList) {

            String contentId = maps.get("content_id") != null ? maps.get("content_id").toString() : "";

            futureList1.add(executorService1.submit(() -> {
                try {



                    getAllZhongTaiBiaoDIWu(contentId);
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

    public String getAllZhongTaiBiaoDIWu(String contentId) throws Exception{

        String biaodiwu = "";
        List<Map<String, Object>> contentList = gwJdbcTemplate.queryForList(ConstantBean.SELECT_ITEM_CONTENT_BY_CONTENTID, contentId);
        if (contentList == null && contentList.size() == 0){
            return null;
        }
        String content = contentList.get(0).get("content").toString();

        if (StringUtils.isNotBlank(content)){
            String target = TargetExtractService.getTargetResult("http://47.104.4.12:5001/to_json_v3/", content);
            if (StringUtils.isNotBlank(target)){
                JSONObject targetObject = JSONObject.parseObject(target);
                if (targetObject.containsKey("targetDetails")){
                    StringBuilder sb = new StringBuilder();
                    JSONArray targetDetails = (JSONArray) targetObject.get("targetDetails");
                    for (Object targetDetail : targetDetails) {
                        String detail = targetDetail.toString();
                        Map detailMap = JSON.parseObject(detail, Map.class);
                        if (detailMap.containsKey("name")){
                            String name = (String) detailMap.get("name");
                            sb.append("名称:"+name);
                        }
                        if (detailMap.containsKey("brand")){
                            String brand = (String) detailMap.get("brand");
                            sb.append(" ,品牌:"+brand);
                        }
                        if (detailMap.containsKey("model")){
                            String model = (String) detailMap.get("model");
                            sb.append(" ,型号:"+model);
                        }
                        sb.append(" ;");
                    }
                    biaodiwu = sb.toString();
                } else {
                    biaodiwu = "";
                }
            }
        }
        if (StringUtils.isNotBlank(biaodiwu)){
            biaodiwu = biaodiwu.substring(0, biaodiwu.length() - 1);
            bdJdbcTemplate.update("UPDATE loiloi_data SET code = ? WHERE content_id = ? ", biaodiwu, contentId);
            log.info("contentId:{} 获取标的物需求字段成功!!! ", contentId);
        }
        return biaodiwu;
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
