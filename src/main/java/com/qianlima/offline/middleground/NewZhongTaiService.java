package com.qianlima.offline.middleground;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.Area;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.util.HttpUtil;
import com.qianlima.offline.util.JdbcUtils;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.QianlimaZTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class NewZhongTaiService {

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    private AtomicInteger atomicInteger=new AtomicInteger(0);

    public String INSERT_ZT_RESULT_HXR = "INSERT INTO han_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


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

    public void saveIntoMySqlByList(List<Map<String,Object>> maps) {
        /*
         *批量插入的方式：
         *addBatch(String)：添加需要批量处理的SQL语句或是参数；
         *executeBatch()：执行批量处理语句；
         *clearBatch():清空缓存的数据
         *2、mysql服务器默认是关闭批处理的，我们需要通过一个参数，让mysql开启批处理的支持。
         * 		 ?rewriteBatchedStatements=true 写在配置文件的url后面
         *
         */
        Connection conn = null;
        PreparedStatement ps = null;
        try {

            long start = System.currentTimeMillis();
            conn = JdbcUtils.getConnection();
            ps = conn.prepareStatement(INSERT_ZT_RESULT_HXR);

            for (Map<String, Object> map : maps) {
                int i = 0;
                ps.setString(1,map.get("task_id").toString());
                ps.setString(2,map.get("keyword").toString());
                ps.setString(3,map.get("content_id").toString());
                ps.setString(4,map.get("title").toString());
                ps.setString(5,map.get("content").toString());
                ps.setString(6,map.get("province").toString());
                ps.setString(7,map.get("city").toString());
                ps.setString(7,map.get("country").toString());
                ps.setString(8,map.get("url").toString());
                ps.setString(9,map.get("baiLian_budget").toString());
                ps.setString(10,map.get("baiLian_amount_unit").toString());
                ps.setString(11,map.get("xmNumber").toString());
                ps.setString(12,map.get("bidding_type").toString());
                ps.setString(13,map.get("progid").toString());
                ps.setString(14,map.get("zhao_biao_unit").toString());
                ps.setString(15,map.get("relation_name").toString());
                ps.setString(16,map.get("relation_way").toString());
                ps.setString(17,map.get("agent_unit").toString());
                ps.setString(18,map.get("agent_relation_ame").toString());
                ps.setString(19,map.get("agent_relation_way").toString());
                ps.setString(20,map.get("zhong_biao_unit").toString());
                ps.setString(21,map.get("link_man").toString());
                ps.setString(22,map.get("link_phone").toString());
                ps.setString(23,map.get("registration_begin_time").toString());
                ps.setString(24,map.get("registration_end_time").toString());
                ps.setString(25,map.get("biding_acquire_time").toString());
                ps.setString(26,map.get("biding_end_time").toString());
                ps.setString(27,map.get("tender_begin_time").toString());
                ps.setString(28,map.get("tender_end_time").toString());
                ps.setString(29,map.get("update_time").toString());
                ps.setString(30,map.get("type").toString());
                ps.setString(31,map.get("bidder").toString());
                ps.setString(32,map.get("notice_types").toString());
                ps.setString(33,map.get("open_biding_time").toString());
                ps.setString(34,map.get("is_electronic").toString());
                ps.setString(35,map.get("code").toString());
                ps.setString(36,map.get("isfile").toString());
                ps.setString(37,map.get("keyword_term").toString());

                ps.addBatch();

                // 每1000条记录插入一次
                if (i % 1000 == 0){
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                }
                i++;
            }
            //剩余不足1000
            ps.executeBatch();
            conn.commit();
            ps.clearBatch();
            long end = System.currentTimeMillis();
            System.out.println("花费的时间为：" + (end - start));//20000:83065 -- 565
        } catch (Exception e) {                                //1000000:16086
            e.printStackTrace();
        }
    }

    public Map<String, Object> handleZhongTaiGetResultMap(NoticeMQ map){
        return handleZhongTaiGetResultMap(map, null);
    }

    public Map<String, Object> handleZhongTaiGetResultMap(NoticeMQ map, HashMap<Integer, Area> areaMap){
        Long contentid = Long.valueOf(map.getContentid());
        Map<String, Object> resultMap = new HashMap<>();
        try {
            HttpUtil httpUtil = new HttpUtil();
            String result = httpUtil.push(contentid.toString());
            JSONObject data = JSON.parseObject(result);
            if (null == data) {
                log.error("异常contentid:{} 原因:中台接口查找不到1", contentid);
                return null;
            }

            JSONObject info = (JSONObject) data.get("data");
            if (info == null) {
                log.error("异常contentid:{} 原因:中台接口查找不到2", contentid);
                return null;
            }

            Integer catid = info.getInteger("catid");
            if (catid == null || catid.intValue() > 100) {
                log.error("异常contentid:{} 原因:不是招标类数据2", contentid);
                return null;
            }

            // 判断是否包含附件
            String isHasAddition = "否";
            StringBuilder urls = new StringBuilder();
            List<Map<String, Object>> jobList1 = gwJdbcTemplate.queryForList("select * from phpcms_c_zb_file where contentid = ?",contentid);
            for (Map<String, Object> stringObjectMap : jobList1) {
                urls.append("http://file.qianlima.com:11180/ae_ids/download/download_out.jsp?id="+stringObjectMap.get("fileid").toString());
            }

            if (StringUtils.isNotBlank(urls.toString())){
                isHasAddition = "是";
            }

            // 百炼
            String bl_budget = null;//获取百炼预算金额
            String bl_zhongbiao_amount = null;//获取百炼中标金额
            StringBuilder bl_zhaoBiaoUnit = new StringBuilder();//获取百炼招标单位
            StringBuilder bl_zhongBiaoUnit = new StringBuilder();//获取百炼中标单位
            StringBuilder bl_bidder = new StringBuilder();//获取候选人
            StringBuilder bl_agents = new StringBuilder();//
            if (info.get("expandField") != null) {
                JSONObject expandField = (JSONObject) info.get("expandField");

                //获取百炼预算金额
                if (expandField.get("budgetDetail") != null) {
                    JSONObject budgetDetail = (JSONObject) expandField.get("budgetDetail");
                    if (budgetDetail.get("totalBudget") != null) {
                        JSONObject totalBudget = (JSONObject) budgetDetail.get("totalBudget");
                        if (totalBudget.get("budget") != null) {
                            bl_budget = totalBudget.get("budget") != null ? totalBudget.get("budget").toString() : null;
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
                                        bl_zhongBiaoUnit.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                        bl_zhongBiaoUnit.append("、");
                                    } else {
                                        bl_zhongBiaoUnit.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
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
                                            bl_bidder.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                            bl_bidder.append("、");
                                        } else {
                                            bl_bidder.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
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
                            bl_zhongbiao_amount = jsonObject.get("amount") != null ? jsonObject.get("amount").toString() : null;
                        }
                    }
                }
                //获取百炼招标单位
                if (expandField.get("tenderees") != null) {
                    List<JSONObject> tenderees = (List<JSONObject>) expandField.get("tenderees");
                    for (int i = 0; i < tenderees.size(); i++) {
                        if (i + 1 != tenderees.size()) {
                            bl_zhaoBiaoUnit.append(tenderees.get(i) != null ? tenderees.get(i) : null);
                            bl_zhaoBiaoUnit.append("、");
                        } else {
                            bl_zhaoBiaoUnit.append(tenderees.get(i) != null ? tenderees.get(i) : null);
                        }
                    }
                }
                //获取百炼代理机构
                if (expandField.get("agents") != null) {
                    List<JSONObject> agents = (List<JSONObject>) expandField.get("agents");
                    for (int i = 0; i < agents.size(); i++) {
                        if (i + 1 != agents.size()) {
                            bl_agents.append(agents.get(i) != null ? agents.get(i) : null);
                            bl_agents.append("、");
                        } else {
                            bl_agents.append(agents.get(i) != null ? agents.get(i) : null);
                        }
                    }
                }

            }
            //获取招标方式
            String bidding_type = null;
            //是否是电子招标
            String is_electronic = null;
            if (info.get("biddingTypeDetail") != null) {
                JSONObject biddingTypeDetail = (JSONObject) info.get("biddingTypeDetail");
                bidding_type = biddingTypeDetail.get("bidding_type") != null ? biddingTypeDetail.get("bidding_type").toString() : null;
                is_electronic = biddingTypeDetail.get("is_electronic") != null ? biddingTypeDetail.get("is_electronic").toString() : null;
            }

            //获取招标单位的联系人和电话
            String relationName = null;
            String relationWay = null;
            if (info.get("zhaoBiaoDetail") != null) {
                JSONObject zhaoBiaoDetail = (JSONObject) info.get("zhaoBiaoDetail");
                relationName = zhaoBiaoDetail.get("relationName") != null ? zhaoBiaoDetail.get("relationName").toString() : null;
                relationWay = zhaoBiaoDetail.get("relationWay") != null ? zhaoBiaoDetail.get("relationWay").toString() : null;
            }

            //获取代理单位的联系人和电话
            String agent_relation_ame = null;
            String agent_relation_way = null;
            if (info.get("agentDetail") != null) {
                JSONObject agentDetail = (JSONObject) info.get("agentDetail");
                agent_relation_ame = agentDetail.get("relationName") != null ? agentDetail.get("relationName").toString() : null;
                agent_relation_way = agentDetail.get("relationWay") != null ? agentDetail.get("relationWay").toString() : null;
            }

            //获取中标单位的联系人和电话
            String link_man = null;
            String link_phone = null;
            if (info.get("zhongbiaoDetail") != null) {
                JSONObject zhongbiaoDetail = (JSONObject) info.get("zhongbiaoDetail");
                link_man = zhongbiaoDetail.get("linkman") != null ? zhongbiaoDetail.get("linkman").toString() : null;
                link_phone = zhongbiaoDetail.get("linkphone") != null ? zhongbiaoDetail.get("linkphone").toString() : null;
            }


            //获取时间详情
            String registration_begin_time = null;
            String registration_end_time = null;
            String biding_acquire_time = null;
            String biding_end_time = null;
            String tender_begin_time = null;
            String tender_end_time = null;
            String open_biding_time = null;
            if (info.get("extractDateDetail") != null) {
                JSONObject extractDateDetail = (JSONObject) info.get("extractDateDetail");
                if (extractDateDetail.get("registration_begin_time") != null){
                    String registration_begin_time1 = extractDateDetail.get("registration_begin_time").toString();
                    if (StringUtils.isNotBlank(registration_begin_time1)){
                        registration_begin_time = DateFormatUtils.format(Long.valueOf(extractDateDetail.get("registration_begin_time").toString()) * 1000L,    "yyyy-MM-dd HH:mm:ss");
                    }
                }

                if (extractDateDetail.get("registration_end_time") != null){
                    String registration_end_time1 = extractDateDetail.get("registration_end_time").toString();
                    if (StringUtils.isNotBlank(registration_end_time1)){
                        registration_end_time = DateFormatUtils.format(Long.valueOf(extractDateDetail.get("registration_end_time").toString()) * 1000L,    "yyyy-MM-dd HH:mm:ss");
                    }
                }

                if (extractDateDetail.get("biding_acquire_time") != null){
                    String registration_end_time1 = extractDateDetail.get("biding_acquire_time").toString();
                    if (StringUtils.isNotBlank(registration_end_time1)){
                        biding_acquire_time = DateFormatUtils.format(Long.valueOf(extractDateDetail.get("biding_acquire_time").toString()) * 1000L,    "yyyy-MM-dd HH:mm:ss");
                    }
                }

                if (extractDateDetail.get("biding_end_time") != null){
                    String registration_end_time1 = extractDateDetail.get("biding_end_time").toString();
                    if (StringUtils.isNotBlank(registration_end_time1)){
                        biding_end_time = DateFormatUtils.format(Long.valueOf(extractDateDetail.get("biding_end_time").toString()) * 1000L,    "yyyy-MM-dd HH:mm:ss");
                    }
                }

                if (extractDateDetail.get("tender_begin_time") != null){
                    String registration_end_time1 = extractDateDetail.get("tender_begin_time").toString();
                    if (StringUtils.isNotBlank(registration_end_time1)){
                        tender_begin_time = DateFormatUtils.format(Long.valueOf(extractDateDetail.get("tender_begin_time").toString()) * 1000L,    "yyyy-MM-dd HH:mm:ss");
                    }
                }


                if (extractDateDetail.get("tender_end_time") != null){
                    String registration_end_time1 = extractDateDetail.get("tender_end_time").toString();
                    if (StringUtils.isNotBlank(registration_end_time1)){
                        tender_end_time = DateFormatUtils.format(Long.valueOf(extractDateDetail.get("tender_end_time").toString()) * 1000L,    "yyyy-MM-dd HH:mm:ss");
                    }
                }


                if (extractDateDetail.get("open_biding_time") != null){
                    String registration_end_time1 = extractDateDetail.get("open_biding_time").toString();
                    if (StringUtils.isNotBlank(registration_end_time1)){
                        open_biding_time = DateFormatUtils.format(Long.valueOf(extractDateDetail.get("open_biding_time").toString()) * 1000L,    "yyyy-MM-dd HH:mm:ss");
                    }
                }
            }
            //获取update时间
            String update = null;
            if (info.get("updatetime") != null) {
                update = DateFormatUtils.format(Long.valueOf(info.get("updatetime").toString()) * 1000L,    "yyyy-MM-dd HH:mm:ss");
            }
            if (StringUtils.isEmpty(bl_zhaoBiaoUnit)) {
                bl_zhaoBiaoUnit.append(info.get("extract_zhaoBiaoUnit") != null ? info.get("extract_zhaoBiaoUnit").toString() : null);
            }
            if (StringUtils.isEmpty(bl_zhongBiaoUnit)) {
                bl_zhongBiaoUnit.append(info.get("extract_zhongBiaoUnit") != null ? info.get("extract_zhongBiaoUnit").toString() : null);
            }
            if (StringUtils.isEmpty(bl_agents)) {
                bl_agents.append(info.get("extract_agentUnit") != null ? info.get("extract_agentUnit").toString() : null);
            }
            if (bl_agents.toString().equals("null")) {
                bl_agents = null;
            }
            if (bl_zhongBiaoUnit.toString().equals("null")) {
                bl_zhongBiaoUnit = null;
            }
            if (bl_zhaoBiaoUnit.toString().equals("null")) {
                bl_zhaoBiaoUnit = null;
            }

            log.info("处理到:{}",atomicInteger.incrementAndGet());

            if(StringUtils.isBlank(bl_budget)){
                bl_budget = info.get("extract_budget") != null?info.get("extract_budget").toString():null;
            }
            if(StringUtils.isBlank(bl_zhongbiao_amount)){
                bl_zhongbiao_amount = info.get("extract_amountUnit") != null?info.get("extract_amountUnit").toString():null;
            }

            String provinceStr = null; //旧省
            String cityStr = null; //旧市
            String countryStr = null; //旧县
            String areaid = null;

            if (info.get("areaid") != null) {
                areaid = info.get("areaid").toString();
            }

            if (StringUtils.isNotBlank(areaid)) {
                provinceStr = getAreaMap(areaid).get("areaProvince");
                cityStr = getAreaMap(areaid).get("areaCity");
                countryStr = getAreaMap(areaid).get("areaCountry");
            }



            StringBuilder notice_types =new StringBuilder();
            if(info.get("dataTagDetail") != null){
                JSONObject dataTagDetail = (JSONObject) info.get("dataTagDetail");
                if(dataTagDetail.get("notice_types") != null){
                    List<String> list =(List<String>) dataTagDetail.get("notice_types");
                    for (String s : list) {
                        notice_types.append(s+",");
                    }
                }
            }
            String notice_typesStr=null;
            if(notice_types != null && notice_types.length() > 0){
                notice_typesStr = notice_types.substring(0, notice_types.length() - 1);
            }
            String keywordTerm = "";

            resultMap.put("task_id",map.getTaskId());
            resultMap.put("keyword",map.getKeyword());
            resultMap.put("content_id",contentid);
            resultMap.put("title",info.get("title"));
            resultMap.put("content",info.get("content"));
            resultMap.put("province",provinceStr);
            resultMap.put("city",cityStr);
            resultMap.put("country",countryStr);
            resultMap.put("url", info.get("url"));
            resultMap.put("baiLian_budget",bl_budget);
            resultMap.put("baiLian_amount_unit",bl_zhongbiao_amount);
            resultMap.put("xmNumber",info.get("xmNumber"));
            resultMap.put("bidding_type",bidding_type);
            resultMap.put("progid",info.get("progid"));
            resultMap.put("zhao_biao_unit",bl_zhaoBiaoUnit);
            resultMap.put("relation_name", LogUtils.format(relationName));
            resultMap.put("relation_way", LogUtils.format(relationWay));
            resultMap.put("agent_unit",bl_agents);
            resultMap.put("agent_relation_ame",agent_relation_ame);
            resultMap.put("agent_relation_way",agent_relation_way);
            resultMap.put("zhong_biao_unit",bl_zhongBiaoUnit);
            resultMap.put("link_man", LogUtils.format(link_man));
            resultMap.put("link_phone", LogUtils.format(link_phone));
            resultMap.put("registration_begin_time",registration_begin_time);
            resultMap.put("registration_end_time",registration_end_time);
            resultMap.put("biding_acquire_time",biding_acquire_time);
            resultMap.put("biding_end_time",biding_end_time);
            resultMap.put("tender_begin_time",tender_begin_time);
            resultMap.put("tender_end_time",tender_end_time);
            resultMap.put("update_time",update);
            resultMap.put("type",urls.toString());
            resultMap.put("bidder",bl_bidder);
            resultMap.put("notice_types",notice_typesStr);
            resultMap.put("open_biding_time",open_biding_time);
            resultMap.put("is_electronic",is_electronic);
            resultMap.put("code",map.getF());
            resultMap.put("isfile",isHasAddition);
            resultMap.put("keyword_term",map.getKeywordTerm());
        } catch (Exception e) {
            log.error("异常contentid:{} 原因:{}", contentid, e);
        }
        return resultMap;
    }

    //获取细分信息类型
    public String handleZhongTaiGetResultMapWithContent(NoticeMQ noticeMQ){
        Long contentid = Long.valueOf(noticeMQ.getContentid());
        String segmentType = null;
        try {
            Map<String, Object> map = QianlimaZTUtil.getFields( String.valueOf(contentid), "notice_segment_type", "notice_segment_type");
            if (map == null) {
                throw new RuntimeException("调取中台失败");
            }
            String returnCode = (String) map.get("returnCode");
            if ("500".equals(returnCode) || "1".equals(returnCode)) {
                log.error("该条 info_id：{}，数据调取中台额外字段失败", String.valueOf(contentid));
            } else if ("0".equals(returnCode)) {
                JSONObject data = (JSONObject) map.get("data");
                if (data == null) {
                    log.error("该条 info_id：{}，数据调取中台额外字段失败", String.valueOf(contentid));
                    throw new RuntimeException("数据调取中台失败");
                }
                JSONArray fileds = data.getJSONArray("fields");

                if (fileds != null && fileds.size() > 0) {
                    for (int d = 0; d < fileds.size(); d++) {
                        JSONObject object = fileds.getJSONObject(d);
                        if (null != object.get("notice_segment_type")) {
                            segmentType = object.getString("notice_segment_type");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("异常contentid:{} 原因:{}", contentid, e);
        }
        return segmentType;
    }


    //获取数据的status,判断是否为99
    public static final String SELECT_PHPCMS_CONTENT_BY_CONTENTID = "SELECT status FROM phpcms_content where contentid = ? ";

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

    // ka_部门内部省、市、县区域联查
    private final static List<String> kaAreaList = new ArrayList<>();


    public synchronized Map<String, String> getAreaMap(String areaId) {
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
