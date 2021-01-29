package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.extract.target.TargetExtractService;
import com.qianlima.offline.bean.ConstantBean;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.rule02.MyRuleUtils;
import com.qianlima.offline.util.ContentSolr;
import com.qianlima.offline.util.KeyUtils;
import com.qianlima.offline.util.MathUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class LianYing01Service {

    @Autowired
    private ContentSolr contentSolr;

    private static final String UPDATA_SQL_01 = "INSERT INTO poc_biaodiwu (contentid, keyword, taskId, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    private MyRuleUtils myRuleUtils;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    private CusDataFieldService cusDataFieldService;

    public void getSolrAllField() throws Exception{

        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();

        String[] aas = { "MR ","CT ","XR ","DR" };

        String[] bbs = { "直线加速器","正电子发射体层摄影装置","正电子发射体层摄影仪","正电子发射体层摄影设备","正电子发射体层摄影机","正电子发射计算机断层显像装置","正电子发射计算机断层显像仪","正电子发射计算机断层显像设备","正电子发射计算机断层显像机","医用磁共振装置","医用磁共振仪","医用磁共振设备","医用磁共振机","医用CT","血管造影装置","血管造影仪","血管造影设备","血管造影机","双源CT","双排CT","数字减影装置","数字减影仪","数字减影设备","数字减影机","能谱CT","螺旋CT","计算机体层摄影装置","计算机体层摄影仪","计算机体层摄影设备","计算机体层摄影机","计算机断层摄影装置","计算机断层摄影仪","计算机断层摄影设备","计算机断层摄影机","计算机断层扫描装置","计算机断层扫描仪","计算机断层扫描设备","计算机断层扫描机","计算机断层成像装置","计算机断层成像仪","计算机断层成像设备","计算机断层成像机","核磁共振装置","核磁共振仪","核磁共振设备","核磁共振谱机","高端CT","定位CT","大孔径CT","磁共振装置","磁共振影像装置","磁共振影像仪","磁共振影像设备","磁共振影像机","磁共振仪","磁共振项目","磁共振设备","磁共振机","磁共振成像装置","磁共振成像仪","磁共振成像设备","磁共振成像机","磁共振采购","宝石CT","X射线成像装置","X射线成像仪","X射线成像设备","X射线成像机","T核磁共振装置","T核磁共振仪","T核磁共振设备","T核磁共振机","T磁共振装置","T磁共振仪","T磁共振设备","T磁共振机","RevolutionCT","PET-CT装置","PETCT装置","PET-CT仪","PETCT仪","PET-CT设备","PETCT设备","PET-CT机","PETCT机","PET/CT装置","PET/CT仪","PET/CT设备","PET/CT机","MR装置","MR仪","MR设备","MR机","DSA装置","DSA仪","DSA设备","DSA机","CT装置","CT影像装置","CT影像仪","CT影像设备","CT影像机","CT仪","CT项目","CT探测仪","CT设备","CT扫描装置","CT扫描仪","CT扫描设备","CT扫描机","CT机","64排CT","320CT","256排CT","16排CT","128排CT","PET-CT","PETCT","PET/CT","乳腺机","乳腺X射线","数字乳腺X射线","C型臂","C臂","移动DR","移动X射线","62排CT","核医学","SPECT/CT"};

        String[] ccs = { "智能医院", "智能医疗", "智能化医疗", "智慧医院", "智慧医疗", "治疗信息化", "治疗数字化", "治疗软件", "诊疗信息化", "诊疗数字化", "诊疗软件", "诊断信息化", "诊断数字化", "诊断软件", "云医院", "云医疗", "远程诊疗信息化", "远程诊疗数字化", "远程诊疗软件", "远程医疗信息化", "远程医疗数字化", "远程医疗软件", "远程信息化", "远程数字化", "远程软件", "预约信息化", "预约数字化", "预约软件", "影像医学信息化", "影像医学数字化", "影像医学软件", "影像信息化", "影像数字化", "影像软件", "移动影像信息化", "移动影像数字化", "移动影像软件", "移动车载CT信息化", "移动车载CT数字化", "移动车载CT软件", "医院智能化", "医院云信息化", "医院云数字化", "医院云软件", "医院影像信息化", "医院影像数字化", "医院影像软件", "医院信息化", "医学影像信息化", "医学影像数字化", "医学影像软件", "医学摄影信息化", "医学摄影数字化", "医学摄影软件", "医学成像信息化", "医学成像数字化", "医学成像软件", "医疗智能化", "医疗云", "医疗影像信息化", "医疗影像数字化", "医疗影像软件", "医疗信息化", "医疗人工智能", "医联体信息化", "医联体数字化", "医联体软件", "医共体信息化", "医共体数字化", "医共体软件", "信息平台建设", "信息平台搭建", "信息安全测评", "心电信息化", "心电数字化", "心电软件", "数字化医疗", "摄像信息化", "摄像数字化", "摄像软件", "人工智能医疗", "人工智能", "区域影像信息化", "区域影像数字化", "区域影像软件", "区域医疗信息化", "区域医疗数字化", "区域医疗软件", "内视镜信息化", "内视镜数字化", "内视镜软件", "内窥镜信息化", "内窥镜数字化", "内窥镜软件", "内镜信息化", "内镜数字化", "内镜软件", "叫号信息化", "叫号数字化", "叫号软件", "胶片信息化", "胶片数字化", "胶片软件", "会诊信息化", "会诊数字化", "会诊软件", "放射信息化", "放射数字化", "放射软件", "电子病历", "等级保护", "等保测评", "成像信息化", "成像数字化", "成像软件", "超声信息化", "超声数字化", "超声影像归档和通信系统", "病理信息化", "病理数字化", "病理软件", "内镜PACS", "PACS信息化", "PACS数字化", "PACS软件", "PACS系统", "预约系统", "叫号系统", "远程系统", "胶片系统", "会诊系统", "医联体系统", "医共体系统", "治疗信息平台", "诊疗信息平台", "诊断信息平台", "远程诊疗信息平台", "远程医疗信息平台", "远程信息平台", "预约信息平台", "影像医学信息平台", "影像信息平台", "移动影像信息平台", "移动车载CT信息平台", "医院智能化信息平台", "医院云信息平台", "医院影像信息平台", "医院信息平台", "医学影像信息平台", "医学摄影信息平台", "医学成像信息平台", "医疗智能化信息平台", "医疗云信息平台", "医疗影像信息平台", "医疗信息平台", "医联体信息平台", "医共体信息平台", "摄像信息平台", "区域影像信息平台", "区域医疗信息平台", "叫号信息平台", "胶片信息平台", "会诊信息平台", "成像信息平台", "病理信息平台", "PACS信息平台"};

        for (String aa : aas) {
            String keyword = aa ;
            List<NoticeMQ> mqEntities02 = contentSolr.companyResultsBaoXian( "yyyymmdd:[20200101 TO 20201231] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND title:\""+aa+"\"", keyword, 101);
            log.info("keyword:{}查询出了size：{}条数据", keyword, mqEntities02.size());
            if (!mqEntities02.isEmpty()) {
                for (NoticeMQ data : mqEntities02) {
                    list1.add(data);
                    if (!dataMap.containsKey(data.getContentid().toString())) {
                        list.add(data);
                        data.setKeyword(keyword);
                        data.setKeywordTerm("设备类");
                        dataMap.put(data.getContentid().toString(), "0");
                    }
                }
            }
        }

        for (String aa : bbs) {
            String keyword = aa ;
            List<NoticeMQ> mqEntities02 = contentSolr.companyResultsBaoXian( "yyyymmdd:[20200101 TO 20201231] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\""+aa+"\"", keyword, 101);
            log.info("keyword:{}查询出了size：{}条数据", keyword, mqEntities02.size());
            if (!mqEntities02.isEmpty()) {
                for (NoticeMQ data : mqEntities02) {
                    list1.add(data);
                    if (!dataMap.containsKey(data.getContentid().toString())) {
                        list.add(data);
                        data.setKeyword(keyword);
                        data.setKeywordTerm("设备类");
                        dataMap.put(data.getContentid().toString(), "0");
                    }
                }
            }
        }


        for (String aa : ccs) {
            String keyword = aa ;
            List<NoticeMQ> mqEntities02 = contentSolr.companyResultsBaoXian( "yyyymmdd:[20200101 TO 20201231] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\""+aa+"\"", keyword, 101);
            log.info("keyword:{}查询出了size：{}条数据", keyword, mqEntities02.size());
            if (!mqEntities02.isEmpty()) {
                for (NoticeMQ data : mqEntities02) {
                    list1.add(data);
                    if (!dataMap.containsKey(data.getContentid().toString())) {
                        list.add(data);
                        data.setKeyword(keyword);
                        data.setKeywordTerm("信息化");
                        dataMap.put(data.getContentid().toString(), "0");
                    }
                }
            }
        }


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
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

    String[] aas = {"MR ","CT ","XR ","DR"};

    String[] bbs = {  "直线加速器","正电子发射体层摄影装置","正电子发射体层摄影仪","正电子发射体层摄影设备","正电子发射体层摄影机","正电子发射计算机断层显像装置","正电子发射计算机断层显像仪","正电子发射计算机断层显像设备","正电子发射计算机断层显像机","医用磁共振装置","医用磁共振仪","医用磁共振设备","医用磁共振机","医用CT","血管造影装置","血管造影仪","血管造影设备","血管造影机","双源CT","双排CT","数字减影装置","数字减影仪","数字减影设备","数字减影机","能谱CT","螺旋CT","计算机体层摄影装置","计算机体层摄影仪","计算机体层摄影设备","计算机体层摄影机","计算机断层摄影装置","计算机断层摄影仪","计算机断层摄影设备","计算机断层摄影机","计算机断层扫描装置","计算机断层扫描仪","计算机断层扫描设备","计算机断层扫描机","计算机断层成像装置","计算机断层成像仪","计算机断层成像设备","计算机断层成像机","核磁共振装置","核磁共振仪","核磁共振设备","核磁共振谱机","高端CT","定位CT","大孔径CT","磁共振装置","磁共振影像装置","磁共振影像仪","磁共振影像设备","磁共振影像机","磁共振仪","磁共振项目","磁共振设备","磁共振机","磁共振成像装置","磁共振成像仪","磁共振成像设备","磁共振成像机","磁共振采购","宝石CT","X射线成像装置","X射线成像仪","X射线成像设备","X射线成像机","T核磁共振装置","T核磁共振仪","T核磁共振设备","T核磁共振机","T磁共振装置","T磁共振仪","T磁共振设备","T磁共振机","RevolutionCT","PET-CT装置","PETCT装置","PET-CT仪","PETCT仪","PET-CT设备","PETCT设备","PET-CT机","PETCT机","PET/CT装置","PET/CT仪","PET/CT设备","PET/CT机","MR装置","MR仪","MR设备","MR机","DSA装置","DSA仪","DSA设备","DSA机","CT装置","CT影像装置","CT影像仪","CT影像设备","CT影像机","CT仪","CT项目","CT探测仪","CT设备","CT扫描装置","CT扫描仪","CT扫描设备","CT扫描机","CT机","64排CT","320CT","256排CT","16排CT","128排CT","PET-CT","PETCT","PET/CT","乳腺机","乳腺X射线","数字乳腺X射线","C型臂","C臂","移动DR","移动X射线","62排CT","核医学","SPECT/CT", "智能医院", "智能医疗", "智能化医疗", "智慧医院", "智慧医疗", "治疗信息化", "治疗数字化", "治疗软件", "诊疗信息化", "诊疗数字化", "诊疗软件", "诊断信息化", "诊断数字化", "诊断软件", "云医院", "云医疗", "远程诊疗信息化", "远程诊疗数字化", "远程诊疗软件", "远程医疗信息化", "远程医疗数字化", "远程医疗软件", "远程信息化", "远程数字化", "远程软件", "预约信息化", "预约数字化", "预约软件", "影像医学信息化", "影像医学数字化", "影像医学软件", "影像信息化", "影像数字化", "影像软件", "移动影像信息化", "移动影像数字化", "移动影像软件", "移动车载CT信息化", "移动车载CT数字化", "移动车载CT软件", "医院智能化", "医院云信息化", "医院云数字化", "医院云软件", "医院影像信息化", "医院影像数字化", "医院影像软件", "医院信息化", "医学影像信息化", "医学影像数字化", "医学影像软件", "医学摄影信息化", "医学摄影数字化", "医学摄影软件", "医学成像信息化", "医学成像数字化", "医学成像软件", "医疗智能化", "医疗云", "医疗影像信息化", "医疗影像数字化", "医疗影像软件", "医疗信息化", "医疗人工智能", "医联体信息化", "医联体数字化", "医联体软件", "医共体信息化", "医共体数字化", "医共体软件", "信息平台建设", "信息平台搭建", "信息安全测评", "心电信息化", "心电数字化", "心电软件", "数字化医疗", "摄像信息化", "摄像数字化", "摄像软件", "人工智能医疗", "人工智能", "区域影像信息化", "区域影像数字化", "区域影像软件", "区域医疗信息化", "区域医疗数字化", "区域医疗软件", "内视镜信息化", "内视镜数字化", "内视镜软件", "内窥镜信息化", "内窥镜数字化", "内窥镜软件", "内镜信息化", "内镜数字化", "内镜软件", "叫号信息化", "叫号数字化", "叫号软件", "胶片信息化", "胶片数字化", "胶片软件", "会诊信息化", "会诊数字化", "会诊软件", "放射信息化", "放射数字化", "放射软件", "电子病历", "等级保护", "等保测评", "成像信息化", "成像数字化", "成像软件", "超声信息化", "超声数字化", "超声影像归档和通信系统", "病理信息化", "病理数字化", "病理软件", "内镜PACS", "PACS信息化", "PACS数字化", "PACS软件", "PACS系统", "预约系统", "叫号系统", "远程系统", "胶片系统", "会诊系统", "医联体系统", "医共体系统", "治疗信息平台", "诊疗信息平台", "诊断信息平台", "远程诊疗信息平台", "远程医疗信息平台", "远程信息平台", "预约信息平台", "影像医学信息平台", "影像信息平台", "移动影像信息平台", "移动车载CT信息平台", "医院智能化信息平台", "医院云信息平台", "医院影像信息平台", "医院信息平台", "医学影像信息平台", "医学摄影信息平台", "医学成像信息平台", "医疗智能化信息平台", "医疗云信息平台", "医疗影像信息平台", "医疗信息平台", "医联体信息平台", "医共体信息平台", "摄像信息平台", "区域影像信息平台", "区域医疗信息平台", "叫号信息平台", "胶片信息平台", "会诊信息平台", "成像信息平台", "病理信息平台", "PACS信息平台"};

    String[] ccs = {  "直线加速器","正电子发射体层摄影装置","正电子发射体层摄影仪","正电子发射体层摄影设备","正电子发射体层摄影机","正电子发射计算机断层显像装置","正电子发射计算机断层显像仪","正电子发射计算机断层显像设备","正电子发射计算机断层显像机","医用磁共振装置","医用磁共振仪","医用磁共振设备","医用磁共振机","医用CT","血管造影装置","血管造影仪","血管造影设备","血管造影机","双源CT","双排CT","数字减影装置","数字减影仪","数字减影设备","数字减影机","能谱CT","螺旋CT","计算机体层摄影装置","计算机体层摄影仪","计算机体层摄影设备","计算机体层摄影机","计算机断层摄影装置","计算机断层摄影仪","计算机断层摄影设备","计算机断层摄影机","计算机断层扫描装置","计算机断层扫描仪","计算机断层扫描设备","计算机断层扫描机","计算机断层成像装置","计算机断层成像仪","计算机断层成像设备","计算机断层成像机","核磁共振装置","核磁共振仪","核磁共振设备","核磁共振谱机","高端CT","定位CT","大孔径CT","磁共振装置","磁共振影像装置","磁共振影像仪","磁共振影像设备","磁共振影像机","磁共振仪","磁共振项目","磁共振设备","磁共振机","磁共振成像装置","磁共振成像仪","磁共振成像设备","磁共振成像机","磁共振采购","宝石CT","X射线成像装置","X射线成像仪","X射线成像设备","X射线成像机","T核磁共振装置","T核磁共振仪","T核磁共振设备","T核磁共振机","T磁共振装置","T磁共振仪","T磁共振设备","T磁共振机","RevolutionCT","PET-CT装置","PETCT装置","PET-CT仪","PETCT仪","PET-CT设备","PETCT设备","PET-CT机","PETCT机","PET/CT装置","PET/CT仪","PET/CT设备","PET/CT机","MR装置","MR仪","MR设备","MR机","DSA装置","DSA仪","DSA设备","DSA机","CT装置","CT影像装置","CT影像仪","CT影像设备","CT影像机","CT仪","CT项目","CT探测仪","CT设备","CT扫描装置","CT扫描仪","CT扫描设备","CT扫描机","CT机","64排CT","320CT","256排CT","16排CT","128排CT","PET-CT","PETCT","PET/CT","乳腺机","乳腺X射线","数字乳腺X射线","C型臂","C臂","移动DR","移动X射线","62排CT","核医学","SPECT/CT", "智能医院", "智能医疗", "智能化医疗", "智慧医院", "智慧医疗", "治疗信息化", "治疗数字化", "治疗软件", "诊疗信息化", "诊疗数字化", "诊疗软件", "诊断信息化", "诊断数字化", "诊断软件", "云医院", "云医疗", "远程诊疗信息化", "远程诊疗数字化", "远程诊疗软件", "远程医疗信息化", "远程医疗数字化", "远程医疗软件", "远程信息化", "远程数字化", "远程软件", "预约信息化", "预约数字化", "预约软件", "影像医学信息化", "影像医学数字化", "影像医学软件", "影像信息化", "影像数字化", "影像软件", "移动影像信息化", "移动影像数字化", "移动影像软件", "移动车载CT信息化", "移动车载CT数字化", "移动车载CT软件", "医院智能化", "医院云信息化", "医院云数字化", "医院云软件", "医院影像信息化", "医院影像数字化", "医院影像软件", "医院信息化", "医学影像信息化", "医学影像数字化", "医学影像软件", "医学摄影信息化", "医学摄影数字化", "医学摄影软件", "医学成像信息化", "医学成像数字化", "医学成像软件", "医疗智能化", "医疗云", "医疗影像信息化", "医疗影像数字化", "医疗影像软件", "医疗信息化", "医疗人工智能", "医联体信息化", "医联体数字化", "医联体软件", "医共体信息化", "医共体数字化", "医共体软件", "信息平台建设", "信息平台搭建", "信息安全测评", "心电信息化", "心电数字化", "心电软件", "数字化医疗", "摄像信息化", "摄像数字化", "摄像软件", "人工智能医疗", "人工智能", "区域影像信息化", "区域影像数字化", "区域影像软件", "区域医疗信息化", "区域医疗数字化", "区域医疗软件", "内视镜信息化", "内视镜数字化", "内视镜软件", "内窥镜信息化", "内窥镜数字化", "内窥镜软件", "内镜信息化", "内镜数字化", "内镜软件", "叫号信息化", "叫号数字化", "叫号软件", "胶片信息化", "胶片数字化", "胶片软件", "会诊信息化", "会诊数字化", "会诊软件", "放射信息化", "放射数字化", "放射软件", "电子病历", "等级保护", "等保测评", "成像信息化", "成像数字化", "成像软件", "超声信息化", "超声数字化", "超声影像归档和通信系统", "病理信息化", "病理数字化", "病理软件", "内镜PACS", "PACS信息化", "PACS数字化", "PACS软件", "PACS系统", "预约系统", "叫号系统", "远程系统", "胶片系统", "会诊系统", "医联体系统", "医共体系统", "治疗信息平台", "诊疗信息平台", "诊断信息平台", "远程诊疗信息平台", "远程医疗信息平台", "远程信息平台", "预约信息平台", "影像医学信息平台", "影像信息平台", "移动影像信息平台", "移动车载CT信息平台", "医院智能化信息平台", "医院云信息平台", "医院影像信息平台", "医院信息平台", "医学影像信息平台", "医学摄影信息平台", "医学成像信息平台", "医疗智能化信息平台", "医疗云信息平台", "医疗影像信息平台", "医疗信息平台", "医联体信息平台", "医共体信息平台", "摄像信息平台", "区域影像信息平台", "区域医疗信息平台", "叫号信息平台", "胶片信息平台", "会诊信息平台", "成像信息平台", "病理信息平台", "PACS信息平台"};

    private void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {
        String zhaoBiaoUnit = noticeMQ.getZhaoBiaoUnit();
        if (StringUtils.isBlank(zhaoBiaoUnit)){
            return;
        }
        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            resultMap.put("keyword", "");
            String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";
            String zhongUnit = resultMap.get("zhao_biao_unit") != null ? resultMap.get("zhao_biao_unit").toString() : "";
            String industry = myRuleUtils.getIndustry(zhongUnit);
            // 校验行业信息
            if ("政府机构-医疗".equals(industry) || "医疗单位-血站".equals(industry) || "医疗单位-急救中心".equals(industry) ||"医疗单位-医疗服务".equals(industry) ||
                    "医疗单位-疾控中心".equals(industry) || "医疗单位-卫生院".equals(industry) || "医疗单位-疗养院".equals(industry) || "医疗单位-专科医院".equals(industry) ||
                    "医疗单位-中医院".equals(industry) || "医疗单位-综合医院".equals(industry) || "商业公司-医疗服务".equals(industry)){

                resultMap.put("code", industry);
                resultMap.put("keyword_term", noticeMQ.getKeywordTerm());
                // 获取正文字段
                List<Map<String, Object>> contentList = gwJdbcTemplate.queryForList(ConstantBean.SELECT_ITEM_CONTENT_BY_CONTENTID, noticeMQ.getContentid().toString());
                if (contentList == null && contentList.size() == 0){
                    return;
                }
                String content = contentList.get(0).get("content").toString();//正文字段
                String tempContent = content;
                content = MathUtil.delHTMLTag(content);//去除正文中的html
                content = title + "&" + content;
                content = content.toUpperCase();//字符转大写
                title = title.toUpperCase();//标题字符转大写

                String keyword = "";
                String keywords = "";

                for (String aa : aas) {
                    if (title.contains(aa.toUpperCase())){
                        keyword += aa + ConstantBean.RULE_SEPARATOR_NAME;
                    }
                }
                for (String bb : bbs) {
                    if (content.contains(bb.toUpperCase())){
                        keyword += bb + ConstantBean.RULE_SEPARATOR_NAME;
                    }
                }
                keyword = StringUtils.isNotBlank(keyword) ? keyword.substring(0, keyword.length() -1) : "";
                if (StringUtils.isBlank(keyword)){
                    keyword = noticeMQ.getKeyword();
                }

                HashMap<String, String> simpleAreaMap = KeyUtils.getSimpleAreaMap();//读取医院信息
                Set<Map.Entry<String, String>> entries = simpleAreaMap.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    String key = entry.getKey();
                    if (zhongUnit.contains(key)){
                        keywords = entry.getValue();
                        break;
                    }
                }
                
                resultMap.put("keyword", keyword);
                resultMap.put("keywords", keywords);
                // 标的物进行匹配
                String progid = resultMap.get("progid").toString();
                Integer taskId = 0;
                if ("0".equals(progid) || "1".equals(progid) || "2".equals(progid)){
                    taskId = 1;
                } else {
                    taskId = 2;
                }
                resultMap.put("task_id", taskId);
                getBiaoDiWuForAll(tempContent, String.valueOf(noticeMQ.getContentid()), taskId);
                cusDataFieldService.saveIntoMysql(resultMap);
            }
        }
    }




    public void getBiaoDiWuForAll(String content, String contentId, Integer taskId){
        if (StringUtils.isBlank(content) && StringUtils.isBlank(contentId)){
            return;
        }
        try {
            //标的物解析表---对象（进行解析）
            String target = TargetExtractService.getTargetResult("http://47.104.4.12:5001/to_json_v3/", content);
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
                        String keyword = "";
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

                        String result = name + "&" + brand + "&" + model;
                        result = result.toUpperCase();
                        for (String cc : ccs) {
                            if (result.contains(cc.toUpperCase())){
                                keyword += cc + ConstantBean.RULE_SEPARATOR_NAME;
                            }
                        }
                        keyword = StringUtils.isNotBlank(keyword) ? keyword.substring(0, keyword.length() -1) : "";

                        bdJdbcTemplate.update(UPDATA_SQL_01, contentId, keyword, taskId, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit);
                    }
                }
            }
        } catch (Exception e){
            log.info("infoId:{} 获取标的物信息异常", contentId);
        }
    }

}
