package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.extract.TargetService;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.NewBiaoDiWuService;
import com.qianlima.offline.service.han.CusDataNewService;
import com.qianlima.offline.service.han.TestService;
import com.qianlima.offline.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2021/1/12.
 */

@Service
@Slf4j
public class TestServiceImpl implements TestService{

    @Autowired
    private UpdateContentSolr contentSolr;

    @Autowired
    private OnlineContentSolr onlineContentSolr;

    @Autowired
    private NewBiaoDiWuService newBiaoDiWuService;
    @Autowired
    private CusDataFieldService cusDataFieldService;

    @Autowired
    private CusDataNewService cusDataNewService;//新方法使用接口 调用中台获取数据

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    public String INSERT_ZT_RESULT_HXR = "INSERT INTO han_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    public String INSERT_ZT_1 = "INSERT INTO han_data_bdeng (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types," +
            "open_biding_time,is_electronic,code,isfile,keyword_term,heici) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";



    /**
     * 最新标的物获取方法
     */
    @Override
    public void getNewBdw() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList = new ArrayList<>();
        //contentid
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT id,contentid FROM han_contentid");
        for (Map<String, Object> mapData : mapList) {
            String syNum = mapData.get("id").toString();//剩余数据量
            futureList.add(executorService1.submit(() -> {
                newBiaoDiWuService.handleForData(Long.valueOf(mapData.get("contentid").toString()));
                log.info("新标的物方法--->:{}",syNum);
            }));
        }
        for (Future future1 : futureList) {
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
        log.info("---------------===============================新标的物方法运行结束==================================");
    }


    @Override
    public void updateKeyword() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList = new ArrayList<>();

        //String[] keywords ={"交换机","锐捷"};
        try {
            List<String> keywords = LogUtils.readRule("hKeywords");
            List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT id,contentid,name,brand,model FROM han_biaodiwu");
            if (mapList !=null && mapList.size() >0){
                List<Map<String,Object>> list = new ArrayList<>();

                for (Map<String, Object> map : mapList) {
                    String id = map.get("id").toString();
                    String contentid = map.get("contentid").toString();
                    String name = map.get("name").toString();
                    String brand = map.get("brand").toString();
                    String model = map.get("model").toString();

                    String key = "";
                    for (String keyword : keywords) {
                        if (name.contains(keyword) || brand.contains(keyword) || model.contains(keyword)){
                            key+=keyword+"、";
                        }
                    }
                    if (ZTStringUtil.isNotBlank(key)){
                        Map<String,Object> m = new HashMap<>();
                        m.put(id,key.substring(0,key.length() - 1));
                        list.add(m);
                    }
                }
                if (list !=null && list.size() > 0){
                    for (Map<String, Object> map : list) {
                        for(Map.Entry<String,Object> e :map.entrySet()){
                            if (e.getValue() !=null){
                                futureList.add(executorService1.submit(() -> {
                                    bdJdbcTemplate.update("UPDATE han_biaodiwu SET keyword = ? WHERE id = ?", e.getValue() , e.getKey());
                                }));
                            }
                        }
                    }
                }
                for (Future future1 : futureList) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getStr() {
        return "成功调通接口";
    }

    @Override
    public String testData() {
        String str ="";
        try {
            List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT id FROM han_data_copy");
            str = "ok";
        }catch (Exception e) {
            e.printStackTrace();
            str ="error";
        }
        return str;
    }

    @Override
    public void getZhongQi(Integer type) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            //读取配置文件中的黑词
            List<String> blacks = LogUtils.readRule("blockKeys");
            //关键词a
            List<String> aa = LogUtils.readRule("keyWords");
            //关键词b
            List<String> bb = LogUtils.readRule("hKeywords");

            //标题检索关键词a
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200801 TO 20210121] AND (progid: 3 OR progid:5) AND catid:[* TO 100] AND title:\"" + str + "\" ", str, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
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
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str);
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

            //标题检索含有“关键词b”AND 全文检索含有“关键词a”
            for (String str : aa) {
                for (String str2 : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200801 TO 20210121] AND (progid: 3 OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim() + "————" + mqEntities.size());
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
                                    if (flag){
                                        listAll.add(data);
                                        data.setKeyword(str+"&"+str2);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();
            //关键词a
            for (String key :aa){
                arrayList.add(key);
            }

            for (String key : aa) {
                for (String str2 : bb) {
                    arrayList.add(key+"&"+str2);
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
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());


           if (type.intValue() == 1){
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
            System.out.println("==========================================此程序运行结束========================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getFangTianXia(Integer type) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            //关键词a
            List<String> aa = LogUtils.readRule("keyWordsA");
            //关键词b
            List<String> bb = LogUtils.readRule("keyWordsB");
            //关键词c
            List<String> cc = LogUtils.readRule("keyWordsC");


            //标题检索含有“关键词a”AND 标题检索含有“关键词b”
            for (String str : aa) {
                for (String str2 : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201218 TO 20201231] AND progid:[0 TO 2] AND catid:[* TO 100] AND title:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim() + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag){
                                        listAll.add(data);
                                        data.setKeyword(str+"&"+str2);
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
            //全文检索关键词c
            for (String str : cc) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201218 TO 20201231] AND progid:[0 TO 2] AND catid:[* TO 100] AND allcontent:\"" + str + "\" ", str, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();

            //关键词a 和 关键词b
            for (String key : aa) {
                for (String str2 : bb) {
                    arrayList.add(key+"&"+str2);
                }
            }
            //关键词c
            for (String key :cc){
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
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());


            if (type.intValue() == 1){
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
            System.out.println("==========================================此程序运行结束========================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getFuJianHaiBo(Integer type,String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            //关键词a
            List<String> aa = LogUtils.readRule("keyWordsA");
            //关键词b
            List<String> bb = LogUtils.readRule("keyWordsB");
            //关键词c
            List<String> cc = LogUtils.readRule("keyWordsC");

            //读取配置文件中的黑词
            List<String> blacks = LogUtils.readRule("blockKeys");

            //标题检索含有“关键词a”AND 标题检索含有“关键词b”
            for (String str : aa) {
                for (String str2 : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:3 OR progid:5) AND catid:[* TO 100] AND title:\"" + str + "\"  AND allcontent:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim() + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag){
                                        listAll.add(data);
                                        data.setKeyword(str+"&"+str2);
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
            //全文检索关键词c
            for (String str : cc) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:3 OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\" ", str, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
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
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();

            //关键词a 和 关键词b
            for (String key : aa) {
                for (String str2 : bb) {
                    arrayList.add(key+"&"+str2);
                }
            }
            //关键词c
            for (String key :cc){
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
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());


            if (type.intValue() == 1){
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
            System.out.println("==========================================此程序运行结束========================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getZongHengDaPeng(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            //关键词a
            List<String> aa = LogUtils.readRule("keyWordsA");
            //关键词b
            List<String> bb = LogUtils.readRule("keyWordsB");

            //读取配置文件中的黑词
            List<String> blacks = LogUtils.readRule("blockKeys");

            //全文检索关键词a AND 关键词b
            for (String str : aa) {
                for (String str2 : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\"  AND allcontent:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim() + "————" + mqEntities.size());
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
                                    if (flag){
                                        listAll.add(data);
                                        data.setKeyword(str+"&"+str2);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();

           //关键词a 和 关键词b
            for (String key : aa) {
                for (String str2 : bb) {
                    arrayList.add(key+"&"+str2);
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
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());


            if (type.intValue() == 1){
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
            System.out.println("==========================================此程序运行结束========================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getZongHengDaPeng2(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            //关键词a
            List<String> aa = LogUtils.readRule("keyWordsA");

            //读取配置文件中的黑词
            List<String> blacks = LogUtils.readRule("blockKeys");

            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\" ", str, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
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
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();

           /*//关键词a 和 关键词b
            for (String key : aa) {
                for (String str2 : bb) {
                    arrayList.add(key+"&"+str2);
                }
            }*/
            //关键词c
            for (String key :aa){
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
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());


            if (type.intValue() == 1){
                if (list != null && list.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(80);
                    List<Future> futureList = new ArrayList<>();
                    int num = list.size();
                    for (NoticeMQ content : list) {
                        String number =String.valueOf(num);
                        futureList.add(executorService.submit(() ->
                                getDataFromZhongTaiAndSave2(content,number)));
                        num --;
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
            System.out.println("==========================================此程序运行结束========================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getZongHengDaPeng3(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            //关键词a
            List<String> aa = LogUtils.readRule("keyWordsA");
            //关键词b
            List<String> bb = LogUtils.readRule("keyWordsB");

            //读取配置文件中的黑词
            List<String> blacks = LogUtils.readRule("blockKeys");

            //全文检索关键词a AND 关键词b
            /*for (String str : aa) {
                for (String str2 : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\"  AND allcontent:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim() + "————" + mqEntities.size());
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
                                    if (flag){
                                        listAll.add(data);
                                        data.setKeyword(str+"&"+str2);
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
            }*/
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5)  AND (zhaoSecondIndustry:\"自然资源\" " +
                            "OR zhaoSecondIndustry:\"能源\" OR zhaoSecondIndustry:\"水利水电\" OR zhaoSecondIndustry:\"能源\" OR zhaoSecondIndustry:\"应急管理\" " +
                            "OR zhaoSecondIndustry:\"石油化工\" OR zhaoSecondIndustry:\"水利\" OR zhaoSecondIndustry:\"新能源\" OR zhaoSecondIndustry:\"消防安防\" " +
                            "OR zhaoSecondIndustry:\"环保\" OR zhaoSecondIndustry:\"林业\" OR zhaoSecondIndustry:\"电气\" OR zhaoSecondIndustry:\"燃气热力\") " +
                            "AND catid:[* TO 100] AND allcontent:\"" + str + "\" ", str, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
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
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();

           /*//关键词a 和 关键词b
            for (String key : aa) {
                for (String str2 : bb) {
                    arrayList.add(key+"&"+str2);
                }
            }*/
            //关键词c
            for (String key :aa){
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
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());


            if (type.intValue() == 1){
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
            System.out.println("==========================================此程序运行结束========================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getHeFeiHangLian(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            //关键词a
            List<String> aa = LogUtils.readRule("keyWordsA");
            //关键词b
            List<String> bb = LogUtils.readRule("keyWordsB");

            //读取配置文件中的黑词
            List<String> blacks = LogUtils.readRule("blockKeys");

            //全文检索关键词a AND 关键词b
            for (String str : aa) {
                for (String str2 : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND title:\"" + str + "\"  AND allcontent:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim() + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag){
                                        listAll.add(data);
                                        data.setKeyword(str+"&"+str2);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();

            //关键词a 和 关键词b
            for (String key : aa) {
                for (String str2 : bb) {
                    arrayList.add(key+"&"+str2);
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
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());


            if (type.intValue() == 1){
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
            System.out.println("==========================================此程序运行结束========================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getBeiJingGuanrui(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            //关键词a
            List<String> aa = LogUtils.readRule("keyWords");

            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND catid:[* TO 100] AND (title:\"" + str + "\" OR zhaoBiaoUnit:\"" + str + "\")", str, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();

               /*//关键词a 和 关键词b
                for (String key : aa) {
                    for (String str2 : bb) {
                        arrayList.add(key+"&"+str2);
                    }
                }*/
            //关键词c
            for (String key :aa){
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
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());


            if (type.intValue() == 1){
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
            System.out.println("==========================================此程序运行结束========================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getYuxin3(Integer type, String date) throws Exception{

        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> list2 = new ArrayList<>();//去重后的数据-联系方式
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        HashMap<String, String> dataMap2 = new HashMap<>();//联系方式

        List<NoticeMQ> list3 = new ArrayList<>();//去手机号数据统计(不包括关键词)

        List<String> keyWords = LogUtils.readRule("keyWords");
        try {
            //自提招标单位检索“行业标签”中标黄部分  AND  标题检索关键词aa
            //futureList1.add(executorService1.submit(() -> {
            List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND progid:3 AND catid:[* TO 100] AND zhongRelationWay:*","", 1);
            if (!mqEntities.isEmpty()) {
                System.out.println("solr所有数据量："+mqEntities.size());
                for (NoticeMQ data : mqEntities) {
                    if (NumberUtil.validateMobilePhone(data.getZhongRelationWay())){
                        list3.add(data);
                        if (keyWords.contains(data.getZhongBiaoUnit())){
                            listAll.add(data);
                            //data.setKeyword(keyWord);
                            if (!dataMap.containsKey(data.getContentid().toString())) {
                                list.add(data);
                                dataMap.put(data.getContentid().toString(), "0");
                            }
                            if (!dataMap2.containsKey(data.getZhongRelationWay())){
                                list2.add(data);
                                dataMap2.put(data.getZhongRelationWay().toString(), "0");
                            }
                        }
                    }
                }
            }


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("去重之后的数据量-联系方式：" + list2.size());
            log.info("去手机号数据统计(不包括关键词)：" + list3.size());
            log.info("==========================");

            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());
            System.out.println("去重之后的数据量-联系方式：" + list2.size());
            System.out.println("去手机号数据统计(不包括关键词)：" + list3.size());


            if (type.intValue() == 1){
                if (list != null && list2.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(80);
                    List<Future> futureList = new ArrayList<>();

                    for (NoticeMQ content : list2) {
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
            System.out.println("==========================================此程序运行结束========================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getLianx(Integer type) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();
        //List<String> keyWords = LogUtils.readRule("keyWords");
        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT * FROM han_unit");
        for (Map<String, Object> map : maps) {
            String keyWord = map.get("unit_name").toString();
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyy:2020 AND catid:[* TO 100] AND zhongBiaoUnit:\"" + keyWord + "\" ","", 1);
                if (!mqEntities.isEmpty()) {
                    System.out.println(keyWord+":"+mqEntities.size());
                    for (NoticeMQ data : mqEntities) {
                        listAll.add(data);
                        //data.setKeyword(keyWord);
                        /*if (!dataMap.containsKey(data.getZhongRelationWay().toString())) {
                            list.add(data);
                            dataMap.put(data.getZhongRelationWay().toString(), "0");
                        }*/
                        if (StrUtil.isNotEmpty(data.getZhongRelationWay())){
                            list.add(data);
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


        log.info("全部数据量：" + listAll.size());
        log.info("去重之后的数据量：" + list.size());

        System.out.println("全部数据量：" + listAll.size());
        System.out.println("去重之后的数据量：" + list.size());


        if (type.intValue() == 1){
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
        System.out.println("==========================================此程序运行结束========================================");

    }

    /**
     * 文思海辉-中标
     * @param type
     * @param date
     */
    @Override
    public void getWenSiHaiHuib(Integer type, String date) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] a = {"RAP","信创","智慧","天眼","信息技术应用创新","IOT","区块链","物联网","互联网+","IT信息产业转型","数字中国","信创产业链","新基建","新型基础建设","信创项目","国家信创园","信创产业园","信创发展","信创通用技术","信创产品","信创体系","SCM","天网","车联网","车路网","街数字","楼数字","物联卡","EAM","数字化","技术服务"};
        String[] dw = {"电力","电厂","电场","制造","汽车","教育","通信","能源","大学","学校","本科","小学","幼儿园","高校","高职","党校","军校","艺校","专升本","理科","文科","医科","学院","师范","院校","理工","体校","医校","技校","中专","职高","职中","中学","附小","托儿所","培训中心","职教中心","教育中心","自然考试","成人教育","远程教育","电网","发电","国电","供电","煤电","核电","水电","电能","风电","电站","热电","电建","华电集团","大唐集团","电务公司","粤电","能耗","能效","节能","风能","地热能","潮汐能","太阳能","电池","核能","中核","加工","装备","重工","轻工","纺织","钢铁","型材","板材","柴油机","不锈钢","电器","材料","家具","机械","空调","印刷","纸业","工业","云南云铝","铜业","锡业","精密铸造","轮胎","电梯","橡胶","润滑油","制品","铝业","海尔集团","电动车","轿车","网约车","商用车","特种车","越野车","工业车","出租车","机动车","旅游车","二手车","共享车","重汽","手机","信号","基站","通讯","信息技术","信息科技","信息安全","信息网络","信息产业","有线网络","有限网络","无线电","辅导","课程","培训","家教","燃气管理","供热","矿产资源","钻井","地矿","石油管理","矿山","矿产","煤矿","石油化工","供力","油田","电业局"};
        List<String> b = LogUtils.readRule("keyWords");
        String[] blacks = {"物业管理","保洁服务","安保服务","保安服务","物业服务","硬件","配件","耗材","家具","办公用品","组件","混凝土","钢管","配电柜","复印机","打印机","粉盒","硒鼓","辅材","标牌","标示牌","宣传视频","宣传物资","慰问品","印刷品","印刷服务","电梯","空调","综合布线","电线","电缆","管件","后勤服务","后勤管理","公务用车","用车服务","公务车","车辆采购","商务车","物业保洁","印刷采购"};


        for (String str : a) {
            for (String str2 : dw) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:3 OR progid:5)  AND catid:[* TO 100] AND title:\"" + str + "\"  AND allcontent:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
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
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str+"&"+str2);
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
        for (String str : b) {
            for (String str2 : dw) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:3 OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\"  AND allcontent:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
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
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str+"&"+str2);
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


        log.info("全部数据量：" + listAll.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        ArrayList<String> arrayList = new ArrayList<>();

        //关键词a 和 定位词
        for (String key : a) {
            for (String str2 : dw) {
                arrayList.add(key+"&"+str2);
            }
        }
        //关键词a 和 定位词
        for (String key : b) {
            for (String str2 : dw) {
                arrayList.add(key+"&"+str2);
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
        System.out.println("全部数据量：" + listAll.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (type.intValue() ==1){
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

    @Override
    public void getWenSiHaiHuib2_2(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] a = {"RAP","信创","天眼","SCM","EAM"};
        String[] b = {"安全监控","供应链运营管理系统","供应链信息系统","供应链金融平台","供应链金融服务平台","供应链系统解决方案","供应链信息平台","供应链大数据应用项目","供应链数字金融项目","供应链协同服务平台","供应链综合服务平台","供应链业务系统","供应链综合管理平台","供应链综合管理系统","供应链协同服务系统","供应链综合服务系统","供应链金融系统","供应链服务平台","供应链服务系统","供应链业务平台","固定资产管理信息系统","固定资产管理系统","固定资产管理信息平台","固定资产管理平台","资产经营管理系统","实物资产系统","资产管理系统","资产管理云平台","资产管理平台","资产托管系统","资产管理软件","资产交易管理信息平台","资产交易平台","资产管理信息化系统","资产数字管理平台","资产精细化管理综合平台","资产监督管理平台","资产监督平台","资产监督管理信息化系统","资产一体化管理平台","资产综合信息管理平台","资产管理信息系统","资产数字化运营管理平台","资产数字化管理平台","资产数字化管理系统","资产数字化运营管理系统","资产一体化管理系统","资产数字管理系统","资产动态管理云平台","资产动态管理系统","资产动态管理平台","资产管理信息化平台","资产盘点系统","资产盘点平台","资产盘点软件","资产盘点管理系统","资产盘点管理平台","资产盘点管理软件","资产管理工具软件","接口管理平台","接口文档管理平台","接口文档管理工具","智慧园区","智慧城市","安保监控","安防监控"};

        String[] bq ={"教育","能源","电力","新能源","制造","汽车","通信","教育服务"};


        for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:0 OR progid:3) AND catid:[* TO 100] AND title:\"" + str +  "\"", str, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str);
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
        for (String str : b) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:0 OR progid:3) AND catid:[* TO 100] AND title:\"" + str +  "\"", str, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str);
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
/*
        for (String str : a) {
            for (String str2 : bq) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:0 OR progid:3) AND catid:[* TO 100] AND title:\"" + str + "\"  AND zhaoBiaoUnit:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str+"&"+str2);
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
        for (String str : b) {
            for (String str2 : bq) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:0 OR progid:3) AND catid:[* TO 100] AND title:\"" + str + "\"  AND zhaoBiaoUnit:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str+"&"+str2);
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
*/

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


        log.info("全部数据量：" + listAll.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        ArrayList<String> arrayList = new ArrayList<>();

        //关键词a 和 定位词
        for (String key : a) {
           /* for (String str2 : bq) {
                arrayList.add(key+"&"+str2);
            }*/
            arrayList.add(key);
        }
        //关键词a 和 定位词
        for (String key : b) {
            /*for (String str2 : bq) {
                arrayList.add(key+"&"+str2);
            }*/
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
        System.out.println("全部数据量：" + listAll.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSaveNew(content)));
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
    public void getDataFromZhongTaiAndSaveNew(NoticeMQ noticeMQ) {
        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            try {
                saveIntoMysql(resultMap,INSERT_ZT_1);
                log.info("数据库存储--->{}",noticeMQ.getContentid());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void getAolinbasi2(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] a = {"内窥镜","胃镜","肠镜","腹腔镜","胃肠镜","宫腔镜","支气管镜","鼻咽喉镜","胆道镜","耳鼻喉镜","宫腔镜","电切镜","耳鼻喉镜","腹腔镜","腔镜","十二指肠镜","超声镜","小肠镜","胸腔镜","宫腔电切","宫腔电切镜","内镜","电子镜","气管镜","输尿管镜","电子胃镜","电子腹腔镜","电子结肠镜","电切镜","纤维支气管镜","膀胱镜","呼吸镜","窥镜","电子膀胱镜","输尿管软镜","电子内窥镜","电子支气管镜","腔镜","电子肠镜","电子胃肠镜","超声刀","能量平台","小探头","测漏器","电刀","光学视管","气腹机","肾盂镜","探头驱动器","纤维镜","胸腔镜","硬性镜","维护保养装置"};
        String[] b = {"鼻咽喉","摄像系统","超声","摄像平台","支气管","输尿管","胃肠","宫腔","腹腔","呼吸","膀胱","消化","胆道","清洗消毒","整体手术室","影像装置","图像处理","摄像头","监视器","保养装置","光源","台车","主机","显示器","适配器"};


        for (String str : a) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\"", str, 2);
                log.info(str.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag){
                                listAll.add(data);
                                data.setKeyword(str);
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
        for (String str : b) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\"", str, 2);
                log.info(str.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag){
                                listAll.add(data);
                                data.setKeyword(str);
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


        log.info("全部数据量：" + listAll.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        ArrayList<String> arrayList = new ArrayList<>();

        //关键词a 和 定位词
        for (String key : a) {
            arrayList.add(key);
        }
        //关键词a 和 定位词
        for (String key : b) {
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
        System.out.println("全部数据量：" + listAll.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (type.intValue() ==1){
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

    @Override
    public void getAolinbasi2_3(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] a = {"内窥镜","胃镜","肠镜","腹腔镜","胃肠镜","宫腔镜","支气管镜","鼻咽喉镜","胆道镜","耳鼻喉镜","宫腔镜","电切镜","耳鼻喉镜","腹腔镜","腔镜","十二指肠镜","超声镜","小肠镜","胸腔镜","宫腔电切","宫腔电切镜","内镜","电子镜","气管镜","输尿管镜","电子胃镜","电子腹腔镜","电子结肠镜","电切镜","纤维支气管镜","膀胱镜","呼吸镜","窥镜","电子膀胱镜","输尿管软镜","电子内窥镜","电子支气管镜","腔镜","电子肠镜","电子胃肠镜","超声刀","能量平台","小探头","测漏器","电刀","光学视管","气腹机","肾盂镜","探头驱动器","纤维镜","胸腔镜","硬性镜","维护保养装置"};
        String[] b = {"鼻咽喉","摄像系统","超声","摄像平台","支气管","输尿管","胃肠","宫腔","腹腔","呼吸","膀胱","消化","胆道","清洗消毒","整体手术室","影像装置","图像处理","摄像头","监视器","保养装置","光源","台车","主机","显示器","适配器"};
        //String[] bq = {"医疗单位"};

        /*for (String str : a) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\"", str, 2);
                log.info(str.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag){
                                listAll.add(data);
                                data.setKeyword(str);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }));
        }*/


        for (String str : b) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND title:\"" + str + "\"  AND zhaoFirstIndustry:\"" + "医疗单位" + "\"", str, 2);
                log.info(str.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag){
                                listAll.add(data);
                                data.setKeyword(str);
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


        log.info("全部数据量：" + listAll.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        ArrayList<String> arrayList = new ArrayList<>();

        //关键词a
        /*for (String key : a) {
            arrayList.add(key);
        }*/
        //关键词b
        for (String key : b) {
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
        System.out.println("全部数据量：" + listAll.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave2(content,"")));
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

    public void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {
        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            try {
                //saveIntoMysql(resultMap,INSERT_ZT_RESULT_HXR);
                //String str="UPDATE han_unit_zbdw SET top02=? where unit_name=?";
                String str ="INSERT INTO han_unit_zbdw (unit_name,top02) VALUES (?,?)";
                bdJdbcTemplate.update(str,resultMap.get("zhong_biao_unit"),resultMap.get("link_phone"));

                log.info("修改的中标单位:{}",resultMap.get("zhong_biao_unit"));
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    public void getDataFromZhongTaiAndSave2(NoticeMQ noticeMQ,String num) {
        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            try {
                saveIntoMysql(resultMap,INSERT_ZT_1);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
       log.info("数据存库---{}",noticeMQ.getContentid().toString());
    }



    @Override
    public void getAolinbasi2_qw(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //String[] a = {"内窥镜","胃镜","肠镜","腹腔镜","胃肠镜","宫腔镜","支气管镜","鼻咽喉镜","胆道镜","耳鼻喉镜","宫腔镜","电切镜","耳鼻喉镜","腹腔镜","腔镜","十二指肠镜","超声镜","小肠镜","胸腔镜","宫腔电切","宫腔电切镜","内镜","电子镜","气管镜","输尿管镜","电子胃镜","电子腹腔镜","电子结肠镜","电切镜","纤维支气管镜","膀胱镜","呼吸镜","窥镜","电子膀胱镜","输尿管软镜","电子内窥镜","电子支气管镜","腔镜","电子肠镜","电子胃肠镜","超声刀","能量平台","小探头","测漏器","电刀","光学视管","气腹机","肾盂镜","探头驱动器","纤维镜","胸腔镜","硬性镜","维护保养装置"};
        String[] b = {"鼻咽喉","摄像系统","超声","摄像平台","支气管","输尿管","胃肠","宫腔","腹腔","呼吸","膀胱","消化","胆道","清洗消毒","整体手术室","影像装置","图像处理","摄像头","监视器","保养装置","光源","台车","主机","显示器","适配器"};

        for (String str : b) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND zhaoFirstIndustry:\"" + "医疗单位" + "\" AND allcontent:\"" + str + "\"", str, 2);
                log.info(str.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag){
                                listAll.add(data);
                                data.setKeyword(str);
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


        log.info("全部数据量：" + listAll.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        ArrayList<String> arrayList = new ArrayList<>();

        //关键词a 和 定位词
        /*for (String key : a) {
            arrayList.add(key);
        }*/
        //关键词a 和 定位词
        for (String key : b) {
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
        System.out.println("全部数据量：" + listAll.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave2(content,"")));
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

    @Override
    public void getBeiDeng2016(Integer type, String date) throws Exception{

        HashMap<String, String> dataMap = new HashMap<>();
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        String[] b = {"医院","诊所","门诊","保健院","健康委员会","医学院","体检中心","健康局","医院部","药房","卫生院","医疗保障局","合作医疗","医药服务管理司","兽医实验室","医药","精神病院","防治院","血液中心","眼科中心","治疗中心","保健中心","保健所","血管病研究所","防治所","外科中心","康复中心","透析中心","正畸中心","荣军院","防治中心","保健站","列腺病研究所","职业病院","防治站","产院","急救中心","卫生局","卫生厅","防治办公室","卫生保健中心","医疗中心","卫生中心","门诊部","卫生服务站","医检所","制剂室","药交所","眼科","医保","医疗保障","卫健委","戒毒所","敬老院","疗养院","眼病防治所","矫治所","结核病防治所","休养所","血站","福利院","医疗机构","病防治办公室","计划生育","生育委员","计生委","大健康","同仁堂","江中集团","医学","健康科技","养生堂","保健品","诊断","康宁","制药","药业","药集团","医疗集团","精神卫生","药店","军医","医用","医疗","诊疗","残联","医护","卫生所","卫生院 ","卫生院校","医科大学","妇幼","健康中心","运动康复","中医馆","预防控制","医务室"};
        //全文+辅助
        String[] qwAndFz = {"pcr","功能分析仪","氧含量测定仪","钾分析仪","二氧化碳电极","钙电极","钾电极","选择性电极","锂电极","参比电极","氯电极","钠电极","葡萄糖电极盒","葡萄糖电极","乳酸电极","氧电极","电极膜","钙分析仪","氯分析仪","钠分析仪","电解质生化分析仪","金标测试仪","浊度分析仪","光法分析仪","恒温杂交仪","扫描图像分析系统","标本测定装置","样本分析设备","成分分析仪器","电泳仪","流式点阵仪器","色谱柱","质谱系统","层析柱","检测阅读系统","信号扩大仪","电泳装置","电泳槽","缓冲液槽","采样设备","采样器具","采样储藏管","标本采集保存管","采样器","取样器","样本采集器具","切片机","整体切片机","组织脱水机","染色机","包埋机","制片机","涂片机","组织处理机","轮转式切片机","平推式切片机","振动式切片机","冷冻切片机","包埋机热台","包埋机冷台","自动涂片机","滴染染色机","裂解仪","样本处理仪器","样本裂解仪","离心机","培养设备","孵育设备","恒温箱","孵育器","恒温培养箱","培养箱","振荡孵育器","检验辅助设备","洗板机","计数板","自动加样系统","低温储存设备","样本处理系统","样品前处理系统","样品检查自动化系统","样品处理系统","样品后处理系统","分杯处理系统","样本孵育系统","超净装置","自动进样系统","真空冷冻干燥箱","纯水机","电刀笔","电极","电极板","电缆线","低温冰箱","超低温冰箱","电化学仪器","电导率仪","实验天平","分光光度计","采样系统","测氧仪","蛋白电泳","电镜","电泳涂装设备","干式恒温器","观察扫描仪","观片灯","光度计","恒温水槽","恒温水浴箱","冷冻箱","生物容器","水机"};
        //全文
        List<String> qw = LogUtils.readRule("keyWords");

        for (String str : qw) {
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"", str, 2);
                log.info(str.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag){
                                listAll.add(data);
                                data.setKeyword(str);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
        }


        for (String str : qwAndFz) {
            for (String str2 : b) {
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"  AND zhaoBiaoUnit:\"" + str2 + "\"", str+"&"+str2, 2);
                log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag){
                                listAll.add(data);
                                data.setKeyword(str+"&"+str2);
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
        for (String str : qwAndFz) {
            for (String str2 : b) {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str+"&"+str2);
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

        log.info("全部数据量：" + listAll.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        ArrayList<String> arrayList = new ArrayList<>();

        //关键词全文
        for (String a :qw){
            arrayList.add(a);
        }

        for (String key : qwAndFz) {
            for (String k : b){
                arrayList.add(key+"&"+k);
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
        System.out.println("全部数据量：" + listAll.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave2(content,"")));
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

    @Override
    public void getBeiDeng20162(Integer type, String date) throws Exception {
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] b = {"医院","诊所","门诊","保健院","健康委员会","医学院","体检中心","健康局","医院部","药房","卫生院","医疗保障局","合作医疗","医药服务管理司","兽医实验室","医药","精神病院","防治院","血液中心","眼科中心","治疗中心","保健中心","保健所","血管病研究所","防治所","外科中心","康复中心","透析中心","正畸中心","荣军院","防治中心","保健站","列腺病研究所","职业病院","防治站","产院","急救中心","卫生局","卫生厅","防治办公室","卫生保健中心","医疗中心","卫生中心","门诊部","卫生服务站","医检所","制剂室","药交所","眼科","医保","医疗保障","卫健委","戒毒所","敬老院","疗养院","眼病防治所","矫治所","结核病防治所","休养所","血站","福利院","医疗机构","病防治办公室","计划生育","生育委员","计生委","大健康","同仁堂","江中集团","医学","健康科技","养生堂","保健品","诊断","康宁","制药","药业","药集团","医疗集团","精神卫生","药店","军医","医用","医疗","诊疗","残联","医护","卫生所","卫生院 ","卫生院校","医科大学","妇幼","健康中心","运动康复","中医馆","预防控制","医务室"};
        //全文+辅助
        String[] qwAndFz = {"功能分析仪","氧含量测定仪","钾分析仪","二氧化碳电极","钙电极","钾电极","选择性电极","锂电极","参比电极","氯电极","钠电极","葡萄糖电极盒","葡萄糖电极","乳酸电极","氧电极","电极膜","钙分析仪","氯分析仪","钠分析仪","电解质生化分析仪","金标测试仪","浊度分析仪","光法分析仪","恒温杂交仪","扫描图像分析系统","标本测定装置","样本分析设备","成分分析仪器","电泳仪","流式点阵仪器","色谱柱","质谱系统","层析柱","检测阅读系统","信号扩大仪","电泳装置","电泳槽","缓冲液槽","采样储藏管","标本采集保存管","采样器","取样器","样本采集器具","切片机","整体切片机","组织脱水机","染色机","包埋机","制片机","涂片机","组织处理机","轮转式切片机","平推式切片机","振动式切片机","冷冻切片机","包埋机热台","包埋机冷台","自动涂片机","滴染染色机","裂解仪","样本处理仪器","样本裂解仪","离心机","培养设备","孵育设备","恒温箱","孵育器","恒温培养箱","培养箱","振荡孵育器","检验辅助设备","洗板机","计数板","自动加样系统","低温储存设备","样本处理系统","样品前处理系统","样品检查自动化系统","样品处理系统","样品后处理系统","分杯处理系统","样本孵育系统","超净装置","自动进样系统","真空冷冻干燥箱","电刀笔","电极","低温冰箱","超低温冰箱","电化学仪器","电导率仪","实验天平","分光光度计","采样系统","测氧仪","蛋白电泳","电镜","电泳涂装设备","干式恒温器","观察扫描仪","光度计","光化学反应仪","恒温水槽","恒温水浴箱","冷冻箱","色谱仪","脱水机","冷水机","自动洗涤脱水机"};
        //全文
        List<String> qw = LogUtils.readRule("keyWords");

        //黑词
        String [] blacks={"废标","流标","终止","违规","招标异常","无效公告","暂停公告","失败公告","终止公告","路灯采购","奶粉采购","采购家具","空调采购","多联机空调","锅炉房设备采购","电视机采购","采购电视机","环卫工具采购","印刷采购","加装电梯","被服采购","家具采购","石材采购","停车设备采购","电梯采购","垃圾压缩成套设备","窗帘采购","混凝土招标","数控机床附件","监理","工程监理","施工监理","广告宣传","临建食堂购餐桌椅","食堂食材采购","食堂食品","员工工装","热机组采购","竹地板材料","有限公司轮胎","保险采购","苗木采购","鱼苗采购","多联机配件","污水处理设备","白色OPPOR9手机","货物类采购","水分配系统","采购日常百货","石材招标","玻璃隔断","玻璃栏杆","医院勘察采购","防褥疮床垫","清洁能源示范","铅桶采购","笔记本电脑采购","车辆维修","人才招聘","保温材料更换","物业管理服务","空调管路系统","安保服务","空调维保","污水处理系统","改造安装防护门",
                "热水系统改造","排风系统升级改造","空调系统改造","采购安装风管机空调","消防维保","绿化带拆除","电梯维保服务","中央空调清洗","消防维修","地下车库加建","食堂对外承包","保护测评","保洁服务","监理单位","监理企业","招租","设施改造","房租","出租","选择招标代理机构","水杯维修","食堂外包服务","后厨管理承包","食堂承包经营","食堂等物业服务","后勤保洁服务","食堂项目承包","肉类配送","保安服务","车转让","变压器扩容","网络招聘服务","保险联网结算系统","宣传片投放","广告服务","工程垃圾设备","景观节点整治","塌方除理","房屋拍卖","汽车采购","工程造价咨询","整体板房询价","租赁服务","垃圾清运","外墙保温","康复大楼工程监理","宣传策划","车辆租赁","办公系统开发","水体治理","审计业务","养老购买","坑塘整治","后勤保洁管理服务","设备维修维护保养",
                "子女保险","保险辅助","保险服务","医疗责任保险","意外保险","伤害保险","运输保险","大病医疗保险","中邮保险","保险统保","保险运营"};
        //List<String> blacks = LogUtils.readRule("blockKeys");

        for (String str : qw) {
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"", str, 2);
                log.info(str.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag){
                                String heici ="";
                                for (String black : blacks) {
                                    if (data.getTitle().contains(black)){
                                        heici +=black+"、";
                                    }
                                }
                                if (StrUtil.isNotEmpty(heici)){
                                    data.setHeici(heici.substring(0, heici.length() - 1));
                                }
                                listAll.add(data);
                                data.setKeyword(str);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
        }


        for (String str : qwAndFz) {
            for (String str2 : b) {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"  AND zhaoBiaoUnit:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    String heici ="";
                                    for (String black : blacks) {
                                        if (data.getTitle().contains(black)){
                                            heici +=black+"、";
                                        }
                                    }
                                    if (StrUtil.isNotEmpty(heici)){
                                        data.setHeici(heici.substring(0, heici.length() - 1));
                                    }
                                    listAll.add(data);
                                    data.setKeyword(str);
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
        for (String str : qwAndFz) {
            for (String str2 : b) {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    String heici ="";
                                    for (String black : blacks) {
                                        if (data.getTitle().contains(black)){
                                            heici +=black+"、";
                                        }
                                    }
                                    if (StrUtil.isNotEmpty(heici)){
                                        data.setHeici(heici.substring(0, heici.length() - 1));
                                    }
                                    listAll.add(data);
                                    data.setKeyword(str);
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

        log.info("全部数据量：" + listAll.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        ArrayList<String> arrayList = new ArrayList<>();

        //关键词全文
        for (String a :qw){
            arrayList.add(a);
        }

        for (String key : qwAndFz) {
            /*for (String k : b){
                arrayList.add(key+"&"+k);
            }*/
            arrayList.add(key);
        }

        //去重前的统计量
        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : listAll) {
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
        System.out.println("全部数据量：" + listAll.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(60);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave2(content,"")));
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


    public void saveIntoMysql(Map<String, Object> map ,String table){
        bdJdbcTemplate.update(table,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"),map.get("heici"));
    }

    // 标的物匹配到的关键词
    private String[] keywords = {};

    private String SQL = "insert into aaa_yiliao_poc(infoId, sum, sum_unit, keyword, serial_number, name, brand, model, " +
            "number, number_unit, price, price_unit, total_price, total_price_unit, configuration) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public void handleForYiLiao(Long contentId){
        String result = TargetService.extract(contentId,"http://172.18.30.243:2023/inspect");
        if (StringUtils.isNotBlank(result)){
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject != null && jsonObject.containsKey("content_target")){
                JSONObject resultObject = jsonObject.getJSONObject("content_target");
                if (resultObject != null && resultObject.containsKey("target_details")){
                    String sum = resultObject.getString("sum");
                    String sum_unit = resultObject.getString("sum_unit");
                    JSONArray targetDetails = resultObject.getJSONArray("target_details");
                    if (targetDetails != null && targetDetails.size() > 0){
                        for (int i = 0; i < targetDetails.size(); i++) {
                            String serial_number = "";
                            String name = "";
                            String brand = "";
                            String model = "";
                            String number = "";
                            String number_unit = "";
                            String price = "";
                            String price_unit = "";
                            String total_price = "";
                            String total_price_unit = "";
                            String configuration = "";
                            String keyword = "";
                            JSONObject finalObject = targetDetails.getJSONObject(i);
                            if (finalObject != null){
                                serial_number = finalObject.getString("serial_number");
                                name = finalObject.getString("name");
                                brand = finalObject.getString("brand");
                                model = finalObject.getString("model");
                                number = finalObject.getString("number");
                                number_unit = finalObject.getString("number_unit");
                                price = finalObject.getString("price");
                                price_unit = finalObject.getString("price_unit");
                                total_price = finalObject.getString("total_price");
                                total_price_unit = finalObject.getString("total_price_unit");
                                JSONArray configurations = finalObject.getJSONArray("configurations");
                                if (configurations != null && configurations.size() > 0){
                                    for (int j = 0; j < configurations.size(); j++) {
                                        JSONObject jsonObject1 = configurations.getJSONObject(j);
                                        String key = jsonObject1.getString("key");
                                        String value = jsonObject1.getString("value");
                                        configuration += key + "：" + value + "：";
                                    }
                                }
                                if (StringUtils.isNotBlank(configuration)){
                                    configuration = configuration.substring(0, configuration.length() - 1);
                                }
                                // 进行匹配关键词操作
                                if (keywords != null && keywords.length > 0){
                                    String allField = name + "&" + brand + "&" + model + "&" + configuration;
                                    for (String key : keywords) {
                                        if (allField.toUpperCase().contains(key.toUpperCase())){
                                            keyword += key + "，";
                                        }
                                    }
                                    if (StringUtils.isNotBlank(keyword)){
                                        keyword = keyword.substring(0, keyword.length() - 1);
                                    }
                                }
                                // 进行数据保存操作
                            }
                            // 进行数据库保存操作
                            bdJdbcTemplate.update(SQL, contentId, sum, sum_unit, keyword, serial_number, name, brand, model, number, number_unit, price, price_unit, total_price, total_price_unit, configuration);
                        }
                    }
                }
            }
        }
    }
}
