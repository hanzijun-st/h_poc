package com.qianlima.offline.service.han.impl;

import com.mysql.jdbc.Connection;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.han.CusDataNewService;
import com.qianlima.offline.service.han.TestProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
public class TestProjectServiceImpl implements TestProjectService {

    @Autowired
    private CusDataNewService cusDataNewService;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("djeJdbcTemplate")
    private JdbcTemplate djeJdbcTemplate;


    @Override
    public void getProjectName() {
        // 获取随机数
        Random random = new Random();

        Long cursorMark = 211787162L;
        Integer pageSize = 10000;
        log.info("开始获取分页数据");
        while (true) {
            long startTime = System.currentTimeMillis();
            List<Map<String, Object>> maps = gwJdbcTemplate.queryForList("select contentid, title,status from phpcms_content where contentid > ? order by contentid asc limit ?", cursorMark, pageSize);
            long secondTime = System.currentTimeMillis();
            log.info("获取了size：{} 条数据, 从数据库中获取数据耗时：{} 毫秒",maps.size(), secondTime - startTime);
            if (maps != null && maps.size() > 0){
                ExecutorService executorService = Executors.newFixedThreadPool(60);
                List<Future> futureList = new ArrayList<>();
                for (Map<String, Object> map : maps) {
                    cursorMark = Long.valueOf(map.get("contentid").toString());
                    if (map.get("status") !=null && Integer.valueOf(map.get("status").toString()) == 99){
                        String title = map.get("title").toString();
                        if (title.contains("...")){
                            //处理对应逻辑
                            NoticeMQ noticeMQ = new NoticeMQ();
                            noticeMQ.setContentid(cursorMark);

                            futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(noticeMQ)));
                        }
                    }
                }
                if (futureList !=null && futureList.size() >0){
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

                long endTime = System.currentTimeMillis();
                log.info("获取了size：{} 条数据, 从数据库中获取数据耗时：{} 毫秒, 本次总共耗时：{} 毫秒, 共size:{} 条符合规则",
                        maps.size(), secondTime - startTime, endTime- startTime);
            } else {
                break;
            }
        }
        log.info("游标获取用户数据结束");
    }

    /**
     * 调用中台数据
     */
    private void getZhongTaiDatasAndSave(NoticeMQ noticeMQ) {

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            String contentId = resultMap.get("content_id").toString();
            //进行大金额替换操作
            List<Map<String, Object>> maps = djeJdbcTemplate.queryForList("select info_id, winner_amount, budget from amount_code where info_id = ?", contentId);
            if (maps != null && maps.size() > 0){
                // 由于大金额处理的特殊性，只能用null进行判断
                String winnerAmount = maps.get(0).get("winner_amount") != null ? maps.get(0).get("winner_amount").toString() : null;
                if (winnerAmount != null){
                    resultMap.put("baiLian_amount_unit", winnerAmount);
                }
                String budget = maps.get(0).get("budget") != null ? maps.get(0).get("budget").toString() : null;
                if (budget != null){
                    resultMap.put("baiLian_budget", budget);
                }
            }
            saveIntoMysql2(resultMap);
        }
    }

    public static final String INSERT_ZT_RESULT_PRO = "INSERT INTO han_new_data_pro (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time," +
            " is_electronic,code,isfile,keyword_term,keywords, infoTypeSegment,monitorUrl, pocDetailUrl) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public void saveIntoMysql2(Map<String, Object> map){
        bdJdbcTemplate.update(INSERT_ZT_RESULT_PRO,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"),map.get("keywords"),map.get("infoTypeSegment"),map.get("monitorUrl"),map.get("pocDetailUrl"));
    }

}