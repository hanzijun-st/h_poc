package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.CusDataNewService;
import com.qianlima.offline.service.han.TestBeiDengService;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.OnlineContentSolr;
import com.qianlima.offline.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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
public class TestBeiDengServiceImpl implements TestBeiDengService {

    @Autowired
    private OnlineContentSolr onlineContentSolr;

    @Autowired
    private CurrencyService currencyService;//为了获取 progid

    @Autowired
    private CusDataNewService cusDataNewService;//调用中台接口

    @Autowired
    @Qualifier("djeJdbcTemplate")
    private JdbcTemplate djeJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Override
    public void getBeiDeng4(Integer type, String date, String progidStr) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词 全文
        List<String> qw = LogUtils.readRule("bdA");

        //关键词 全文+辅助
        //List<String> qwAndFz = LogUtils.readRule("bdAndFz");

        //b词
        //String[] b ={"医院","诊所","门诊","保健院","健康委员会","医学院","体检中心","健康局","医院部","药房","卫生院","医疗保障局","合作医疗","医药服务管理司","兽医实验室","医药","精神病院","防治院","血液中心","眼科中心","治疗中心","保健中心","保健所","血管病研究所","防治所","外科中心","康复中心","透析中心","正畸中心","荣军院","防治中心","保健站","列腺病研究所","职业病院","防治站","产院","急救中心","卫生局","卫生厅","防治办公室","卫生保健中心","医疗中心","卫生中心","门诊部","卫生服务站","医检所","制剂室","药交所","眼科","医保","医疗保障","卫健委","戒毒所","敬老院","疗养院","眼病防治所","矫治所","结核病防治所","休养所","血站","福利院","医疗机构","病防治办公室","计划生育","生育委员","计生委","大健康","同仁堂","江中集团","医学","健康科技","养生堂","保健品","诊断","康宁","制药","药业","药集团","医疗集团","精神卫生","药店","军医","医用","医疗","诊疗","残联","医护","卫生所","卫生院 ","卫生院校","医科大学","妇幼","健康中心","运动康复","中医馆","预防控制","医务室"};

        //黑词
        //String[] hc={"车辆维修","人才招聘","保温材料更换","物业管理服务","空调管路系统","安保服务","空调维保","污水处理系统","改造安装防护门","热水系统改造","排风系统升级改造","空调系统改造","采购安装风管机空调","消防维保","绿化带拆除","电梯维保服务","中央空调清洗","消防维修","地下车库加建","食堂对外承包","保护测评","保洁服务","监理单位","监理企业","招租","设施改造","房租","出租","选择招标代理机构","水杯维修","食堂外包服务","后厨管理承包","食堂承包经营","食堂等物业服务","后勤保洁服务","食堂项目承包","肉类配送","保安服务","车转让","变压器扩容","网络招聘服务","保险联网结算系统","宣传片投放","广告服务","工程垃圾设备","景观节点整治","塌方除理","房屋拍卖","汽车采购","工程造价咨询","整体板房询价","租赁服务","垃圾清运","外墙保温","康复大楼工程监理","宣传策划","车辆租赁","办公系统开发","水体治理","审计业务","养老购买","坑塘整治","后勤保洁管理服务","设备维修维护保养","冷水机组主机设备及末端设备采购","路灯采购","奶粉采购","采购家具","空调采购","多联机空调","锅炉房设备采购","电视机采购","采购电视机","环卫工具采购","印刷采购","加装电梯","被服采购","家具采购","石材采购","停车设备采购","电梯采购","垃圾压缩成套设备","窗帘采购","混凝土招标","数控机床附件","监理","工程监理","施工监理","广告宣传","临建食堂购餐桌椅","食堂食材采购","食堂食品","员工工装","热机组采购","竹地板材料","有限公司轮胎","保险采购","苗木采购","鱼苗采购","多联机配件","污水处理设备","白色OPPOR9手机","货物类采购","水分配系统","采购日常百货","石材招标","玻璃隔断","玻璃栏杆","医院勘察采购","防褥疮床垫","清洁能源示范","铅桶采购","笔记本电脑采购","空调设备招标","废标","流标","终止","违规","招标异常","无效公告","暂停公告","失败公告","终止公告"};

        for (String str : qw) {
            futureList1.add(executorService1.submit(() -> {
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
            }));
        }


       /* for (String str : qwAndFz) {
            for (String str2 : b) {
                futureList1.add(executorService1.submit(() -> {
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
                }));
            }
        }
        for (String str : qwAndFz) {
            for (String str2 : b) {
                futureList1.add(executorService1.submit(() -> {
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
                }));
            }
        }*/

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

        //关键词全文
        for (String a :qw){
            arrayList.add(a);
        }

       /* for (String key : qwAndFz) {
            for (String k : b){
                arrayList.add(key+"&"+k);
            }
        }*/

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
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
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
    public void getBeideng4_2(Integer type, String date, String progidStr) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词 全文
        //List<String> qw = LogUtils.readRule("bdA");

        //关键词 全文+辅助
        List<String> qwAndFz = LogUtils.readRule("bdAndFz");

        //b词
        String[] b ={"医院","诊所","门诊","保健院","健康委员会","医学院","体检中心","健康局","医院部","药房","卫生院","医疗保障局","合作医疗","医药服务管理司","兽医实验室","医药","精神病院","防治院","血液中心","眼科中心","治疗中心","保健中心","保健所","血管病研究所","防治所","外科中心","康复中心","透析中心","正畸中心","荣军院","防治中心","保健站","列腺病研究所","职业病院","防治站","产院","急救中心","卫生局","卫生厅","防治办公室","卫生保健中心","医疗中心","卫生中心","门诊部","卫生服务站","医检所","制剂室","药交所","眼科","医保","医疗保障","卫健委","戒毒所","敬老院","疗养院","眼病防治所","矫治所","结核病防治所","休养所","血站","福利院","医疗机构","病防治办公室","计划生育","生育委员","计生委","大健康","同仁堂","江中集团","医学","健康科技","养生堂","保健品","诊断","康宁","制药","药业","药集团","医疗集团","精神卫生","药店","军医","医用","医疗","诊疗","残联","医护","卫生所","卫生院 ","卫生院校","医科大学","妇幼","健康中心","运动康复","中医馆","预防控制","医务室"};

        //黑词
        //String[] hc={"车辆维修","人才招聘","保温材料更换","物业管理服务","空调管路系统","安保服务","空调维保","污水处理系统","改造安装防护门","热水系统改造","排风系统升级改造","空调系统改造","采购安装风管机空调","消防维保","绿化带拆除","电梯维保服务","中央空调清洗","消防维修","地下车库加建","食堂对外承包","保护测评","保洁服务","监理单位","监理企业","招租","设施改造","房租","出租","选择招标代理机构","水杯维修","食堂外包服务","后厨管理承包","食堂承包经营","食堂等物业服务","后勤保洁服务","食堂项目承包","肉类配送","保安服务","车转让","变压器扩容","网络招聘服务","保险联网结算系统","宣传片投放","广告服务","工程垃圾设备","景观节点整治","塌方除理","房屋拍卖","汽车采购","工程造价咨询","整体板房询价","租赁服务","垃圾清运","外墙保温","康复大楼工程监理","宣传策划","车辆租赁","办公系统开发","水体治理","审计业务","养老购买","坑塘整治","后勤保洁管理服务","设备维修维护保养","冷水机组主机设备及末端设备采购","路灯采购","奶粉采购","采购家具","空调采购","多联机空调","锅炉房设备采购","电视机采购","采购电视机","环卫工具采购","印刷采购","加装电梯","被服采购","家具采购","石材采购","停车设备采购","电梯采购","垃圾压缩成套设备","窗帘采购","混凝土招标","数控机床附件","监理","工程监理","施工监理","广告宣传","临建食堂购餐桌椅","食堂食材采购","食堂食品","员工工装","热机组采购","竹地板材料","有限公司轮胎","保险采购","苗木采购","鱼苗采购","多联机配件","污水处理设备","白色OPPOR9手机","货物类采购","水分配系统","采购日常百货","石材招标","玻璃隔断","玻璃栏杆","医院勘察采购","防褥疮床垫","清洁能源示范","铅桶采购","笔记本电脑采购","空调设备招标","废标","流标","终止","违规","招标异常","无效公告","暂停公告","失败公告","终止公告"};

        for (String str : qwAndFz) {
            for (String str2 : b) {
                futureList1.add(executorService1.submit(() -> {
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
                }));
            }
        }
        for (String str : qwAndFz) {
            for (String str2 : b) {
                futureList1.add(executorService1.submit(() -> {
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

        //关键词全文
        for (String key : qwAndFz) {
            for (String k : b){
                arrayList.add(key+"&"+k);
            }
        }

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
                ExecutorService executorService = Executors.newFixedThreadPool(30);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave2(content)));
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
    public void getBeideng3(Integer type, String date, String progidStr) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词 全文
        List<String> qw = LogUtils.readRule("bdA");

        //关键词 全文+辅助
        List<String> qwAndFz = LogUtils.readRule("bdAndFz");

        //b词
        String[] b ={"医院","诊所","门诊","保健院","健康委员会","医学院","体检中心","健康局","医院部","药房","卫生院","医疗保障局","合作医疗","医药服务管理司","兽医实验室","医药","精神病院","防治院","血液中心","眼科中心","治疗中心","保健中心","保健所","血管病研究所","防治所","外科中心","康复中心","透析中心","正畸中心","荣军院","防治中心","保健站","列腺病研究所","职业病院","防治站","产院","急救中心","卫生局","卫生厅","防治办公室","卫生保健中心","医疗中心","卫生中心","门诊部","卫生服务站","医检所","制剂室","药交所","眼科","医保","医疗保障","卫健委","戒毒所","敬老院","疗养院","眼病防治所","矫治所","结核病防治所","休养所","血站","福利院","医疗机构","病防治办公室","计划生育","生育委员","计生委","大健康","同仁堂","江中集团","医学","健康科技","养生堂","保健品","诊断","康宁","制药","药业","药集团","医疗集团","精神卫生","药店","军医","医用","医疗","诊疗","残联","医护","卫生所","卫生院 ","卫生院校","医科大学","妇幼","健康中心","运动康复","中医馆","预防控制","医务室"};

        //黑词
        //String[] hc={"车辆维修","人才招聘","保温材料更换","物业管理服务","空调管路系统","安保服务","空调维保","污水处理系统","改造安装防护门","热水系统改造","排风系统升级改造","空调系统改造","采购安装风管机空调","消防维保","绿化带拆除","电梯维保服务","中央空调清洗","消防维修","地下车库加建","食堂对外承包","保护测评","保洁服务","监理单位","监理企业","招租","设施改造","房租","出租","选择招标代理机构","水杯维修","食堂外包服务","后厨管理承包","食堂承包经营","食堂等物业服务","后勤保洁服务","食堂项目承包","肉类配送","保安服务","车转让","变压器扩容","网络招聘服务","保险联网结算系统","宣传片投放","广告服务","工程垃圾设备","景观节点整治","塌方除理","房屋拍卖","汽车采购","工程造价咨询","整体板房询价","租赁服务","垃圾清运","外墙保温","康复大楼工程监理","宣传策划","车辆租赁","办公系统开发","水体治理","审计业务","养老购买","坑塘整治","后勤保洁管理服务","设备维修维护保养","冷水机组主机设备及末端设备采购","路灯采购","奶粉采购","采购家具","空调采购","多联机空调","锅炉房设备采购","电视机采购","采购电视机","环卫工具采购","印刷采购","加装电梯","被服采购","家具采购","石材采购","停车设备采购","电梯采购","垃圾压缩成套设备","窗帘采购","混凝土招标","数控机床附件","监理","工程监理","施工监理","广告宣传","临建食堂购餐桌椅","食堂食材采购","食堂食品","员工工装","热机组采购","竹地板材料","有限公司轮胎","保险采购","苗木采购","鱼苗采购","多联机配件","污水处理设备","白色OPPOR9手机","货物类采购","水分配系统","采购日常百货","石材招标","玻璃隔断","玻璃栏杆","医院勘察采购","防褥疮床垫","清洁能源示范","铅桶采购","笔记本电脑采购","空调设备招标","废标","流标","终止","违规","招标异常","无效公告","暂停公告","失败公告","终止公告"};


        for (String str : qw) {
            futureList1.add(executorService1.submit(() -> {
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
            }));
        }


        for (String str : qwAndFz) {
            for (String str2 : b) {
                futureList1.add(executorService1.submit(() -> {
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
                }));
            }
        }
        for (String str : qwAndFz) {
            for (String str2 : b) {
                futureList1.add(executorService1.submit(() -> {
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
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave3(content)));
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
    public void getBeideng4_1(Integer type, String date, String progidStr) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词 全文
        List<String> qw = LogUtils.readRule("bdA");

        //关键词 全文+辅助
        List<String> qwAndFz = LogUtils.readRule("bdAndFz");

        //b词
        String[] b ={"医院","诊所","门诊","保健院","健康委员会","医学院","体检中心","健康局","医院部","药房","卫生院","医疗保障局","合作医疗","医药服务管理司","兽医实验室","医药","精神病院","防治院","血液中心","眼科中心","治疗中心","保健中心","保健所","血管病研究所","防治所","外科中心","康复中心","透析中心","正畸中心","荣军院","防治中心","保健站","列腺病研究所","职业病院","防治站","产院","急救中心","卫生局","卫生厅","防治办公室","卫生保健中心","医疗中心","卫生中心","门诊部","卫生服务站","医检所","制剂室","药交所","眼科","医保","医疗保障","卫健委","戒毒所","敬老院","疗养院","眼病防治所","矫治所","结核病防治所","休养所","血站","福利院","医疗机构","病防治办公室","计划生育","生育委员","计生委","大健康","同仁堂","江中集团","医学","健康科技","养生堂","保健品","诊断","康宁","制药","药业","药集团","医疗集团","精神卫生","药店","军医","医用","医疗","诊疗","残联","医护","卫生所","卫生院 ","卫生院校","医科大学","妇幼","健康中心","运动康复","中医馆","预防控制","医务室"};

        //黑词
        String[] blacks={"车辆维修","人才招聘","保温材料更换","物业管理服务","空调管路系统","安保服务","空调维保","污水处理系统","改造安装防护门","热水系统改造","排风系统升级改造","空调系统改造","采购安装风管机空调","消防维保","绿化带拆除","电梯维保服务","中央空调清洗","消防维修","地下车库加建","食堂对外承包","保护测评","保洁服务","监理单位","监理企业","招租","设施改造","房租","出租","选择招标代理机构","水杯维修","食堂外包服务","后厨管理承包","食堂承包经营","食堂等物业服务","后勤保洁服务","食堂项目承包","肉类配送","保安服务","车转让","变压器扩容","网络招聘服务","保险联网结算系统","宣传片投放","广告服务","工程垃圾设备","景观节点整治","塌方除理","房屋拍卖","汽车采购","工程造价咨询","整体板房询价","租赁服务","垃圾清运","外墙保温","康复大楼工程监理","宣传策划","车辆租赁","办公系统开发","水体治理","审计业务","养老购买","坑塘整治","后勤保洁管理服务","设备维修维护保养","冷水机组主机设备及末端设备采购","路灯采购","奶粉采购","采购家具","空调采购","多联机空调","锅炉房设备采购","电视机采购","采购电视机","环卫工具采购","印刷采购","加装电梯","被服采购","家具采购","石材采购","停车设备采购","电梯采购","垃圾压缩成套设备","窗帘采购","混凝土招标","数控机床附件","监理","工程监理","施工监理","广告宣传","临建食堂购餐桌椅","食堂食材采购","食堂食品","员工工装","热机组采购","竹地板材料","有限公司轮胎","保险采购","苗木采购","鱼苗采购","多联机配件","污水处理设备","白色OPPOR9手机","货物类采购","水分配系统","采购日常百货","石材招标","玻璃隔断","玻璃栏杆","医院勘察采购","防褥疮床垫","清洁能源示范","铅桶采购","笔记本电脑采购","空调设备招标","废标","流标","终止","违规","招标异常","无效公告","暂停公告","失败公告","终止公告"};


        for (String str : qw) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"", str, 2);
                log.info(str.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            String heici ="";
                            for (String black : blacks) {
                                if (data.getTitle().contains(black)){
                                    heici +=black+"、";
                                }
                            }
                            if (StrUtil.isNotEmpty(heici)){
                                data.setKeywordTerm(heici.substring(0, heici.length() - 1));
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


        for (String str : qwAndFz) {
            for (String str2 : b) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"  AND zhaoBiaoUnit:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                String heici ="";
                                for (String black : blacks) {
                                    if (data.getTitle().contains(black)){
                                        heici +=black+"、";
                                    }
                                }
                                if (StrUtil.isNotEmpty(heici)){
                                    data.setKeywordTerm(heici.substring(0, heici.length() - 1));
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
        for (String str : qwAndFz) {
            for (String str2 : b) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                String heici ="";
                                for (String black : blacks) {
                                    if (data.getTitle().contains(black)){
                                        heici +=black+"、";
                                    }
                                }
                                if (StrUtil.isNotEmpty(heici)){
                                    data.setKeywordTerm(heici.substring(0, heici.length() - 1));
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

   /*     ArrayList<String> arrayList = new ArrayList<>();

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
        }*/
        System.out.println("全部数据量：" + listAll.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                String[] keywords = {"钙电极","钾电极","锂电极","氯电极","钠电极","氧电极","电极膜","酶免仪","酶标仪","比浊仪","浊度计","尿糖计","电泳仪","色谱柱","质谱仪","层析柱","电泳槽","采血笔","采血管","隐血珠","采血针","切片机","染色机","包埋机","制片机","涂片机","裂解仪","离心机","恒温箱","孵育器","洗板机","计数板","血沉管","血流变仪","血库系统","钾分析仪","参比电极","乳酸电极","钙分析仪","氯分析仪","钠分析仪","电泳仪器","质谱系统","电泳装置","缓冲液槽","采样设备","采样器具","采样拭子","培养设备","孵育设备","超净装置","血型分析仪","半自动血栓","血凝分析仪","血糖分析仪","血液粘度计","血栓弹力仪","凝血分析仪","功能分析仪","血沉分析仪","流式细胞仪","生化分析仪","肌酐分析仪","血糖血酮仪","血脂分析仪","乳酸分析仪","尿酸分析仪","血气分析仪","血气采血器","血氧分析仪","选择性电极","葡萄糖电极","pH分析仪","尿素电极盒","乳酸电极盒","免疫印迹仪","金标测试仪","蛋白印迹仪","蛋白分析仪","抗体检测仪","浊度分析仪","蛋白检测仪","酶免分析仪","酶标分析仪","荧光阅读仪","光法分析仪","免疫分析仪","精子分析仪","基因测序仪","检测分析仪","基因扩增仪","恒温杂交仪","电子比浊仪","药敏分析仪","细菌培养仪","医用显微镜","尿液分析仪","成分分析仪","粪便分析仪","体液分析仪","白带分析仪","流式点阵仪","色谱分析仪","蛋白层析柱","信号扩大仪","电泳凝胶板","动脉血气针","末梢采血针","末梢采血器","真空采血管","血液采集卡","微生物拭子","病毒采样盒","采样储藏管","病毒采样管",
                        "激光采血仪","足跟采血器","激光采血机","整体切片机","组织脱水机","组织处理机","推片染色机","冷冻切片机","细胞制片机","包埋机热台","包埋机冷台","自动涂片机","特殊染色机","滴染染色机","抗原修复仪","细胞过滤器","裂解洗脱仪","样本裂解仪","医用离心机","超速离心机","核酸提取仪","核酸纯化仪","恒温培养箱","生化培养箱","医用培养箱","恒温保存箱","厌氧培养箱","振荡孵育器","恒温箱系统","酶标洗板机","微孔洗板机","细胞计数板","医用冷藏箱","医用冷冻箱","细胞分选仪","血球记数板","医用低温箱","生物安全柜","洁净工作台","血型分析仪器","凝血分析仪器","血型卡离心机","全自动涂片机","自动血库系统","血小板聚集仪","红细胞变形仪","血液分析系统","血细胞分析仪","血细胞计数器","白细胞计数仪","二聚体分析仪","ACT监测仪","血栓弹力图仪","血小板分析仪","血小板凝集仪","血流变分析仪","生化分析设备","生化分析仪器","胆红素分析仪","血酮体测试仪","葡萄糖分析仪","电解质分析仪","胆固醇分析仪","氧含量测定仪","电化学测氧仪","血气分析系统","血气分析仪器","血气检测电极","血气分析设备","二氧化碳电极","葡萄糖电极盒","免疫分析设备","荧光判读系统","免疫分析系统","临床检验系统","PCR扩增仪","基因测序系统","PCR分析仪","基因测序仪器","核酸扩增仪器","微生物比浊仪","菌培养监测仪","微生物鉴定仪","细菌测定系统","细菌分析系统","图像扫描仪器","图像分析仪器","尿液分析设备","样本分析设备","尿液分析仪器","成分分析仪器","尿液分析系统","粪便分析仪器","精子分析仪器","体液分析仪器","便潜血分析仪","尿液分析试纸","流式点阵仪器","质谱检测系统","冰点渗透压计","检测阅读系统","毛细管电泳仪","琼脂糖电泳仪","非真空采血管","血样采集容器","隐血采样胶囊","样本采样拭子","集菌培养容器","血液化验设备","血液化验器具","红白血球吸管","动静脉采血针","轮转式切片机","平推式切片机","振动式切片机","抗原热修复仪","玻片处理系统","样本处理仪器","样本分离设备","微孔板离心机","CO2培养箱","厌氧培养装置","厌氧培养系统","血小板振荡器","检验辅助设备","自动加样系统","低温储存设备","样本处理系统","去血片洗板机","血细胞计数板","尿沉渣计数板","样品处理系统","分杯处理系统","自动进样系统","脏器冷藏装置","医用低温设备","医用冷藏设备","医用冷冻设备","血液学分析设备","血细胞分析仪器","血小板分析仪器","血流变分析仪器","红细胞沉降仪器","血红蛋白测定仪","流式细胞分析仪","干式血球计数仪","即时凝血分析仪","凝血速率监测仪","自动凝血计时器","凝血功能分析仪","血液流变分析仪","动态血沉分析仪","淋巴细胞计数仪","血红蛋白分析仪","血糖血压测试仪","血糖两用检测仪","血糖乳酸分析仪","血气酸碱分析仪","电解质分析仪器","电解质分析设备","血液血气分析仪","血气生化分析仪","红细胞压积电极","代谢物测量系统","酶联免疫分析仪","荧光免疫分析仪","免疫层析分析仪",
                        "免疫分析一体机","化学发光测定仪","早孕试纸阅读仪","散射比浊分析仪","化学比浊测定仪","生化免疫分析仪","排卵试纸阅读仪","生物学分析设备","生物芯片阅读仪","PCR分析系统","恒温核酸扩增仪","核酸分子杂交仪","微生物分析设备","呼气试验测试仪","结核杆菌分析仪","微生物比浊仪器","微生物鉴定仪器","倒置生物显微镜","正置生物显微镜","数码生物显微镜","光学生物显微镜","荧光生物显微镜","病理切片扫描仪","显微镜扫描系统","显微影像分析仪","放射免疫测定仪","放射免疫分析仪","放射免疫计数器","液体闪烁计数器","分泌物分析仪器","粪便常规分析仪","精子质量分析仪","精子采集分析仪","自动尿液分析仪","生殖道分析仪器","渗透压测定仪器","微量元素分析仪","血液铅镉分析仪","生物芯片反应仪","基因芯片阅读仪","血样采集连接头","真空静脉采血管","微量无菌采血管","标本采集保存管","病变细胞采集器","静脉血样采血管","末梢血采集容器","胃隐血采集器具","微量血液搅拌器","微量血液振荡器","细胞离心涂片机","免疫组化染色机","核酸提取纯化仪","细胞洗涤离心机","血型专用离心机","超净恒温培养箱","二氧化碳培养箱","医用血液冷藏箱","医用血浆速冻机","医用冷藏冷冻箱","样品前处理系统","样品后处理系统",
                        "低温生物降温仪","血液制品冷藏箱","冷冻干燥血浆机","真空冷冻干燥箱","流式细胞分析仪器","全自动血型分析仪","血液体液分析系统","T淋巴细胞计数仪","血细胞形态分析仪","细胞形态学分析仪","纤溶多功能分析仪","半自动凝血分析仪","全自动凝血分析仪","血小板功能分析仪","红细胞沉降压积仪","血糖参数分析仪器","血糖与血脂监测仪","多项电解质分析仪","胆固醇两用检测仪","胆固醇乳酸分析仪","血氧饱和度测试仪","CO2红外分析仪","电解质血气分析仪","电解质检测电极块","电解质生化分析仪","生化分析仪用电极","生化免疫分析仪器","心脏标志物检测仪","荧光显微检测系统","核酸检测分析系统","实时定量PCR仪","基因扩增热循环仪","核酸扩增分析仪器","核酸分子杂交仪器","微生物培养监测仪","微生物质谱鉴定仪","微生物药敏分析仪","细菌内毒素检测仪","碳13呼气质谱仪","碳13红外光谱仪","碳13呼气分析仪","真菌葡聚糖检测仪","扫描图像分析系统","超倍生物显微系统","LED生物显微镜","核素标本测定装置","放射免疫γ计数器","放射性层析扫描仪","阴道分泌物检测仪","体液形态学分析仪","微量元素分析仪器","液相色谱分析仪器","生物芯片分析仪器","血液五元素分析仪","三重四极杆质谱仪","冰点渗透压测定仪","胶体渗透压测定仪","微阵列芯片检测仪","微阵列芯片扫描仪","动静脉血样采集针","真空动静脉采血针","真空动静脉采血器","静脉血样采集容器","医用超低温冷冻箱","医用液氮储存系统","粪便分析前处理仪","医用生物防护设备","一次性使用采样器","一次性使用取样器","血细胞形态分析仪器","全自动血细胞分析仪","半自动血细胞分析仪","血液流变参数测试仪","血细胞形态学分析仪","凝血酶原时间检测仪","活化凝血时间分析仪","活化凝血时间监测仪","血液流变动态分析仪","动态血沉压积测试仪","红细胞沉降率测定仪","氧自由基生化分析仪","尿微量白蛋白分析仪","甘油三酯乳酸分析仪","经皮血氧分压监测仪","半自动电解质分析仪","电解质分析仪用电极","化学发光免疫分析仪","免疫散射浊度分析仪","金标免疫层析分析仪","免疫层析试条检测仪","医用PCR分析系统","sanger测序仪","恒温核酸扩增分析仪","基因测序文库制备仪","幽门螺旋杆菌检测仪","幽门螺旋杆菌测定仪","微生物培养监测仪器","微生物质谱鉴定仪器","真菌葡聚糖检测仪器","放射性层析扫描装置","体液形态学分析仪器","精子自动检测分析仪","生殖道分泌物分析仪","医用原子吸收光谱仪","循环肿瘤细胞分析仪","压电蛋白芯片分析仪","琼脂糖凝胶电泳装置","样品检查自动化系统","样本处理及孵育系统","粪便标本采集保存管","全自动凝血纤溶分析仪","全自动凝血因子分析仪","新生儿总胆红素测定仪","血红蛋白干化学分析仪","间接免疫荧光分析仪器","电化学发光免疫分析仪","金标斑点法定量读数仪","血液微生物培养监测仪","微生物药敏培养监测仪","微生物鉴定药敏分析仪","幽门螺旋杆菌分析仪器","玻片扫描分析影像系统","细胞医学图像分析系统","医学显微图像分析系统","循环肿瘤细胞分析仪器","真空静脉血样采集容器","动静脉采血针及连接件","微生物样本前处理系统","全自动血栓止血分析系统","全自动配血及血型分析仪","血型分析用凝胶卡判读仪","时间分辨免疫荧光分析仪","微生物药敏培养监测仪器","微生物鉴定药敏分析仪器","染色体显微图像扫描系统","放射性核素标本测定装置","流式点阵发光免疫分析仪","生物免疫层析芯片检测仪","医用开放式血液冷藏周转箱","自动扫描显微镜和图像分析系统","低温冰箱","超低温冰箱","免疫荧光分析仪","氦质谱检漏仪","冷冻箱","医用冰箱","脱水机","酶联免疫检测仪"};

                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave4(content,keywords)));
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

    /**
     * 调用中台数据，进行处理
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
            saveIntoMysql(resultMap);
        }
    }
    /**
     * 调用中台数据，进行处理-规则二
     */
    private void getZhongTaiDatasAndSave2(NoticeMQ noticeMQ) {

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            saveIntoMysql2(resultMap);
        }
    }
    /**
     * 调用中台数据，进行处理-规则三
     */
    private void getZhongTaiDatasAndSave3(NoticeMQ noticeMQ) {

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            saveIntoMysql3(resultMap);
        }
    }
    /**
     * 调用中台数据，进行处理-规则四
     */
    private void getZhongTaiDatasAndSave4(NoticeMQ noticeMQ,String[] keywords) {

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);

        if (resultMap != null) {
            String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
            String title = resultMap.get("title").toString();//标题

            content = content+"&" +title;
            // 进行匹配关键词操作
            if (keywords != null && keywords.length > 0){
                String keyword = "";
                for (String aa : keywords) {
                    if (content.contains(aa)){
                        keyword += (aa + ",");
                    }
                }
                if (StringUtils.isNotBlank(keyword)) {
                    keyword = keyword.substring(0, keyword.length() - 1);
                    resultMap.put("keyword", keyword);
                }
            }
            saveIntoMysql4(resultMap);
        }
    }

    // 数据入库操作
    public static final String INSERT_ZT_RESULT_YILIAO = "INSERT INTO han_new_data_bd1 (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time," +
            "is_electronic,code,isfile,keyword_term,keywords, infoTypeSegment,monitorUrl, pocDetailUrl) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    // 数据入库操作-规则二
    public static final String INSERT_ZT_RESULT_YILIAO2 = "INSERT INTO han_new_data_bd2 (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time," +
            "is_electronic,code,isfile,keyword_term,keywords, infoTypeSegment,monitorUrl, pocDetailUrl) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    // 数据入库操作-规则三
    public static final String INSERT_ZT_RESULT_YILIAO3 = "INSERT INTO han_new_data_bd3 (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time," +
            "is_electronic,code,isfile,keyword_term,keywords, infoTypeSegment,monitorUrl, pocDetailUrl) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    // 数据入库操作-规则四
    public static final String INSERT_ZT_RESULT_YILIAO4 = "INSERT INTO han_new_data_bd4 (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time," +
            "is_electronic,code,isfile,keyword_term,keywords, infoTypeSegment,monitorUrl, pocDetailUrl) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public void saveIntoMysql(Map<String, Object> map){
        bdJdbcTemplate.update(INSERT_ZT_RESULT_YILIAO,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"),map.get("keywords"),map.get("infoTypeSegment"),map.get("monitorUrl"),map.get("pocDetailUrl"));
    }
    public void saveIntoMysql2(Map<String, Object> map){
        bdJdbcTemplate.update(INSERT_ZT_RESULT_YILIAO2,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"),map.get("keywords"),map.get("infoTypeSegment"),map.get("monitorUrl"),map.get("pocDetailUrl"));
    }
    public void saveIntoMysql3(Map<String, Object> map){
        bdJdbcTemplate.update(INSERT_ZT_RESULT_YILIAO3,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"),map.get("keywords"),map.get("infoTypeSegment"),map.get("monitorUrl"),map.get("pocDetailUrl"));
    }
    public void saveIntoMysql4(Map<String, Object> map){
        bdJdbcTemplate.update(INSERT_ZT_RESULT_YILIAO4,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
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