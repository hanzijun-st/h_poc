package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.extract.TargetService;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.NewBiaoDiWuService;
import com.qianlima.offline.service.PocService;
import com.qianlima.offline.service.ZhongTaiBiaoDiWuService;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.TestService;
import com.qianlima.offline.util.ContentSolr;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.UpdateContentSolr;
import com.qianlima.offline.util.ZTStringUtil;
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
    private ZhongTaiBiaoDiWuService bdwService;
    @Autowired
    private NewBiaoDiWuService newBiaoDiWuService;
    @Autowired
    private CusDataFieldService cusDataFieldService;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    public String INSERT_ZT_RESULT_HXR = "INSERT INTO han_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    @Override
    public void getBdw() {
        try {
            bdwService.getSolrAllField2("hBdw");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {
        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            try {
                saveIntoMysql(resultMap,INSERT_ZT_RESULT_HXR);
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
                saveIntoMysql(resultMap,INSERT_ZT_RESULT_HXR);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("存库剩余的数据量：{}",num);
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
                map.get("code"), map.get("isfile"), map.get("keyword_term"));
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
