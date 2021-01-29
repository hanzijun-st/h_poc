package com.qianlima.offline.service;


import com.qianlima.offline.bean.Area;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.middleground.NewZhongTaiService;
import com.qianlima.offline.middleground.NotBaiLianZhongTaiService;
import com.qianlima.offline.util.ContentSolr;
import com.qianlima.offline.util.IctContentSolr;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.MathUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.qianlima.offline.rule02.NewRuleUtils.getIndustry;

@Service
@Slf4j
public class DeliveryPocService {

    @Autowired
    private ContentSolr contentSolr;

    @Autowired
    private IctContentSolr ictContentSolr;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    private NotBaiLianZhongTaiService notBaiLianZhongTaiService;

    @Autowired
    private NewZhongTaiService newZhongTaiService;

    @Autowired
    private ZhongTaiBiaoDiWuService zhongTaiBiaoDiWuService;


    HashMap<Integer, Area> areaMap = new HashMap<>();

    //地区
    @PostConstruct
    public void init() throws IOException {
        List<Map<String, Object>> maps = gwJdbcTemplate.queryForList("SELECT * FROM phpcms_area");
        for (Map<String, Object> map : maps) {
            Area area = new Area();
            area.setAreaid(Integer.valueOf(map.get("areaid").toString()));
            area.setName(map.get("name").toString());
            area.setParentid(map.get("parentid").toString());
            area.setArrparentid(map.get("arrparentid").toString());
            areaMap.put(Integer.valueOf(map.get("areaid").toString()), area);
        }
    }

    String ids[] = {"182001854","182296542","191670686","181982147","191595470","192746693","183769738","191670418","191670593"};

    //调取中台数据
    public void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            String contentInfo = resultMap.get("content").toString();
            String content = processAboutContent(contentInfo);
            if (StringUtils.isNotBlank(content)) {
                resultMap.put("content", content);
            }
            newZhongTaiService.saveIntoMysql(resultMap);
        }
    }

    //调取中台数据 二次 进行对比数据
    public void getDataFromZhongTaiAndSave2(NoticeMQ noticeMQ) {

        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
//            String zhao_biao_unit = resultMap.get("zhao_biao_unit").toString();
            String zhong_biao_unit = resultMap.get("zhong_biao_unit").toString();
//            String keyword = "";
                //精确用.equals(aa)  模糊用.contains(aa)
            if (zhong_biao_unit.contains("三维天地")){
                newZhongTaiService.saveIntoMysql(resultMap);
            }


        }
    }

    //调取中台数据 多个关键词后追加(包含黑词) && 匹配行业标签 && 二次进行对比数据(招标单位包含关键词)
    public void getDataFromZhongTaiAndSave3(NoticeMQ noticeMQ) {

        String[] aaa = {"物业管理服务","物业服务","保安服务","保洁服务","家政服务","物业保洁","物业管理","建设用地","建筑用地","住宅用地","住房用地","工业用地","商业用地","划拨用地","建设地块","土地拍卖","土地流转","土地出让","土地出租","土地对外流转","用地出让","用地使用权","土地使用权","地块拍卖","地块竞得","地块成交","挂牌出让","地块挂牌","地块出让","土地划拨","用地划拨","地块划拨","国有土地"};
        String[] bbb = {"建设用地","建筑用地","住宅用地","住房用地","工业用地","商业用地","划拨用地","建设地块","土地拍卖","土地流转","土地出让","土地出租","土地对外流转","用地出让","用地使用权","土地使用权","地块拍卖","地块竞得","地块成交","挂牌出让","地块挂牌","地块出让","土地划拨","用地划拨","地块划拨","国有土地"};

        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";
            String content = resultMap.get("content") != null ? resultMap.get("content").toString() : "";
            String task_id = resultMap.get("task_id") != null ? resultMap.get("task_id").toString() : "";
            content = title + "&" + content;
            content = content.toUpperCase();
            String keyword = "";
            if (task_id.equals("65")){
                for (String bb : bbb) {
                    if (content.contains(bb)) {
                        String key = bb;
                        keyword += (key + "、");
                    }
                }
            }
            if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.substring(0, keyword.length() - 1);
                resultMap.put("keyword", keyword);
            }
            newZhongTaiService.saveIntoMysql(resultMap);

        }

    }

    //调取中台数据———— //预算金额 OR 中标金额字段值≥1000万
    public void getDataFromZhongTaiAndSave4(NoticeMQ noticeMQ){
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        //预算金额 OR 中标金额字段值≥1000万
        if (checkAmount(noticeMQ.getAmount())) {
            Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
            if (resultMap != null) {
                String contentInfo = resultMap.get("content").toString();
                String content = processAboutContent(contentInfo);
                if (StringUtils.isNotBlank(content)) {
                    resultMap.put("content", content);
                }
                newZhongTaiService.saveIntoMysql(resultMap);
            }
        }
    }

    //调取中台数据———— //预算金额 OR 中标金额字段值≥1000万 并且 数据进行二次对比
    public void getDataFromZhongTaiAndSave44(NoticeMQ noticeMQ){
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        String[] aaa = {"碧水源","中交","城乡"};
        //招标预算字段值≥1000万
        if (checkAmount(noticeMQ.getAmount())) {
            Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
            if (resultMap != null) {
//                String zhao_biao_unit = resultMap.get("zhao_biao_unit").toString();
            String zhong_biao_unit = resultMap.get("zhong_biao_unit").toString();
                for (String aa : aaa) {
                    //精确用.equals(aa)  模糊用.contains(aa)
                    if (zhong_biao_unit.contains(aa)){
                        newZhongTaiService.saveIntoMysql(resultMap);
                    }
                }
            }
        }
    }

    //调取中台数据并 获取标的物
    public void getDataFromZhongTaiAndSave5(NoticeMQ noticeMQ) {
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            newZhongTaiService.saveIntoMysql(resultMap);
            String contentId = resultMap.get("content_id") != null ? resultMap.get("content_id").toString() : "";
            String content = resultMap.get("content") != null ? resultMap.get("content").toString() : "";
            try {
                zhongTaiBiaoDiWuService.getAllZhongTaiBiaoDIWu(contentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //调取中台数据 并 匹配行业标签
    public void getDataFromZhongTaiAndSave6(NoticeMQ noticeMQ) {
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> map = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (map != null) {
            String zhaobiaounit = map.get("zhao_biao_unit") != null ? map.get("zhao_biao_unit").toString() : "";
            String zhaobiaoindustry = getIndustry(zhaobiaounit);
            String[] zhaobiaosplit = zhaobiaoindustry.split("-");
            if (zhaobiaosplit[0].contains("金融企业") || zhaobiaosplit[1].contains("金融")){
                newZhongTaiService.saveIntoMysql(map);
            }
        }
    }

    //调取中台数据 并 获取标的物解析表 并 匹配行业标签
    public void getDataFromZhongTaiAndSave7(NoticeMQ noticeMQ) {
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            String contentId = resultMap.get("content_id") != null ? resultMap.get("content_id").toString() : "";
            String zhaobiaounit = resultMap.get("zhao_biao_unit") != null ? resultMap.get("zhao_biao_unit").toString() : "";
            String zhaobiaoindustry = getIndustry(zhaobiaounit);
            String[] zhaobiaosplit = zhaobiaoindustry.split("-");
            if (zhaobiaosplit[1].contains("金融") || zhaobiaosplit[0].contains("金融企业")){
                // 匹配行业标签
                newZhongTaiService.saveIntoMysql(resultMap);
                try {
                    //获取标的物清单表
//                    zhongTaiBiaoDiWuService.getAllZhongTaiBiaoDIWu(contentId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //调取中台数据 并 匹配关键词
    public void getDataFromZhongTaiAndSave8(NoticeMQ noticeMQ) throws IOException {
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }

        String[] hhy = {"银行业监督管理","保险监督","证券监督","证监会","金融","外汇","经济委员会","经济局","经济服务","银监局","货币","经济管理","经济发展","人民币","保监局","银监会","保监会","经济运行","基金会","信用中心","银监分局","经济贸易","反洗钱","经信局","社会信用","证券业","经贸发展","期货","股票","经济合作","银行","分行","支行","央行","农商行","储蓄","证券","国债","交易所","保险经纪","保险股份","保险有限","保险公司","保险集团","中国人寿","保险责任","保险（集团","保险(集团","太平人寿","太平财险","人寿保险","人保财险","国联人寿","经济合作社","信用社","信用合作联社","经济联合社","联社","联合社","供销合作社","合作社","经济社","银联","信托","私募","基金","经济","经贸","资产管理","资产运营","资产经营","投资","融资","财务","信贷","贷款","资产发展","资本","资产","支付","结算","清算","理财","资金","信用卡","资管"};
        String[] aaa = {"软件","硬件","网络","系统","设备","智能","智慧","终端","主机","硬盘","平板","板卡","显卡","电源","电脑","光盘","磁盘","键盘","触屏","主屏","读卡","微机","手机","存储","镜头","屏幕","磁带","音控","音响","音箱","摄影","摄制","录音","播室","导播","配音","屏播","录播","VR","直播","视频","影音","音像","音视","试听","抖音","电纸","电子","数传","数码","数字","舆情","OA","智库","应答","银医","银校","页面","网阅","创课","主页","腾讯","搜狗","门户","官网","新浪","微信","微端","微博","头条","企微","快手","云鉴","云政","冀云","微云","云镜","云端","云勘","云图","警智","智拍","识别","算法","ＶＲ","AI","消息","邮件","钉钉","端口","队列","接口","杀软","显存"};
        List<String> bbb = LogUtils.readRule("smf");

        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";
            String content = resultMap.get("content") != null ? resultMap.get("content").toString() : "";
            String zhao_biao_unit = resultMap.get("zhao_biao_unit") != null ? resultMap.get("zhao_biao_unit").toString() : "";
            content = title + "&" + content;
            content = content.toUpperCase();
            String keyword = "";
            String code = "";
            for (String hy : hhy) {
                if (title.contains(hy) || zhao_biao_unit.contains(hy)){
                    keyword += (hy + "、") ;
                }
            }
            for (String aa : aaa) {
                if (content.contains(aa)){
                    code += (aa + "、");
                }
            }
            for (String bb : bbb) {
                if (content.contains(bb)){
                    code += (bb + "、");
                }
            }
            if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.substring(0, keyword.length() - 1);
                resultMap.put("keyword", keyword);
            }
            if (StringUtils.isNotBlank(code)) {
                code = code.substring(0, code.length() - 1);
                resultMap.put("task_id", code);
            }
            newZhongTaiService.saveIntoMysql(resultMap);
        }
    }

    //调取中台数据 多个关键词后追加(包含黑词) && 匹配行业标签 && 二次进行对比数据(招标单位包含关键词)
    public void getDataFromZhongTaiAndSave9(NoticeMQ noticeMQ) throws IOException {

        String[] aaa = {"物业管理服务","物业服务","保安服务","保洁服务","家政服务","物业保洁","物业管理","建设用地","建筑用地","住宅用地","住房用地","工业用地","商业用地","划拨用地","建设地块","土地拍卖","土地流转","土地出让","土地出租","土地对外流转","用地出让","用地使用权","土地使用权","地块拍卖","地块竞得","地块成交","挂牌出让","地块挂牌","地块出让","土地划拨","用地划拨","地块划拨","国有土地"};

        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";
            String content = resultMap.get("content") != null ? resultMap.get("content").toString() : "";
            content = title + "&" + content;
            content = content.toUpperCase();
            String keyword = "";
            for (String aa : aaa) {
                if (content.contains(aa)) {
                    keyword += (aa + "、");
                }
            }
            if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.substring(0, keyword.length() - 1);
                resultMap.put("keyword", keyword);
            }
            newZhongTaiService.saveIntoMysql(resultMap);

        }

    }

    // 只去除<a>标签所有信息
    public static String processAboutContent(String content) {
        Document document = Jsoup.parse(content);
        Elements elements = document.select("a[href]");
        Integer elementSize = elements.size();
        for (Integer i = 0; i < elementSize; i++) {
            Element element = elements.get(i);
            if (element == null || document.select("a[href]") == null || document.select("a[href]").size() == 0) {
                break;
            }
            if (StringUtils.isNotBlank(element.attr("href"))) {
                if (element.is("a")) {
                    element.remove();
                }
            }
        }
        return document.body().html();
    }

    /**
     * 判断字符串是否是金额
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        if(StringUtils.isBlank(str)){
            return false;
        }
        Pattern pattern = Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,5})?$"); // 判断小数点后2位的数字的正则表达式
        Matcher match = pattern.matcher(str);
        if (match.matches() == false) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkAmount(String amount) {
        //金额为空 或（金额纯数字的& 金额大于等于1000万）
        if (StringUtils.isBlank(amount) ||
                (MathUtil.match(amount) && new BigDecimal(amount).compareTo(new BigDecimal("10000000")) >= 0)) {
            return true;
        }
        return false;
    }

    //中节能 线下交付
    public void getZhongJieNengSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"环卫一体化","道路清扫","道路保洁","道路清扫保洁","垃圾收集","垃圾转运","垃圾收转运","垃圾清运","智慧环卫"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20170101 TO 20201231] AND province:17 AND progid:3 AND catid:[* TO 100] AND title:\"" + aa + "\" ", key, 1);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
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


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            String key = aa ;
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
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave4(content)));
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

    //大金投资 线下交付
    public void getDaJinSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"多联机","中央空调","机电分包","挂壁式空调","立柜式空调","窗式空调","吊顶式空调","挂机空调","立式空调","空调挂机","柜机空调","家用空调","壁挂空调","变频空调","风管机","天花机","商用空调","空调"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201130] AND province:2 AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 2);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
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


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            String key = aa ;
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
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());

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

    //和德创新 线下交付
    public void getHeDeSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"生命防护栏","安全防护栏","波形护栏","防撞护栏","安全护栏"};
        String[] aa1 = {"护栏"};
        String[] aa2 = {"高速路","隧道","桥梁","桥面","大道","大桥","市政","乡道","公路"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190101 TO 20191231] AND progid:3 AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 2);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
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

        for (String a1 : aa1) {
            for (String a2 : aa2) {
                futureList1.add(executorService1.submit(() -> {
                    String key = a1 + "&" + a2 ;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190101 TO 20191231] AND progid:3 AND catid:[* TO 100] AND allcontent:\"" + a1 + "\" AND allcontent:\"" + a2 + "\" ", key, 2);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    list1.add(data);
                                    data.setKeyword(key);
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


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            String key = aa ;
            arrayList.add(key);
        }
        for (String a1 : aa1) {
            for (String a2 : aa2) {
                String key = a1 + "&" + a2 ;
                arrayList.add(key);
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
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());

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

    //雷度米特 线下交付
    public void getLeiDuMiTeSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"血气", "心脏标志物", "心肌标志物", "床旁免疫", "降钙素原", "荧光免疫", "免疫荧光", "POCT", "心梗三项"};
        String[] blacks = {"工程改建","改建工程","工程建设","建设工程","工程装修","装修工程","工程改造","改造工程","装修","粉刷","空调","电梯","装饰","零星工程","停车场工程","卫生院","基层","流标","废标","无效","暂停","终止","中止"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201001 TO 20201218] AND progid:[0 TO 3] AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 2);
                log.info(key.trim() + "————" + mqEntities.size());
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
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
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


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            String key = aa ;
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
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave5(content)));
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

    //永升物业
    public void getYongShengSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"物业","保安","保洁","家政","清洁","绿化","后勤","安保"};
        String[] aa1 = {"管理","服务","劳务","外包"};
        String[] bbb = {"物业管理服务","物业服务","保安服务","保洁服务","家政服务","物业保洁","物业管理"};

        for (String aa : aaa) {
            for (String a1 : aa1) {
                futureList1.add(executorService1.submit(() -> {
                    String key = aa + "&" + a1;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20180101 TO 20201209] AND (province:31 OR province:29 OR province:7)  AND (progid:3 OR progid:5) AND catid:[* TO 100] AND (title:\"" + aa + "\" AND title:\"" + a1 + "\" )", key, 64);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    list1.add(data);
                                    data.setKeyword(key);
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

        for (String bb : bbb) {
            futureList1.add(executorService1.submit(() -> {
                String key = bb ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20180101 TO 20201209] AND (province:31 OR province:29 OR province:7) AND (progid:3 OR progid:5) AND catid:[* TO 100] AND (allcontent:\"" + bb + "\")", key, 65);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
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


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            for (String a1 : aa1) {
                String key = aa + "&" + a1;
                arrayList.add(key);
            }
        }
        arrayList.addAll(Arrays.asList(bbb));

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
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());



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

    //淡水泉 线下交付
    public void getDanShuiQuanSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"视频","监控","摄像","雪亮","天网","天眼"};
        String[] bbb = {"智慧城市","智慧交通","智能交通","电子警察"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201001 TO 20201228] AND progid:[0 TO 3] AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 1);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
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

        for (String bb : bbb) {
            futureList1.add(executorService1.submit(() -> {
                String key = bb ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201001 TO 20201228] AND progid:[0 TO 3] AND catid:[* TO 100] AND title:\"" + bb + "\" ", key, 2);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
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


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            String key = aa ;
            arrayList.add(key);
        }
        for (String aa : bbb) {
            String key = aa ;
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
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave4(content)));
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

    //淡水泉 线下交付2
    public void getDanShuiQuan2SolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"污水","供水","自来水","再生水","管网","水厂"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201201 TO 20201228] AND progid:[0 TO 2] AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 1);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
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


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            String key = aa ;
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
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave4(content)));
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

    //淡水泉 线下交付3
    public void getDanShuiQuan3SolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"污水","供水","自来水","再生水","管网","水厂"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201231] AND progid:3 AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 1);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
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


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            String key = aa ;
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
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave44(content)));
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

    //维尔利 线下交付
    public void getWeiErLiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"渗滤液"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201101 TO 20201230] AND progid:3 AND catid:[* TO 100] AND title:\"" + aa + "\" ", key, 1);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
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


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            String key = aa ;
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
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());

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

    public void getSanWeiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"三维天地"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201231] AND (progid:[0 TO 3] OR progid:5)  AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 1);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
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


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String aa : aaa) {
            String key = aa ;
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
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave2(content)));
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
















