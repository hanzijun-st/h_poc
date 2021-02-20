package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.solr.common.util.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class CusDataFieldService {

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    static String apiUrl = "http://inner-datafetcher.intra.qianlima.com/dc/bidding/fields";

    private AtomicInteger atomicInteger=new AtomicInteger(0);

    // ka_部门内部省、市、县区域联查
    private final static List<String> kaAreaList = new ArrayList<>();

    // 数据入库操作
    private static final String INSERT_ZT_RESULT_HXR = "INSERT INTO han_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    //获取数据的status,判断是否为99
    private static final String SELECT_PHPCMS_CONTENT_BY_CONTENTID = "SELECT status FROM phpcms_content where contentid = ? ";

    /**
     * 判断当前数据的数据状态
     */
    public boolean checkStatus(String contentid){
        boolean result = false;
        Map<String, Object> map = gwJdbcTemplate.queryForMap(SELECT_PHPCMS_CONTENT_BY_CONTENTID, contentid);
        if (map != null && map.get("status") != null) {
            int status = Integer.parseInt(map.get("status").toString());
            if (status == 99) {
                result = true;
            }
        }
        return result;
    }

    /**
     * 保存数据入库
     */
    public void saveIntoMysql(Map<String, Object> map){
        bdJdbcTemplate.update(INSERT_ZT_RESULT_HXR,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"));
    }

    /**
     * 获取数据接口（全部自提字段）, flag 是否需要"正文"字段 ture：需要  false：不需要
     */
    public Map<String, Object> getAllFieldsWithZiTi(NoticeMQ noticeMQ, boolean flag){
        Map<String, Object> hashMap = new HashMap<>();
        JSONArray jsonArray = getJSONArrayWithFields(noticeMQ.getContentid().toString(), flag);
        if (jsonArray != null && jsonArray.size() > 0){
            // 转换基础字段、时间字段信息、获取自提定制数据
            hashMap = getResultMapWithGenPart(jsonArray, hashMap, noticeMQ.getContentid().toString());
            hashMap = getResultMapWithTimePart(jsonArray, hashMap);
            hashMap = getResultMapWithZiTi(jsonArray, hashMap);
            if (hashMap == null || hashMap.isEmpty()){
                return null;
            }
            hashMap.put("task_id", noticeMQ.getTaskId());
            hashMap.put("keyword", noticeMQ.getKeyword());
            hashMap.put("content_id", noticeMQ.getContentid().toString()); // contentId
            hashMap.put("code", noticeMQ.getF()); //F词
        }
        return hashMap;
    }


    public Map<String, Object> getAllFieldsWithOther(NoticeMQ noticeMQ, boolean flag){
        Map<String, Object> hashMap = new HashMap<>();
        JSONArray jsonArray = getJSONArrayWithFields(noticeMQ.getContentid().toString(), flag);
        if (jsonArray != null && jsonArray.size() > 0){
            // 转换基础字段、时间字段信息、获取自提定制数据
            hashMap = getResultMapWithGenPart(jsonArray, hashMap, noticeMQ.getContentid().toString());
            hashMap = getResultMapWithTimePart(jsonArray, hashMap);
            hashMap = getResultMapWithOther(jsonArray, hashMap);
            if (hashMap == null || hashMap.isEmpty()){
                return null;
            }
            hashMap.put("task_id", noticeMQ.getTaskId());
            hashMap.put("keyword", noticeMQ.getKeyword());
            hashMap.put("content_id", noticeMQ.getContentid().toString()); // contentId
            hashMap.put("code", noticeMQ.getF()); //F词
        }
        if (hashMap.get("title") == null){
            return null;
        }
        return hashMap;
    }



    /**
     * 获取数据接口（全部百炼字段）, flag 是否需要"正文"字段 ture：需要  false：不需要
     */
    public Map<String, Object> getAllFieldsWithBaiLian(NoticeMQ noticeMQ, boolean flag){
        Map<String, Object> hashMap = new HashMap<>();
        JSONArray jsonArray = getJSONArrayWithFields(noticeMQ.getContentid().toString(), flag);
        if (jsonArray != null && jsonArray.size() > 0){
            // 转换基础字段、时间字段信息、获取百炼定制数据
            hashMap = getResultMapWithGenPart(jsonArray, hashMap, noticeMQ.getContentid().toString());
            hashMap = getResultMapWithTimePart(jsonArray, hashMap);
            hashMap = getResultMapWithBaiLian(jsonArray, hashMap);
            if (hashMap == null || hashMap.isEmpty()){
                return null;
            }
            hashMap.put("task_id", noticeMQ.getTaskId());
            hashMap.put("keyword", noticeMQ.getKeyword());
            hashMap.put("content_id", noticeMQ.getContentid().toString()); // contentId
            hashMap.put("code", noticeMQ.getF()); //F词
        }

        return hashMap;
    }

    /**
     * 获取数据接口（混合字段, 百炼为主，自提为辅）, flag 是否需要"正文"字段 ture：需要  false：不需要
     */
    public Map<String, Object> getAllFieldsWithHunHe(NoticeMQ noticeMQ, boolean flag){
        Map<String, Object> hashMap = new HashMap<>();
        JSONArray jsonArray = getJSONArrayWithFields(noticeMQ.getContentid().toString(), flag);
        if (jsonArray != null && jsonArray.size() > 0){
            // 转换基础字段、时间字段信息、获取混合定制数据
            hashMap = getResultMapWithGenPart(jsonArray, hashMap, noticeMQ.getContentid().toString());
            hashMap = getResultMapWithTimePart(jsonArray, hashMap);
            hashMap = getResultMapWithHunHe(jsonArray, hashMap);
            if (hashMap == null || hashMap.isEmpty()){
                return null;
            }
            hashMap.put("task_id", noticeMQ.getTaskId());
            hashMap.put("keyword", noticeMQ.getKeyword());
            hashMap.put("content_id", noticeMQ.getContentid().toString()); // contentId
            hashMap.put("code", noticeMQ.getF()); //F词
        }

        return hashMap;
    }

    // 获取百炼单位、金额
    private Map<String, Object> getResultMapWithHunHe(JSONArray jsonArray, Map<String, Object> resultMap){
        String blBudget = null; //获取百自提预算金额
        String blZhongbiaoAmount = null; //获取自提中标金额
        StringBuilder blZhaoBiaoUnit = new StringBuilder(); //获取百炼招标单位
        StringBuilder blZhongBiaoUnit = new StringBuilder(); //获取百炼中标单位
        StringBuilder blBidder = new StringBuilder(); //获取候选人
        StringBuilder blAgents = new StringBuilder(); //百炼代理机构
        for (int d = 0; d < jsonArray.size(); d++) {
            JSONObject object = jsonArray.getJSONObject(d);
            if (null != object.getJSONObject("expandField")) {
                JSONObject expandField = object.getJSONObject("expandField");
                if (expandField != null) {
                    //获取百炼预算金额
                    if (expandField.get("budgetDetail") != null) {
                        JSONObject budgetDetail = (JSONObject) expandField.get("budgetDetail");
                        if (budgetDetail.get("totalBudget") != null) {
                            JSONObject totalBudget = (JSONObject) budgetDetail.get("totalBudget");
                            if (totalBudget.get("budget") != null) {
                                blBudget = totalBudget.get("budget") != null ? totalBudget.get("budget").toString() : null;
                            }
                        }
                    }
                    //获取百炼中标单位
                    int winnersSize = 0;
                    if (expandField.get("winners") != null) {
                        List<JSONObject> winners = (List<JSONObject>) expandField.get("winners");
                        for (JSONObject winner : winners) {
                            winnersSize++;
                            if (winner.get("bidderDetails") != null) {
                                List<JSONObject> bidderDetails = (List<JSONObject>) winner.get("bidderDetails");
                                for (int i = 0; i < bidderDetails.size(); i++) {
                                    if (bidderDetails.get(i).get("bidder") != null) {
                                        if (i + 1 != bidderDetails.size() || winnersSize != winners.size()) {
                                            blZhongBiaoUnit.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                            blZhongBiaoUnit.append("、");
                                        } else {
                                            blZhongBiaoUnit.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                        }
                                    }
                                }
                            }
                        }
                        //获取候选人
                        int biddersSize = 0;
                        if (expandField.get("bidders") != null) {
                            List<JSONObject> bidders = (List<JSONObject>) expandField.get("bidders");
                            for (JSONObject bidder : bidders) {
                                biddersSize++;
                                if (bidder.get("bidderDetails") != null) {
                                    List<JSONObject> bidderDetails = (List<JSONObject>) bidder.get("bidderDetails");
                                    for (int i = 0; i < bidderDetails.size(); i++) {
                                        if (bidderDetails.get(i).get("bidder") != null) {
                                            if (i + 1 != bidderDetails.size() || biddersSize != winners.size()) {
                                                blBidder.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                                blBidder.append("、");
                                            } else {
                                                blBidder.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //获取百炼中标金额
                        if (winners.get(0).get("bidderDetails") != null) {
                            JSONArray bidderDetails = (JSONArray) winners.get(0).get("bidderDetails");
                            JSONObject jsonObject = (JSONObject) bidderDetails.get(0);
                            if (jsonObject.get("amount") != null) {
                                blZhongbiaoAmount = jsonObject.get("amount") != null ? jsonObject.get("amount").toString() : null;
                            }
                        }
                    }
                    //获取百炼招标单位
                    if (expandField.get("tenderees") != null) {
                        List<JSONObject> tenderees = (List<JSONObject>) expandField.get("tenderees");
                        for (int i = 0; i < tenderees.size(); i++) {
                            if (i + 1 != tenderees.size()) {
                                blZhaoBiaoUnit.append(tenderees.get(i) != null ? tenderees.get(i) : null);
                                blZhaoBiaoUnit.append("、");
                            } else {
                                blZhaoBiaoUnit.append(tenderees.get(i) != null ? tenderees.get(i) : null);
                            }
                        }
                    }
                    //获取百炼代理机构
                    if (expandField.get("agents") != null) {
                        List<JSONObject> agents = (List<JSONObject>) expandField.get("agents");
                        for (int i = 0; i < agents.size(); i++) {
                            if (i + 1 != agents.size()) {
                                blAgents.append(agents.get(i) != null ? agents.get(i) : null);
                                blAgents.append("、");
                            } else {
                                blAgents.append(agents.get(i) != null ? agents.get(i) : null);
                            }
                        }
                    }
                }
                // 判断百炼数据是否为空
                if (StringUtils.isBlank(blBudget) && null != object.get("extract_budget")){
                    blBudget = object.getString("extract_budget");
                }
                if (StringUtils.isBlank(blZhongbiaoAmount) && null != object.get("extract_amountUnit")){
                    blZhongbiaoAmount = object.getString("extract_amountUnit");
                }
                if (StringUtils.isBlank(blZhaoBiaoUnit) && null != object.get("extract_zhaoBiaoUnit")){
                    blZhaoBiaoUnit.append(object.getString("extract_zhaoBiaoUnit"));
                }
                if (StringUtils.isBlank(blZhongBiaoUnit) && null != object.get("extract_zhongBiaoUnit")){
                    blZhongBiaoUnit.append(object.getString("extract_zhongBiaoUnit"));
                }
                if (StringUtils.isBlank(blAgents) && null != object.get("extract_agentUnit")){
                    blAgents.append(object.getString("extract_agentUnit"));
                }

            }
        }
        resultMap.put("baiLian_budget", blBudget); //获取预算金额
        resultMap.put("baiLian_amount_unit", blZhongbiaoAmount);//获取中标金额
        resultMap.put("zhong_biao_unit", blZhongBiaoUnit); //获取中标单位
        resultMap.put("zhao_biao_unit", blZhaoBiaoUnit);//获取招标单位
        resultMap.put("bidder", blBidder);  //候选人
        resultMap.put("agent_unit", blAgents);  //百炼代理机构
        return resultMap;

    }



    // 获取百炼单位、金额
    private Map<String, Object> getResultMapWithOther(JSONArray jsonArray, Map<String, Object> resultMap){
        String blBudget = null; //获取百自提预算金额
        String blZhongbiaoAmount = null; //获取自提中标金额
        StringBuilder blZhaoBiaoUnit = new StringBuilder(); //获取百炼招标单位
        StringBuilder blZhongBiaoUnit = new StringBuilder(); //获取百炼中标单位
        StringBuilder blBidder = new StringBuilder(); //获取候选人
        String blAgents = null; //百炼代理机构
        for (int d = 0; d < jsonArray.size(); d++) {
            JSONObject object = jsonArray.getJSONObject(d);
            if (null != object.getJSONObject("expandField")) {
                JSONObject expandField = object.getJSONObject("expandField");
                if (expandField != null) {
                    //获取百炼预算金额
                    if (expandField.get("budgetDetail") != null) {
                        JSONObject budgetDetail = (JSONObject) expandField.get("budgetDetail");
                        if (budgetDetail.get("totalBudget") != null) {
                            JSONObject totalBudget = (JSONObject) budgetDetail.get("totalBudget");
                            if (totalBudget.get("budget") != null) {
                                blBudget = totalBudget.get("budget") != null ? totalBudget.get("budget").toString() : null;
                            }
                        }
                    }
                    //获取百炼中标单位
                    int winnersSize = 0;
                    if (expandField.get("winners") != null) {
                        List<JSONObject> winners = (List<JSONObject>) expandField.get("winners");
                        for (JSONObject winner : winners) {
                            winnersSize++;
                            if (winner.get("bidderDetails") != null) {
                                List<JSONObject> bidderDetails = (List<JSONObject>) winner.get("bidderDetails");
                                for (int i = 0; i < bidderDetails.size(); i++) {
                                    if (bidderDetails.get(i).get("bidder") != null) {
                                        if (i + 1 != bidderDetails.size() || winnersSize != winners.size()) {
                                            blZhongBiaoUnit.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                            blZhongBiaoUnit.append("、");
                                        } else {
                                            blZhongBiaoUnit.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                        }
                                    }
                                }
                            }
                        }
                        //获取候选人
                        int biddersSize = 0;
                        if (expandField.get("bidders") != null) {
                            List<JSONObject> bidders = (List<JSONObject>) expandField.get("bidders");
                            for (JSONObject bidder : bidders) {
                                biddersSize++;
                                if (bidder.get("bidderDetails") != null) {
                                    List<JSONObject> bidderDetails = (List<JSONObject>) bidder.get("bidderDetails");
                                    for (int i = 0; i < bidderDetails.size(); i++) {
                                        if (bidderDetails.get(i).get("bidder") != null) {
                                            if (i + 1 != bidderDetails.size() || biddersSize != winners.size()) {
                                                blBidder.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                                blBidder.append("、");
                                            } else {
                                                blBidder.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //获取百炼中标金额
                        if (winners.get(0).get("bidderDetails") != null) {
                            JSONArray bidderDetails = (JSONArray) winners.get(0).get("bidderDetails");
                            JSONObject jsonObject = (JSONObject) bidderDetails.get(0);
                            if (jsonObject.get("amount") != null) {
                                blZhongbiaoAmount = jsonObject.get("amount") != null ? jsonObject.get("amount").toString() : null;
                            }
                        }
                    }
                }
            }

            // 判断百炼数据是否为空
            if (StringUtils.isBlank(blBudget)){
                if ( null != object.get("extract_budget")){
                    blBudget = object.getString("extract_budget");
                }
            }
            if (StringUtils.isBlank(blZhongbiaoAmount)){
                if ( null != object.get("extract_amountUnit")){
                    blZhongbiaoAmount = object.getString("extract_amountUnit");
                }
            }
            if (null != object.get("extract_zhaoBiaoUnit")){
                blZhaoBiaoUnit.append(object.getString("extract_zhaoBiaoUnit"));
            }
            if (StringUtils.isBlank(blZhongBiaoUnit)){
                if (null != object.get("extract_zhongBiaoUnit")){
                    blZhongBiaoUnit.append(object.getString("extract_zhongBiaoUnit"));
                }
            }
            if (null != object.get("extract_agentUnit")){
                blAgents = object.getString("extract_agentUnit");
            }
        }
        resultMap.put("baiLian_budget", StringUtils.isBlank(blBudget) ? "" : blBudget); //获取预算金额
        resultMap.put("baiLian_amount_unit", StringUtils.isBlank(blZhongbiaoAmount) ? "" : blZhongbiaoAmount);//获取中标金额
        resultMap.put("zhong_biao_unit", StringUtils.isBlank(blZhongBiaoUnit) ? "" : blZhongBiaoUnit); //获取中标单位
        resultMap.put("zhao_biao_unit", StringUtils.isBlank(blZhaoBiaoUnit) ? "" : blZhaoBiaoUnit);//获取招标单位
        resultMap.put("bidder", StringUtils.isBlank(blBidder) ? "" : blBidder);  //候选人
        resultMap.put("agent_unit", StringUtils.isBlank(blAgents) ? "" : blAgents);  //百炼代理机构
        return resultMap;

    }

    // 获取百炼单位、金额
    private Map<String, Object> getResultMapWithZiTi(JSONArray jsonArray, Map<String, Object> resultMap){
        String blBudget = null; //获取百自提预算金额
        String blZhongbiaoAmount = null; //获取自提中标金额
        String blZhaoBiaoUnit = null; //获取自提招标单位
        String blZhongBiaoUnit = null; //获取自提中标单位
        String blBidder = null; //获取候选人
        String blAgents = null; //百炼代理机构
        for (int d = 0; d < jsonArray.size(); d++) {
            JSONObject object = jsonArray.getJSONObject(d);
            // 获取招标预算
            if (null != object.get("extract_budget")) {
                blBudget = object.getString("extract_budget");
            } else if (null != object.get("extract_zhaoBiaoUnit")) {
                blZhaoBiaoUnit = object.getString("extract_zhaoBiaoUnit");
            } else if (null != object.get("extract_zhongBiaoUnit")) {
                blZhongBiaoUnit = object.getString("extract_zhongBiaoUnit");
            } else if (null != object.get("extract_amountUnit")) {
                blZhongbiaoAmount = object.getString("extract_amountUnit");
            }  else if (null != object.get("extract_agentUnit")) {
                blAgents = object.getString("extract_agentUnit");
            }
        }
        resultMap.put("baiLian_budget", blBudget); //获取预算金额
        resultMap.put("baiLian_amount_unit", blZhongbiaoAmount);//获取中标金额
        resultMap.put("zhong_biao_unit", blZhongBiaoUnit); //获取中标单位
        resultMap.put("zhao_biao_unit", blZhaoBiaoUnit);//获取招标单位
        resultMap.put("bidder", blBidder);  //候选人
        resultMap.put("agent_unit", blAgents);  //百炼代理机构
        return resultMap;
    }

    // 获取百炼单位、金额
    private Map<String, Object> getResultMapWithBaiLian(JSONArray jsonArray, Map<String, Object> resultMap){
        String blBudget = null; //获取百炼预算金额
        String blZhongbiaoAmount = null; //获取百炼中标金额
        StringBuilder blZhaoBiaoUnit = new StringBuilder(); //获取百炼招标单位
        StringBuilder blZhongBiaoUnit = new StringBuilder(); //获取百炼中标单位
        StringBuilder blBidder = new StringBuilder(); //获取候选人
        StringBuilder blAgents = new StringBuilder(); //百炼代理机构
        for (int d = 0; d < jsonArray.size(); d++) {
            JSONObject object = jsonArray.getJSONObject(d);
             if (null != object.getJSONObject("expandField")) {
                JSONObject expandField = object.getJSONObject("expandField");
                if (expandField != null) {
                    //获取百炼预算金额
                    if (expandField.get("budgetDetail") != null) {
                        JSONObject budgetDetail = (JSONObject) expandField.get("budgetDetail");
                        if (budgetDetail.get("totalBudget") != null) {
                            JSONObject totalBudget = (JSONObject) budgetDetail.get("totalBudget");
                            if (totalBudget.get("budget") != null) {
                                blBudget = totalBudget.get("budget") != null ? totalBudget.get("budget").toString() : null;
                            }
                        }
                    }
                    //获取百炼中标单位
                    int winnersSize = 0;
                    if (expandField.get("winners") != null) {
                        List<JSONObject> winners = (List<JSONObject>) expandField.get("winners");
                        for (JSONObject winner : winners) {
                            winnersSize++;
                            if (winner.get("bidderDetails") != null) {
                                List<JSONObject> bidderDetails = (List<JSONObject>) winner.get("bidderDetails");
                                for (int i = 0; i < bidderDetails.size(); i++) {
                                    if (bidderDetails.get(i).get("bidder") != null) {
                                        if (i + 1 != bidderDetails.size() || winnersSize != winners.size()) {
                                            blZhongBiaoUnit.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                            blZhongBiaoUnit.append("、");
                                        } else {
                                            blZhongBiaoUnit.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                        }
                                    }
                                }
                            }
                        }
                        //获取候选人
                        int biddersSize = 0;
                        if (expandField.get("bidders") != null) {
                            List<JSONObject> bidders = (List<JSONObject>) expandField.get("bidders");
                            for (JSONObject bidder : bidders) {
                                biddersSize++;
                                if (bidder.get("bidderDetails") != null) {
                                    List<JSONObject> bidderDetails = (List<JSONObject>) bidder.get("bidderDetails");
                                    for (int i = 0; i < bidderDetails.size(); i++) {
                                        if (bidderDetails.get(i).get("bidder") != null) {
                                            if (i + 1 != bidderDetails.size() || biddersSize != winners.size()) {
                                                blBidder.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                                blBidder.append("、");
                                            } else {
                                                blBidder.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //获取百炼中标金额
                        if (winners.get(0).get("bidderDetails") != null) {
                            JSONArray bidderDetails = (JSONArray) winners.get(0).get("bidderDetails");
                            JSONObject jsonObject = (JSONObject) bidderDetails.get(0);
                            if (jsonObject.get("amount") != null) {
                                blZhongbiaoAmount = jsonObject.get("amount") != null ? jsonObject.get("amount").toString() : null;
                            }
                        }
                    }
                    //获取百炼招标单位
                    if (expandField.get("tenderees") != null) {
                        List<JSONObject> tenderees = (List<JSONObject>) expandField.get("tenderees");
                        for (int i = 0; i < tenderees.size(); i++) {
                            if (i + 1 != tenderees.size()) {
                                blZhaoBiaoUnit.append(tenderees.get(i) != null ? tenderees.get(i) : null);
                                blZhaoBiaoUnit.append("、");
                            } else {
                                blZhaoBiaoUnit.append(tenderees.get(i) != null ? tenderees.get(i) : null);
                            }
                        }
                    }
                    //获取百炼代理机构
                    if (expandField.get("agents") != null) {
                        List<JSONObject> agents = (List<JSONObject>) expandField.get("agents");
                        for (int i = 0; i < agents.size(); i++) {
                            if (i + 1 != agents.size()) {
                                blAgents.append(agents.get(i) != null ? agents.get(i) : null);
                                blAgents.append("、");
                            } else {
                                blAgents.append(agents.get(i) != null ? agents.get(i) : null);
                            }
                        }
                    }
                }
            }
        }
        resultMap.put("baiLian_budget", blBudget); //获取百炼预算金额
        resultMap.put("baiLian_amount_unit", blZhongbiaoAmount);//获取百炼中标金额
        resultMap.put("zhong_biao_unit", blZhongBiaoUnit); //获取百炼中标单位
        resultMap.put("zhao_biao_unit", blZhaoBiaoUnit);//获取百炼招标单位
        resultMap.put("bidder", blBidder);  //候选人
        resultMap.put("agent_unit", blAgents);  //百炼代理机构
        return resultMap;
    }

    // 转换基础字段部分
    private Map<String, Object> getResultMapWithGenPart(JSONArray jsonArray, Map<String, Object> resultMap, String infoId){
        String isElectronic = null;  // 是否电子招标
        String biddingType = null;  // 招标类型
        String areaid = null;  // 地区id（area_areaId）
        String title = null; // 标题
        String content = null; // 正文
        String url = null; // 链接
        String progid = null; // progid
        String updatetime = null; // 发布时间
        String xmNumber = null; // 项目编号
        String relationName = null; //招标单位联系人
        String relationWay = null; //招标单位联系方式
        String linkMan = null; //招标单位联系人
        String linkPhone = null; //招标单位联系人
        String agentRelationName = null; //招标单位联系人
        String agentRelationWay = null; //招标单位联系人

        for (int d = 0; d < jsonArray.size(); d++) {
            JSONObject object = jsonArray.getJSONObject(d);
            if (null != object.get("title")) {
                title = object.getString("title");
            } else if (null != object.get("url")) {
                url = object.getString("url");
            } else if (null != object.get("progid")) {
                progid = object.getString("progid");
            } else if (null != object.get("updatetime")) {
                if (StringUtils.isNotBlank(object.getString("updatetime"))) {
                    updatetime = DateFormatUtils.format(Long.valueOf(object.getString("updatetime")) * 1000L, "yyyy-MM-dd HH:mm:ss");
                }
            } else if (null != object.get("content")) {
                content = object.getString("content");
            } else if (null != object.get("xmNumber")) {
                xmNumber = object.getString("xmNumber");

            } else if (null != object.get("area_areaid")) {
                areaid = object.getString("area_areaid");

            } else if (null != object.get("biddingTypeDetail")) {
                JSONObject biddingTypeDetail = object.getJSONObject("biddingTypeDetail");
                isElectronic = biddingTypeDetail.getString("is_electronic");
                biddingType = biddingTypeDetail.getString("bidding_type");
            }  else if (null != object.getJSONObject("zhaoBiaoDetail")) {
                JSONObject zhaoBiaoDetail = object.getJSONObject("zhaoBiaoDetail");
                if (zhaoBiaoDetail != null) {
                    relationName = zhaoBiaoDetail.get("relationName") != null ? zhaoBiaoDetail.get("relationName").toString() : null;
                    relationWay = zhaoBiaoDetail.get("relationWay") != null ? zhaoBiaoDetail.get("relationWay").toString() : null;
                }
            } else if (null != object.getJSONObject("agentDetail")) {
                JSONObject agentDetail = object.getJSONObject("agentDetail");
                if (agentDetail != null) {
                    agentRelationName = agentDetail.get("relationName") != null ? agentDetail.get("relationName").toString() : null;
                    agentRelationWay = agentDetail.get("relationWay") != null ? agentDetail.get("relationWay").toString() : null;
                }
            } else if (null != object.getJSONObject("zhongbiaoDetail")) {
                JSONObject zhongbiaoDetail = object.getJSONObject("zhongbiaoDetail");
                if (zhongbiaoDetail != null) {
                    linkMan = zhongbiaoDetail.get("linkman") != null ? zhongbiaoDetail.get("linkman").toString() : null;
                    linkPhone = zhongbiaoDetail.get("linkphone") != null ? zhongbiaoDetail.get("linkphone").toString() : null;
                }
            }
        }

        String provinceName = null;
        String cityName = null;
        String countryName = null;
        if (StringUtils.isNotBlank(areaid)) {
            provinceName = getAreaMap(areaid).get("areaProvince");
            cityName = getAreaMap(areaid).get("areaCity");
            countryName = getAreaMap(areaid).get("areaCountry");
        }

        // 判断是否包含附件
        String isHasAddition = "否";
        StringBuilder urls = new StringBuilder();
        List<Map<String, Object>> jobList1 = gwJdbcTemplate.queryForList("select * from phpcms_c_zb_file where contentid = ?", infoId);
        for (Map<String, Object> stringObjectMap : jobList1) {
            urls.append("http://file.qianlima.com:11180/ae_ids/download/download_out.jsp?id=" + stringObjectMap.get("fileid").toString());
        }
        if (StringUtils.isNotBlank(urls.toString())) {
            isHasAddition = "是";
        }
        resultMap.put("title", title); // 标题
        resultMap.put("content", content); //正文
        resultMap.put("province", provinceName); // 省
        resultMap.put("city", cityName); // 市
        resultMap.put("country", countryName); // 县
        resultMap.put("url", url); // url
        resultMap.put("xmNumber", xmNumber); //序列号
        resultMap.put("bidding_type", biddingType); // 招标方式
        resultMap.put("progid", progid); // 信息类型
        resultMap.put("update_time", updatetime);  // 更新时间
        resultMap.put("is_electronic", isElectronic);  // 是否电子招标
        resultMap.put("isfile", isHasAddition); // 是否包含附件
        resultMap.put("relation_name", format(relationName));
        resultMap.put("relation_way", format(relationWay));
        resultMap.put("agent_relation_ame", agentRelationName);
        resultMap.put("agent_relation_way", agentRelationWay);
        resultMap.put("link_man", format(linkMan));
        resultMap.put("link_phone", format(linkPhone));
        resultMap.put("type", "");  // 预留字段1
        resultMap.put("keyword_term", ""); // 预留字段2
        return resultMap;
    }

    // 转换基础字段部分
    private Map<String, Object> getResultMapWithTimePart(JSONArray jsonArray, Map<String, Object> resultMap){
        String bidingEndTime = null; //报名开始时间
        String registrationBeginTime = null; //报名截止时间
        String registrationEndTime = null; //标书获取时间
        String bidingAcquireTime = null; //开标时间
        String openBidingTime = null; //投标开始时间
        String tenderBeginTime = null; //投标截止时间
        String tenderEndTime = null; //标书截止时间

        for (int d = 0; d < jsonArray.size(); d++) {
            JSONObject object = jsonArray.getJSONObject(d);
            if (null != object.get("extractDateDetail")) {
                JSONObject extractDateDetail = object.getJSONObject("extractDateDetail");
                if (extractDateDetail.get("registration_begin_time") != null) {
                    registrationBeginTime = extractDateDetail.getString("registration_begin_time");
                    if (StringUtils.isNotBlank(registrationBeginTime)) {
                        registrationBeginTime = DateFormatUtils.format(Long.valueOf(registrationBeginTime) * 1000L, "yyyy-MM-dd HH:mm:ss");
                    }
                }
                if (extractDateDetail.get("registration_end_time") != null) {
                    registrationEndTime = extractDateDetail.getString("registration_end_time");
                    if (StringUtils.isNotBlank(registrationEndTime)) {
                        registrationEndTime = DateFormatUtils.format(Long.valueOf(registrationEndTime) * 1000L, "yyyy-MM-dd HH:mm:ss");
                    }
                }
                if (extractDateDetail.get("biding_acquire_time") != null) {
                    bidingAcquireTime = extractDateDetail.getString("biding_acquire_time");
                    if (StringUtils.isNotBlank(bidingAcquireTime)) {
                        bidingAcquireTime = DateFormatUtils.format(Long.valueOf(bidingAcquireTime) * 1000L, "yyyy-MM-dd HH:mm:ss");
                    }
                }
                if (extractDateDetail.get("biding_end_time") != null) {
                    bidingEndTime = extractDateDetail.getString("biding_end_time");
                    if (StringUtils.isNotBlank(bidingEndTime)) {
                        bidingEndTime = DateFormatUtils.format(Long.valueOf(bidingEndTime) * 1000L, "yyyy-MM-dd HH:mm:ss");
                    }
                }
                if (extractDateDetail.get("tender_begin_time") != null) {
                    tenderBeginTime = extractDateDetail.getString("tender_begin_time");
                    if (StringUtils.isNotBlank(tenderBeginTime)) {
                        tenderBeginTime = DateFormatUtils.format(Long.valueOf(tenderBeginTime) * 1000L, "yyyy-MM-dd HH:mm:ss");
                    }
                }
                if (extractDateDetail.get("tender_end_time") != null) {
                    tenderEndTime = extractDateDetail.getString("tender_end_time");
                    if (StringUtils.isNotBlank(tenderEndTime)) {
                        tenderEndTime = DateFormatUtils.format(Long.valueOf(tenderEndTime) * 1000L, "yyyy-MM-dd HH:mm:ss");
                    }
                }
                if (extractDateDetail.get("open_biding_time") != null) {
                    openBidingTime = extractDateDetail.getString("open_biding_time");
                    if (StringUtils.isNotBlank(openBidingTime)) {
                        openBidingTime = DateFormatUtils.format(Long.valueOf(openBidingTime) * 1000L, "yyyy-MM-dd HH:mm:ss");
                    }
                }
            }
        }
        resultMap.put("registration_begin_time", registrationBeginTime);  //报名开始时间
        if (StrUtil.isNotEmpty(registrationEndTime)){
            resultMap.put("registration_end_time", com.qianlima.offline.util.DateUtils.parseDateFromDateStr(registrationEndTime)); //报名截止时间
        }else {
            resultMap.put("registration_end_time", null); //报名截止时间
        }
        resultMap.put("biding_acquire_time", bidingAcquireTime);  //标书获取时间
        resultMap.put("open_biding_time", openBidingTime);  //开标时间
        resultMap.put("tender_begin_time", tenderBeginTime);  //投标开始时间
        resultMap.put("tender_end_time", tenderEndTime);  //投标截止时间
        resultMap.put("biding_end_time", bidingEndTime); //标书截止时间
        return resultMap;
    }



    private JSONArray getJSONArrayWithFields(String infoId, boolean flag){
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = null;
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            //4、创建HttpGet请求
            HttpGet httpGet = new HttpGet("http://cusdata.qianlima.com/zt/api/"+infoId);
            httpGet.setConfig(requestConfig);
            CloseableHttpResponse response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                log.info("=====调用分支机构接口===="+infoId);
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (StringUtils.isNotBlank(result)){
                    jsonObject = JSON.parseObject(result);
                }
            } else {
                log.info("infoId:{} 调用数据详情接口异常, 返回状态不是 200 ", infoId);
                throw new RuntimeException("调用数据详情接口异常，请联系管理员， 返回状态不是 200 ");
            }
        } catch (Exception e){
            log.error("调用数据详情接口异常:{}, 获取不到详情数据", e);
        }
        if (jsonObject == null){
            log.error("调用数据详情接口异常", infoId);
            throw new RuntimeException("调用数据详情接口异常， 获取到的数据为 空 ");
        }
        String code = jsonObject.getString("code");
        if ("1".equals(code)){
            bdJdbcTemplate.update("INSERT INTO table_code (content_id,code) VALUES (?,?)",infoId,code);
        }
        if ("-1".equals(code) || "1".equals(code) || "2".equals(code)) {
            log.error("infoId:{} 调用数据详情接口异常, 对应的状态码 code ：{} ", infoId, code);
            throw new RuntimeException("调用数据详情接口异常");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (jsonObject == null){
            log.error("调用数据详情接口异常", infoId);
            throw new RuntimeException("调用数据详情接口异常， 获取到的数据内容 为 空 ");
        }
        Map<String,Object> map = JSONObject.parseObject(data.toString(), Map.class);
        Set<Map.Entry<String, Object>> entries = map.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            JSONObject result = new JSONObject();
            result.put(entry.getKey(), entry.getValue());
            jsonArray.add(result);
        }
        return jsonArray;
    }




    // 获取中台接口通用字段
//    private JSONArray getJSONArrayWithFields(String infoId, boolean flag){
//        JSONArray jsonArray = new JSONArray();
//        try {
//            String fileName = "id,title,url,progid,area_areaid,updatetime,xmNumber,extract_budget,extract_zhaoBiaoUnit,extract_zhongBiaoUnit,extract_amountUnit,extract_agentUnit," +
//                    "extractDateDetail,biddingTypeDetail,expandField,zhaoBiaoDetail,agentDetail,zhongbiaoDetail" ;
//            if (flag){
//                fileName += ",content";
//            }
//            Map<String, Object> map = QianlimaZTUtil.getFields(apiUrl, infoId, fileName, "");
//            log.info("处理到:{}",atomicInteger.incrementAndGet());
//            if (map == null) {
//                log.error("获取中台接口失败", infoId);
//                throw new RuntimeException("调取中台失败");
//            }
//            String returnCode = (String) map.get("returnCode");
//            if ("500".equals(returnCode) || "1".equals(returnCode)) {
//                log.error("该条 info_id：{}，数据调取中台字段失败", infoId);
//                throw new RuntimeException("数据调取中台失败");
//            } else if ("0".equals(returnCode)) {
//                JSONObject data = (JSONObject) map.get("data");
//                if (data == null) {
//                    log.error("该条 info_id：{}，数据调取中台字段失败", infoId);
//                    throw new RuntimeException("数据调取中台失败");
//                }
//                jsonArray = data.getJSONArray("fields");
//            }
//        } catch (Exception e){
//            log.error("数据调取中台字段失败, infoId:{} 原因:{}", infoId, e);
//        }
//        return jsonArray;
//    }

    /// 招标单位联系人、联系电话。中标单位联系人、联系电话 多个的用的英文逗号分隔。
    private static String format(String field) {
        if (StringUtils.isEmpty(field)) {
            return "";
        }
        return field.replaceAll("，", ",");
    }

    // 获取地区映射
    private synchronized Map<String, String> getAreaMap(String areaId) {
        Map<String, String> resultMap = new HashMap<>();
        if (kaAreaList == null || kaAreaList.size() == 0) {
            try {
                ClassPathResource classPathResource = new ClassPathResource("area/ka_area.txt");
                InputStream inputStream = classPathResource.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line = bufferedReader.readLine();
                while (StringUtils.isNotBlank(line)) {//BufferedReader有readLine()，可以实现按行读取
                    kaAreaList.add(line);
                    line = bufferedReader.readLine();
                }
            } catch (Exception e) {
                log.error("读取ka_area 失败, 请查证原因");
            }
        }
        for (String kaArea : kaAreaList) {
            String[] areaList = kaArea.split(":", -1);
            if (areaList != null && areaList.length == 4) {
                if (areaList[0].equals(areaId)) {
                    resultMap.put("areaProvince", areaList[1]);
                    resultMap.put("areaCity", areaList[2]);
                    resultMap.put("areaCountry", areaList[3]);
                }
            }
        }
        return resultMap;
    }
}
