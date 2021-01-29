package com.qianlima.offline.service;

import com.qianlima.offline.bean.Area;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.middleground.NotBaiLianZhongTaiService;
import com.qianlima.offline.util.ContentSolr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class BoKeService {

    @Autowired
    private ContentSolr contentSolr;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    private NotBaiLianZhongTaiService zhongTaiService;

    HashMap<Integer, Area> areaMap =  new HashMap<>();

    @PostConstruct
    public void init() {
        List<Map<String, Object>> maps = gwJdbcTemplate.queryForList("SELECT * FROM phpcms_area");
        for (Map<String, Object> map : maps) {
            Area area = new Area();
            area.setAreaid(Integer.valueOf(map.get("areaid").toString()));
            area.setName(map.get("name").toString());
            area.setParentid(map.get("parentid").toString());
            area.setArrparentid(map.get("arrparentid").toString());
            areaMap.put(Integer.valueOf(map.get("areaid").toString()),area);
        }
    }

    public void getSolrAllField() throws Exception{

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();


        //行业词
        String[] dds = {"医院","急救","抢救","诊所","门诊","救护","医学院","救治","保健院","健康委员会","体检中心","健康局","医院部","药房","卫生院","医疗","卫生所","医疗保障局","合作医疗","医药服务管理司","兽医实验室","医药","精神病院","防治院","血液中心","眼科中心","治疗中心","保健中心","保健所","血管病研究所","防治所","外科中心","康复中心","透析中心","正畸中心","荣军院","防治中心","保健站","列腺病研究所","职业病院","防治站","产院","急救中心","卫生局","卫生厅","防治办公室","卫生保健中心","医疗中心","卫生中心","门诊部","卫生服务站","医检所","制剂室","药交所","戒毒所","敬老院","疗养院","眼病防治所","矫治所","结核病防治所","休养所","血站","福利院","医疗机构","病防治办公室","疾病","健康","妇幼","军医","医用","诊疗","残联","医护","卫生院","卫生院校","医科大学","健康中心","运动康复","中医","预防控制","医务","药业","制药","药品","药物","兽药","中药","医药","药材","基因","生物科技","生物技术","畜牧","养殖","饲料","饲养","食料","农牧","牧业","畜业","喂养","驯养","放牧","圈养","大学","艺校","军校","医校","党校","职校","技校","联校","团校","体校","学院","院校","专科","中专","大专","本科","专修","专升本","研究生","博士生","一本","二本","三本","985","211","理科","食品","饮料","肉制品","面制品","调味料","水产品","农产品","速食","饮品","乳业","乳品","啤酒","白酒","粮油","文科","医科","高专","预科","理工","成教","函授","网络教育","自考","自学考试","成人教育","技能","职业","中职","高职","高校","职专"};

        //产品词
        String[] ees = {"实验室纯水机","体外诊断试剂","通风柜","配药柜","医用冷藏箱","医用气溶胶吸附器","黄疸治疗仪","经皮黄疸测试仪","医用离心机","病理取材台","二氧化碳培养箱","紫外线消毒器","病理取材台","电热恒温培养箱","移动pcr方舱实验室","核酸采样亭","核酸采样工作站","紫外线杀菌车","雾化消毒机器人","负压隔离舱负压担架","口罩"};

        // 全文检索含有“产品词a”，AND标题 OR 自提招标单位检索含有“行业词a”
        for (String dd : dds) {
            for (String ee : ees) {
                String keyword = dd + "&" + ee;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian( "yyyymmdd:[20161001 TO 20201022] AND progid:3 AND catid:[* TO 100] AND (title:\""+dd+"\" OR zhaoBiaoUnit:\""+dd+"\") AND allcontent: \""+ee+"\"", keyword, 3);
                log.info("keyword:{}查询出了size：{}条数据", keyword, mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        list1.add(data);
                        if (!dataMap.containsKey(data.getContentid().toString())) {
                            list.add(data);
                            data.setKeyword(keyword);
                            dataMap.put(data.getContentid().toString(), "0");
                        }
                    }
                }
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


        log.info("全部数据量："+list1.size());
        log.info("去重之后的数据量："+list.size());
        log.info("==========================");


        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() ->  getDataFromZhongTaiAndSave(content)));
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
        log.info("数据全部跑完啦,总数量为：" +1);

    }


    public void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ){
        boolean result = zhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = zhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            zhongTaiService.saveIntoMysql(resultMap);
        }

    }

}
