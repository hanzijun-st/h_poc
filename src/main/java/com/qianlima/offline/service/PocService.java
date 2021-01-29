package com.qianlima.offline.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.Area;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.middleground.BaiLianZhongTaiService;
import com.qianlima.offline.middleground.NewZhongTaiService;
import com.qianlima.offline.middleground.NotBaiLianZhongTaiService;
import com.qianlima.offline.middleground.ZhongTaiService;
import com.qianlima.offline.rule02.NewRuleUtils;
import com.qianlima.offline.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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

@Service
@Slf4j
public class PocService {

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
    private ZhongTaiService zhongTaiService;

    @Autowired
    private BaiLianZhongTaiService baiLianZhongTaiService;

    @Autowired
    private ZhongTaiBiaoDiWuService zhongTaiBiaoDiWuService;

    @Autowired
    private CusDataFieldService cusDataFieldService;
    @Autowired
    private CusDataFieldService2 cusDataFieldService2;


    HashMap<Integer, Area> areaMap = new HashMap<>();

    //地区
    @PostConstruct
    public void init() {
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
        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            String contentInfo = resultMap.get("content").toString();
            String content = processAboutContent(contentInfo);
            if (StringUtils.isNotBlank(content)) {
                resultMap.put("content", content);
            }
            newZhongTaiService.saveIntoMysql(resultMap);
        }
    }

    //调取中台数据
    public Map<String, Object> getDataFromZhongTaiAndList(NoticeMQ noticeMQ) {
        boolean result = cusDataFieldService2.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return null;
        }
        Map<String, Object> resultMap = cusDataFieldService2.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            String contentInfo = resultMap.get("content").toString();
            String content = processAboutContent(contentInfo);
            if (StringUtils.isNotBlank(content)) {
                resultMap.put("content", content);
            }
            newZhongTaiService.saveIntoMysql(resultMap);
        }
        return resultMap;
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

    //调取中台数据 二次 进行对比数据
    public void getDataFromZhongTaiAndSave2(NoticeMQ noticeMQ) {

        String[] aaa = {"联通","联合网络通信","联合通信"};

        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
//            String zhao_biao_unit = resultMap.get("zhao_biao_unit").toString();
            String zhong_biao_unit = resultMap.get("zhong_biao_unit").toString();
            String keyword = "";
            for (String aa : aaa) {
                //精确用.equals(aa)  模糊用.contains(aa)
                if (zhong_biao_unit.contains(aa)){
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

    //调取中台数据 多个关键词后追加(包含黑词) && 匹配行业标签 && 二次进行对比数据(招标单位包含关键词)
    public void getDataFromZhongTaiAndSave3(NoticeMQ noticeMQ) throws IOException {

        String[] aaa = {"医","护理","护士","整形","健康","诊治","急救","治疗"};
        String[] bbb = {"内镜","会诊","诊断","胶片","超声","医共体","医联体","医疗云","云医院","云医疗","医院云","PACS","公共卫生","集成平台","智慧医院","远程医疗","远程会诊","远程诊疗","电子病历","区域医疗","医学影像","预约系统","影像系统","叫号系统","预约平台","叫号平台","人工智能","医疗上云","超声系统","挂号平台","挂号系统","在线挂号","数字医疗","诊断系统","医联预约","医疗信息化","医院信息化","数字化医疗","医院智能化","智能化医疗","健康医疗云","医疗云服务","医疗云平台","信息化咨询","信息化项目","信息化建设","信息化系统","信息化平台","大超声系统","挂号app","医疗数字化","智慧云医院","智能云系统","信息安全测评","医疗人工智能","人工智能医疗","大数据云医疗","远程医疗协作","远程服务系统","超声诊断系统","彩银超声系统","远程超声系统","远程会诊系统","微信预约挂号","预约门诊挂号","预约挂号系统","预约挂号平台","微信挂号平台","网上挂号平台","挂号预约平台","微医预约挂号","挂号网上预约","医疗挂号软件","网上挂号预约","网上预约挂号","网络预约挂号","医疗叫号系统","排队叫号系统","叫号排队系统","人工智能应用","人工智能平台","人工智能产品","内镜治疗系统","内镜清洗系统","预约分诊系统","医联预约平台","预约管理系统","集中预约系统","网上预约医生","网上预约专家","预约转诊接口","医疗信息化改造","医疗信息化建设","数字化放射成像","医院智能化建设","医疗影像云存储","智医助理信息化","信息化管理平台","信息化应用系统","超声微探头系统","人工智能与医疗","智慧云医院服务","智慧云平台建设","影像云平台服务","智能化系统研发","智能化系统项目","智能化系统建设","智能化系统集成","智能化系统工程","智能化升级建设","智能化升级改造","智能化建设项目","智能化建设系统","智能化建设采购","智能化管理系统","智能化工程建设","智能化改造项目","智能化安装项目","影像传输与归纳","超声骨科手术系统","容积成像超声系统","远程会诊咨询系统","药房取药叫号系统","叫号系统在线询价","人工智能导诊系统","人工智能网络安全","医院信息管理系统","智能诊断系统硬件","中医综合诊断系统","耳鼻喉科内镜系统","预约诊疗服务系统","医技集中预约系统","预约管理信息系统","服务预约管理系统","“智医助理”信息化","信息化管理服务平台","超声切割止血刀系统","多普勒超声诊断系统","教学诊断与改进平台","诊断与改进信息化平台","医学影像管理诊断软件","超声光散射乳腺诊断系统","多重PCR分子诊断系统","影像诊断结构化报告系统","便携式彩超多普勒超声系统","人工智能AI辅助诊查系统","全自动快速分子诊断检测系统","结核分枝杆菌复合群快速诊断系统"};
        String[] ccc = {"远程","信息化","云平台","智能"};
        String[] cc1 = {"数字化"};
        String[] cc2 = {"医疗"};
        List<String> blacks = LogUtils.readRule("smf");

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
            boolean flag = true;
            for (String black : blacks) {
                if(StringUtils.isNotBlank(title) && title.contains(black)){
                    flag = false;
                    break;
                }
            }
            if (flag){
                for (String bb : bbb) {
                    if (content.contains(bb)) {
                        keyword += (bb + "、");
                    }
                }
                for (String cc : ccc) {
                    if (title.contains(cc)) {
                        keyword += (cc + "、");
                    }
                }
                for (String c1 : cc1) {
                    for (String c2 : cc2) {
                        if (title.contains(c1) && title.contains(c2)) {
                            keyword += (c1 + "&" + c2 + "、");
                        }
                    }
                }
            }
            if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.substring(0, keyword.length() - 1);
                resultMap.put("keyword", keyword);
            }

            String zhaobiaounit = resultMap.get("zhao_biao_unit") != null ? resultMap.get("zhao_biao_unit").toString() : "";
            String zhaobiaoindustry = NewRuleUtils.getIndustry(zhaobiaounit);
            String[] zhaobiaosplit = zhaobiaoindustry.split("-");
            String[] hhy = {"医疗","血站","急救中心","疾控中心","卫生院","疗养院","专科医院","中医院","综合医院","医疗服务"};
            for (String hy : hhy) {
                if (zhaobiaosplit[1].contains(hy)){
                    newZhongTaiService.saveIntoMysql(resultMap);
                }
            }
            for (String aa : aaa) {
                if (zhaobiaounit.contains(aa) && (zhaobiaosplit[1].contains("大学") || zhaobiaosplit[1].contains("培训") || zhaobiaosplit[1].contains("学校"))){
                    newZhongTaiService.saveIntoMysql(resultMap);
                }
            }
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
        if (checkAmount(noticeMQ.getBudget()) || checkAmount(noticeMQ.getNewAmountUnit())) {
            Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
            if (resultMap != null) {
                newZhongTaiService.saveIntoMysql(resultMap);
            }
        }
    }

    //调取中台数据并 获取标的物
    public void getDataFromZhongTaiAndSave5(NoticeMQ noticeMQ) {

        String[] aaa = {"电脑","平板","音频","PC机","计算机","可穿戴","IOT","物联网"};
        String[] bbb = {"智能音频","智能物联","智能平板","智能路由","智能电视","智能穿戴","智能白板","智慧音频","智慧物联","智慧平板","智慧电视","智慧白板","掌上计算机","掌上电脑","游戏本","一体台式机","一体机电脑","液晶平板电视","液晶电视","台式计算机","台式机","台式电脑","手提电脑","商务本","全面屏电视","苹果计算机","苹果电脑","苹果笔记本","苹果ipad","平板电脑","平板笔记本","品牌电脑","路由器","联想台式机","联想计算机","联想电脑","联想笔记本","可穿戴设备","计算机购置","计算机采购","机房电脑","惠普台式机","惠普平板","惠普计算机","惠普电脑","惠普笔记本","华为计算机","华为电脑","华为笔记本","华硕台式机","华硕平板","华硕计算机","华硕电脑","华硕笔记本","购置电脑","高性能移动计算机","高性能笔记本","高清电视","电子白板","电视一体机","电视设备","电视机","电视购置","电视采购","电脑主机","电脑整机","电脑一体机","电脑设备","电脑购置","电脑采购","电脑笔记本","戴尔台式机","戴尔平板","戴尔计算机","戴尔电脑","戴尔笔记本","打印设备","打印机","彩色电视","采购计算机","采购电视","采购电脑","便携式计算机","便携式电脑","便携计算机","便携电脑","笔记本电脑","办公计算机","办公电脑","macbookpro电脑","MacBook","Lenovo台式机","Lenovo计算机","Lenovo电脑","Lenovo笔记本","HP台式机","HP平板","HP计算机","HP电脑","HP笔记本","DELL台式机","DELL平板","DELL计算机","DELL电脑","DELL笔记本","ASUS台式机","ASUS平板","ASUS计算机","ASUS电脑","ASUS笔记本"};
        String[] blacks = {"平板拖把", "LED平板灯", "平板集装箱", "平板探测器"};

        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            String contentId = resultMap.get("content_id") != null ? resultMap.get("content_id").toString() : "";
            String content = resultMap.get("content") != null ? resultMap.get("content").toString() : "";
            String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";
            String task_id = resultMap.get("task_id") != null ? resultMap.get("task_id").toString() : "";
            content = title + "&" + content;
            content = content.toUpperCase();
            String keyword = "";
            String code = "";
            boolean flag = true;
            for (String black : blacks) {
                if(StringUtils.isNotBlank(title) && title.contains(black)){
                    flag = false;
                    break;
                }
            }
            if (flag){
                if (task_id.equals("1")){
                    for (String aa : aaa) {
                        if (title.contains(aa)){
                            keyword += (aa + "、") ;
                        }
                    }
                }
                if (task_id.equals("2")){
                    for (String bb : bbb) {
                        if (content.contains(bb)){
                            code += (bb + "、") ;
                        }
                    }
                }
            }
            if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.substring(0, keyword.length() - 1);
                resultMap.put("keyword", keyword);
            }
            if (StringUtils.isNotBlank(code)) {
                code = code.substring(0, code.length() - 1);
                resultMap.put("keyword_term", code);
            }
            newZhongTaiService.saveIntoMysql(resultMap);
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
        String progid = handleZhongTaiGetResultMapWithContent(noticeMQ);
        if (map != null && progid != null && (progid.equals("3") || progid.equals("4") || progid.equals("9") || progid.equals("11") || progid.equals("12"))) {
            map.put("keyword_term", progid);
            String zhaobiaounit = map.get("zhao_biao_unit") != null ? map.get("zhao_biao_unit").toString() : "";
            String task_id = map.get("task_id") != null ? map.get("task_id").toString() : "";
            if (task_id.equals("2")){
                String zhaobiaoindustry = NewRuleUtils.getIndustry(zhaobiaounit);
                String[] zhaobiaosplit = zhaobiaoindustry.split("-");
                if (StringUtils.isNotBlank(zhaobiaounit)){
                    if ("医疗单位".equals(zhaobiaosplit[0]) || "政府机构-医疗".equals(zhaobiaoindustry) || "商业公司-医疗服务".equals(zhaobiaoindustry) || zhaobiaounit.contains("监狱")){
                        newZhongTaiService.saveIntoMysql(map);
                    }
                }
            }else {
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
            String zhaobiaoindustry = NewRuleUtils.getIndustry(zhaobiaounit);
            String[] zhaobiaosplit = zhaobiaoindustry.split("-");
            if (zhaobiaosplit[1].contains("金融") || zhaobiaosplit[0].contains("金融企业")){
                // 匹配行业标签
                newZhongTaiService.saveIntoMysql(resultMap);
                try {
                    //获取标的物清单表
                    zhongTaiBiaoDiWuService.getAllZhongTaiBiaoDIWu(contentId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //调取中台数据 并 匹配关键词
    public void getDataFromZhongTaiAndSave8(NoticeMQ noticeMQ) throws IOException {

        String[] aaa = {"服务机器人","语音机器人","智能机器人","接待机器人","讲解机器人","移动机器人","养老机器人","酒店机器人","政务机器人","迎宾机器人","聊天机器人"};

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
                    String key = aa;
                    keyword += (key + "、");
                }
            }
            if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.substring(0, keyword.length() - 1);
                resultMap.put("keyword", keyword);
            }
            newZhongTaiService.saveIntoMysql(resultMap);
//            zhongTaiBiaoDiWuServiceForOne.getAllZhongTaiBiaoDIWu(contentId);
        }

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

    /**
     * 判断字符串是否是金额
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        if(StringUtils.isBlank(str)){
            return false;
        }
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,5})?$"); // 判断小数点后2位的数字的正则表达式
        java.util.regex.Matcher match = pattern.matcher(str);
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

    //获取乙方宝标的物块
    public String getYFBBiaoDiWu(String content) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            HttpPost post = new HttpPost("http://47.93.191.54:5110/z");
            //创建参数列表
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("text", content));
            post.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
            //url格式编码
            post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            //设置超时时间为60秒
            post.setConfig(requestConfig);
            //执行请求
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String entity = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                return entity;
            }
        } catch (Exception e) {
            log.error("结果细分判断出错:{}", e);
            throw new RuntimeException("乙方宝标的物出错");
        }
        return null;
    }

    //阿里云-业务部
    public void geALiYunSolrAllField(String date) throws IOException {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] ddd = {"排水系统","污水系统","污水管理","排水管理","废水系统","废水管理","水资源系统","水环境系统","自来水系统","水污染系统","地下水系统","直饮水系统","水质系统","供水系统","净水系统","给水系统","地表水系统","饮用水系统","排水信息化","废水信息化","污水信息化","水资源信息化","水环境信息化","自来水信息化","水污染信息化","地下水信息化","直饮水信息化","水质信息化","水库信息化","供水信息化","净水信息化","给水信息化","地表水信息化","饮用水信息化","排水设备","废水设备","污水设备","水资源设备","水环境设备","水污染设备","地下水设备","水质设备","水库设备","供水设备","净水设备","给水设备","地表水设备","饮用水设备","排水设施","废水设施","污水设施","水资源设施","水环境设施","自来水设施","水污染设施","地下水设施","直饮水设施","水质设施","水库设施","供水设施","给水设施","地表水设施","饮用水设施","排水数字","废水数字","污水数字","水资源数字","水环境数字","自来水数字","水污染数字","地下水数字","直饮水数字","水质数字","水库数字","供水数字","净水数字","给水数字","地表水数字","饮用水数字","排水数据","废水数据","污水数据","水资源数据","水环境数据","自来水数据","水污染数据","地下水数据","直饮水数据","水质数据","水库数据","供水数据","净水数据","给水数据","地表水数据","饮用水数据","排水平台","废水平台","污水平台","水资源平台","水环境平台","自来水平台","水污染平台","地下水平台","直饮水平台","水质平台","水库平台","供水平台","净水平台","给水平台","地表水平台","饮用水平台","排水虚拟","废水虚拟","污水虚拟","水资源虚拟","水环境虚拟","自来水虚拟","水污染虚拟","地下水虚拟","直饮水虚拟","水质虚拟","水库虚拟","供水虚拟","净水虚拟","给水虚拟","地表水虚拟","饮用水虚拟","排水在线","废水在线","污水在线","水资源在线","水环境在线","自来水在线","水污染在线","地下水在线","直饮水在线","水质在线","水库在线","供水在线","净水在线","给水在线","地表水在线","饮用水在线","排水监测","污水监测","水资源监测","水环境监测","自来水监测","水污染监测","地下水监测","直饮水监测","水库监测","供水监测","净水监测","给水监测","地表水监测","饮用水监测","排水监控","废水监控","污水监控","水资源监控","水环境监控","自来水监控","水污染监控","地下水监控","直饮水监控","水质监控","水库监控","供水监控","净水监控","给水监控","地表水监控","饮用水监控","排水自动监控","废水自动监控","污水自动监控","水资源自动监控","水环境自动监控","自来水自动监控","水污染自动监控","地下水自动监控","直饮水自动监控","水质自动监控","水库自动监控","供水自动监控","净水自动监控","给水自动监控","地表水自动监控","饮用水自动监控","排水在线监测","废水在线监测","污水在线监测","水资源在线监测","水环境在线监测","自来水在线监测","水污染在线监测","地下水在线监测","直饮水在线监测","水质在线监测","水库在线监测","供水在线监测","净水在线监测","给水在线监测","地表水在线监测","饮用水在线监测","在线排水","在线废水","在线污水","在线水资源","在线水环境","在线自来水","在线水污染","在线地下水","在线直饮水","在线供水","在线净水","在线给水","在线地表水","在线饮用水","智慧水务"};
        String[] blacks = {"印刷","销号公示","物业管理服务","物业服务","物业管理","医疗设备","备件","耗材","管材","厨房用具","办公用品","办公设备","阀门","防护栅栏","消防改造","清淤","配电","围墙","电梯","空调","绿化","保洁","保安","安保","盖板","滑坡治理","线路","道路改造","车棚","塑胶","道路拓宽","清淤","垃圾无害化","科研设备","泵车","仪器","教学设备"};

        for (String dd : ddd) {
            futureList1.add(executorService1.submit(() -> {
                    String key = dd;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201205 TO 20201209] AND (progid:3 OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + dd + "\" ", key, 3);
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

        arrayList.addAll(Arrays.asList(ddd));


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
                futureList.add(executorService.submit(() -> {
                    getDataFromZhongTaiAndSave(content);
                }));
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

    //匹配行业标签
    public void getRuiWoDeSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        futureList1.add(executorService1.submit(() -> {
            List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201101 TO 20201216] AND progid:1 ", "", 1);
            if (!mqEntities.isEmpty()) {
                for (NoticeMQ data : mqEntities) {
                    if (data.getTitle() != null) {
                        boolean flag = true;
                        if (flag) {
                            list1.add(data);
                            if (!dataMap.containsKey(data.getContentid().toString())) {
                                list.add(data);
                                dataMap.put(data.getContentid().toString(), "0");
                            }
                        }
                    }
                }
            }
        }));

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

    //平安联想
    public void getPingAnSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"医疗废物系统","医疗废物信息化系统","医废管理系统","医废系统","医疗废弃物系统","医疗废弃物管理系统","医疗废弃物信息化系统","医疗废物管理系统","医废信息化系统","医疗废物平台","医疗废弃物平台"};
        String[] bbb = {"医疗"};
        String[] bb1 = {"废物","废弃物"};
        String[] bb2 = {"系统","信息化","平台"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190101 TO 20201023] AND progid:3 AND catid:[* TO 100] AND (allcontent:\"" + aa + "\")", key, 111);
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
            for (String b1 : bb1) {
                for (String b2 : bb2) {
                    futureList1.add(executorService1.submit(() -> {
                        String key = bb+"&"+b1+"&"+b2 ;
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190101 TO 20201023] AND progid:3 AND catid:[* TO 100] AND (title:\"" + bb + "\" AND title:\"" + b1 + "\" AND title:\"" + b2 + "\")", key, 1);
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
        arrayList.addAll(Arrays.asList(aaa));
        for (String bb : bbb) {
            for (String b1 : bb1) {
                for (String b2 : bb2) {
                    String key = bb+"&"+b1+"&"+b2 ;
                    arrayList.add(key);
                }
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



//        if (list != null && list.size() > 0) {
//            ExecutorService executorService = Executors.newFixedThreadPool(80);
//            List<Future> futureList = new ArrayList<>();
//            for (NoticeMQ content : list) {
//                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
//            }
//            for (Future future : futureList) {
//                try {
//                    future.get();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//            executorService.shutdown();
//        }
    }

    //维尔利线下交付
    public void getWeiErLiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"渗滤液"};
//        String[] bbb = {"治理","搬迁"};
//        String[] ccc = {"北京国环清华环境工程设计研究院有限公司"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201119] AND progid:3 AND catid:[* TO 100] AND (title:\"" + aa + "\" )", key, 1);
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

//        for (String cc : ccc) {
//            futureList1.add(executorService1.submit(() -> {
//                String key = cc ;
//                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20150101 TO 20201102] AND (progid:3 OR progid:[0 TO 1]) AND catid:[* TO 100] AND (blZhongBiaoUnit:\"" + cc + "\")", key, 1);
//                log.info(key.trim() + "————" + mqEntities.size());
//                if (!mqEntities.isEmpty()) {
//                    for (NoticeMQ data : mqEntities) {
//                        if (data.getTitle() != null) {
//                            boolean flag = true;
//                            if (flag) {
//                                list1.add(data);
//                                data.setKeyword(key);
//                                if (!dataMap.containsKey(data.getContentid().toString())) {
//                                    list.add(data);
//                                    dataMap.put(data.getContentid().toString(), "0");
//                                }
//                            }
//                        }
//                    }
//                }
//            }));
//        }


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

        arrayList.addAll(Arrays.asList(aaa));



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

    //睿博天米
    public void getRuiBoSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"服务机器人","语音机器人","智能机器人","接待机器人","讲解机器人","移动机器人","养老机器人","酒店机器人","政务机器人","迎宾机器人","聊天机器人"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201101 TO 20201231] AND progid:3 AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 104);
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
        arrayList.addAll(Arrays.asList(aaa));

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
                futureList.add(executorService.submit(() -> {
                    try {
                        getDataFromZhongTaiAndSave8(content);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
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

    //中讯邮电
    public void getZhongXunSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"IDC机房","大数据中心机房","智能计算中心机房","电信机房","模块化机房","通信机房","双线机房","数据中心机房","医院机房","法庭机房","机房建设","电脑机房","学生机房","综合机房","实验机房","办公区机房","机房基础设施","信息中心机房","学院机房","信息机房","实训机房","DSA机房","实验室机房","CT机房","CT室机房","核心机房","校区机房","司法局机房","电话机房","通风机房","放射影像机房","放射治疗机房","机房UPS","一体化基站","机房改造","机房改建","智能机房","教学机房","基地机房","主机房","网络机房","网络中心机房","自动化机房","新馆机房","档案馆机房","牙片机房","屏蔽机房","设备机房","用房中心机房","灾备机房","工程机房","数据机房","备用机房","发电机房","传输机房","防护机房","消控机房","中心机房","一体化机房","机房整改","模拟机房","检察院机房","校园机房","可视化机房","建筑机房","直管机房","医共体机房"};
        String[] bbb = {"设计"};
        String[] ccc = {"机房"};


        for (String aa : aaa) {
            for (String bb : bbb) {
                futureList1.add(executorService1.submit(() -> {
                    String key = aa + "&" + bb ;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201031] AND progid:3 AND catid:[* TO 100] AND (allcontent:\"" + aa + "\" AND title:\"" + bb + "\" )", key, 104);
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
            for (String cc : ccc) {
                futureList1.add(executorService1.submit(() -> {
                    String key = bb + "&" + cc ;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201031] AND progid:3 AND catid:[* TO 100] AND (title:\"" + cc + "\" AND title:\"" + bb + "\" )", key, 106);
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
            for (String bb : bbb) {
                String key = aa + "&" + bb;
                arrayList.add(key);
            }
        }
        for (String cc : ccc) {
            for (String bb : bbb) {
                String key = cc + "&" + bb;
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

    //美敦力
    public void getMeiDunLiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"高频电刀","能量平台","超声刀","超声手术刀","高频电外科系统","高频电外科设备","电刀","电外科工作站"};
        String[] bbb = {"新建","扩建","改建","改造"};

//        for (String aa : aaa) {
//            futureList1.add(executorService1.submit(() -> {
//                String key = aa ;
//                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201224] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 105);
//                log.info(key.trim() + "————" + mqEntities.size());
//                if (!mqEntities.isEmpty()) {
//                    for (NoticeMQ data : mqEntities) {
//                        if (data.getTitle() != null) {
//                            boolean flag = true;
//                            if (flag) {
//                                list1.add(data);
//                                data.setKeyword(key);
//                                if (!dataMap.containsKey(data.getContentid().toString())) {
//                                    list.add(data);
//                                    dataMap.put(data.getContentid().toString(), "0");
//                                }
//                            }
//                        }
//                    }
//                }
//            }));
//        }

        for (String bb : bbb) {
            futureList1.add(executorService1.submit(() -> {
                String key = bb ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201224] AND (progid:3 OR progid:5) AND catid:[* TO 100] AND title:\"" + bb + "\" ", key, 1);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null && (data.getZhaoBiaoUnit() != null && data.getZhaoBiaoUnit() != "")) {
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

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave6(content)));
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

    //电子网络设备
    public void getDianZiLiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"电脑","台式机","服务器","交换机","存储器","中继器","集线器","网桥","路由器","计算机","笔记本","光缆","显示屏","显示器","移动硬盘","终端设备","网络设备","硬盘","采集终端","主机"};
        String[] bbb = {"网络接口卡", "调制调解器", "光纤收发器", "无线接入点"};


        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20191118 TO 20201118] AND progid:3 AND catid:[* TO 100] AND (title:\"" + aa + "\" )", key, 1);
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
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20191118 TO 20201118] AND progid:3 AND catid:[* TO 100] AND (allcontent:\"" + bb + "\")", key, 1);
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
        arrayList.addAll(Arrays.asList(aaa));
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

    //安永
    public void getAnYongSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"长城电梯集团","天津瀚宇机电","天津奥华电梯","聚隆创展电梯","山西蒂森曼隆电梯","天津长城电梯"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20100101 TO 20201120] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 9989);
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



//        if (list != null && list.size() > 0) {
//            ExecutorService executorService = Executors.newFixedThreadPool(80);
//            List<Future> futureList = new ArrayList<>();
//            for (NoticeMQ content : list) {
//                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
//            }
//            for (Future future : futureList) {
//                try {
//                    future.get();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//            executorService.shutdown();
//        }
    }

    //福州雪品
    public void getXunPinSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"肝纤维化","无创呼吸机","骨密度","肺功能仪","有创呼吸机","家用呼吸机","双水平呼吸机","新生儿呼吸机","睡眠呼吸机","正压呼吸机","人工呼吸机","气氛呼吸机","肺功能检测仪","肺功能测量仪","肺功能检查仪","肺功能测试仪"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20141124 TO 20201124] AND province:3 AND progid:3 AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 1101);
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

    //上海商众
    public void getShangZhongSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"态势感知","威胁感知","威胁检测","威胁情报","防火墙","抗拒绝服务攻击","DDOS","防病毒","日志审计","运维审计","安全审计","VPN","堡垒机","云安全","上网行为","网络准入","访问控制","认证计费","身份认证","RADIUS","BRAS","云网络","应用交付","负载均衡","无线网络","无线WIFI","无线WLAN","云存储","路由器","网络设备","信息安全","网络安全","应用安全","数据安全","主机安全","安全管理","安全服务","移动安全","安全测评","业务安全","可信安全","流控设备","网络流量","应用性能","网络性能","安全可信","动态防御","主动防御","欺骗防御","深度包检测","DPI设备","DPI系统","APT","高级持续性威胁","沙箱","漏斗扫描","微隔离","零信任","云访问","云原生","云计算","服务器设备","存储设备","入侵防御","入侵检测","端点安全","终端安全"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20180101 TO 20201130] AND progid:3 AND catid:[* TO 100] AND title:\"" + aa + "\" ", key, 10);
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



//        if (list != null && list.size() > 0) {
//            ExecutorService executorService = Executors.newFixedThreadPool(80);
//            List<Future> futureList = new ArrayList<>();
//            for (NoticeMQ content : list) {
//                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
//            }
//            for (Future future : futureList) {
//                try {
//                    future.get();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//            executorService.shutdown();
//        }
    }

    //华大基因
    public void getHuaDaJiYinSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"华大基因","华大三生园","武汉华大医学检验所","华大精准营养","华大临床检验","苏州泓迅生物","华大临床检验","北京华大吉比爱生物","广润保险经纪","菁良基因科技","中地海外农业","华大智慧农业","润生苔藓科技","华大吉诺因","北京吉因加医学检验实验室","北芯生命","嘉晨西海","深圳裕策生物","艾欣达伟","深圳知因细胞","华大优选","何氏眼视光","何氏眼科","杭州先为达生物"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190101 TO 20191231] AND progid:3 AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 111);
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

    //浙江华数广电
    public void getHuaShuSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"联通","联合网络通信","联合通信"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201207] AND province:30 AND progid:3 AND catid:[* TO 100] AND blZhongBiaoUnit:* AND blZhongBiaoUnit:\"" + aa + "\" ", key, 12);
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

    //银河物业
    public void getYinHeSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"物业管理服务"};
        String[] blacks = {"绿化","工程","物业用户","物业公司","物业管理公司","物业服务公司","物业管理服务公司"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190101 TO 20201126] AND progid:3 AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 10);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            for (String black : blacks) {
                                if(StringUtils.isNotBlank(data.getZhongBiaoUnit()) && data.getZhongBiaoUnit().contains(black)){
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

    //深信服
    public void getShenXinFuSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"网络建设","无线网络","智慧校园","无线AP","交换机"};
        String[] bbb = {"WIFI", "WI-FI"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20180101 TO 20201124] AND province:23 AND progid:3 AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 1);
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
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20180101 TO 20201124] AND province:23 AND progid:3 AND catid:[* TO 100] AND title:\"" + bb + "\" ", key, 1);
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
        arrayList.addAll(Arrays.asList(aaa));
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

    //大金投资
    public void getDaJinSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"多联机","中央空调","机电分包","挂壁式空调","立柜式空调","窗式空调","吊顶式空调","挂机空调","立式空调","空调挂机","柜机空调","家用空调","壁挂空调","变频空调","风管机","天花机","商用空调","空调"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201124] AND province:2 AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 11);
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

    //上海院校
    public void getYuanXiaoSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"食堂外包"};
        String[] bbb = {"上海交通大学","复旦大学","上海财经大学","上海理工大学","上海海事大学"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20150101 TO 20201130] AND province:24 AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 111);
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
            for (String bb : bbb) {
                String key = aa + "&" + bb;
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

    //勘探者科技
    public void getKanTanZheSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"油管设备","高压油管","油管机设备","油管液压机","液压管","输油管","回油管","低压管","石油管法兰自动焊机"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200501 TO 20201130] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 1);
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

    //中铁建设
    public void getZhongTieSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"中国建筑第三工程局","中建三局"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20191201 TO 20201130] AND province:12 AND progid:3 AND catid:[* TO 100] AND blZhongBiaoUnit:* AND blZhongBiaoUnit:\"" + aa + "\" ", key, 1);
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

    //蔚来之光
    public void getWeiLaiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"太阳能路灯","路灯灯杆","太阳能电池","太阳能电池板","太阳能景观灯","光伏电池","太阳能板","太阳能灯","太阳能电板","太阳能发电板"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200615 TO 20201203] AND progid:[0 TO 2] AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 1);
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

    //广汽汇理线下交付
    public void getGuangQiSolrAllField(String date) throws IOException {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] hhy = {"银行业监督管理","保险监督","证券监督","证监会","金融","外汇","经济委员会","经济局","经济服务","银监局","货币","经济管理","经济发展","人民币","保监局","银监会","保监会","经济运行","基金会","信用中心","银监分局","经济贸易","反洗钱","经信局","社会信用","证券业","经贸发展","期货","股票","经济合作","银行","分行","支行","央行","农商行","储蓄","证券","国债","交易所","保险经纪","保险股份","保险有限","保险公司","保险集团","中国人寿","保险责任","保险（集团","保险(集团","太平人寿","太平财险","人寿保险","人保财险","国联人寿","经济合作社","信用社","信用合作联社","经济联合社","联社","联合社","供销合作社","合作社","经济社","银联","信托","私募","基金","经济","经贸","资产管理","资产运营","资产经营","投资","融资","财务","信贷","贷款","资产发展","资本","资产","支付","结算","清算","理财","资金","信用卡","资管"};
        String[] aaa = {"软件","硬件","网络","系统","设备","智能","智慧","终端","主机","硬盘","平板","板卡","显卡","电源","电脑","光盘","磁盘","键盘","触屏","主屏","读卡","微机","手机","存储","镜头","屏幕","磁带","音控","音响","音箱","摄影","摄制","录音","播室","导播","配音","屏播","录播","VR","直播","视频","影音","音像","音视","试听","抖音","电纸","电子","数传","数码","数字","舆情","OA","智库","应答","银医","银校","页面","网阅","创课","主页","腾讯","搜狗","门户","官网","新浪","微信","微端","微博","头条","企微","快手","云鉴","云政","冀云","微云","云镜","云端","云勘","云图","警智","智拍","识别","算法","ＶＲ","AI","消息","邮件","钉钉","端口","队列","接口","杀软","显存"};
        List<String> bbb = LogUtils.readRule("smf");

        for (String aa : aaa) {
            for (String hy : hhy) {
                futureList1.add(executorService1.submit(() -> {
                    String key = aa + "&" + hy;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200921 TO 20200925] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND title:\"" + aa + "\" AND title:\"" + hy + "\"", key, null);
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
            for (String hy : hhy) {
                futureList1.add(executorService1.submit(() -> {
                    String key = bb + "&" + hy;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200921 TO 20200925] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + bb + "\" AND title:\"" + hy + "\"", key, null);
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

        for (String aa : aaa) {
            for (String hy : hhy) {
                futureList1.add(executorService1.submit(() -> {
                    String key = aa + "&" + hy;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200921 TO 20200925] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND title:\"" + aa + "\" AND zhaoBiaoUnit:\"" + hy + "\"", key, null);
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
            for (String hy : hhy) {
                futureList1.add(executorService1.submit(() -> {
                    String key = bb + "&" + hy;
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200921 TO 20200925] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + bb + "\" AND zhaoBiaoUnit:\"" + hy + "\"", key, null);
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
            for (String hy : hhy) {
                String key = aa + "&" + hy;
                arrayList.add(key);
            }
        }
        for (String bb : bbb) {
            for (String hy : hhy) {
                String key = bb + "&" + hy;
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
                futureList.add(executorService.submit(() -> {
                    try {
                        getDataFromZhongTaiAndSave8(content);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
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

    //晶元光电
    public void getJingYuanSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"LED显示屏","LED屏","电子屏","电子显示屏","LED大屏","LED显示器","全彩显示屏","LED液晶屏","高清LED","LED全彩显示屏","LED液晶显示屏","LED全彩屏","LED条屏","LED点阵","LED拼接屏","LED彩屏","LED高清显示屏","蓝光LED","大屏显示器","广视角显示器","会议屏","透明屏","舞台大屏幕","LED单元板","LED户外显示屏","LED广告屏","LED小间距显示屏","红光LED","LCD液晶拼接屏","LED单色屏","LED室内全彩屏","LED室内显示屏","LED诱导屏","LED彩色屏","LED走字屏","LED背景屏","LED户外大屏","LED双色屏","LED广告牌","LED透明屏","LED广告显示屏","LED室外显示屏","绿光LED","广场大屏幕","地砖屏","LED指示牌","LED广告机","LED防水屏","LED看板","LED地砖屏","LED弧形屏","LED文字屏","LED无缝拼接屏","LED格栅屏","LED舞台显示屏","LED舞台屏","LED三色屏","LED拼接墙","LED异形屏","LED租赁屏","LED玻璃屏","LED柔性屏","LED网格屏","LED互动地砖","展厅大屏幕","LED户外宣传屏","LED时光隧道","LED冰屏","LED会议大屏","LED车载显示屏","LED节能屏","LED走字显示屏","LED球形屏","LED智能海报屏","LED交通情报板","LED幕墙屏","LED高清透光屏","LED镜子屏","LED货架屏","LED六面屏","LED地毯屏","LED创意屏","LED海报屏","飘字屏幕","LED穹幕屏","酒店大屏幕","LED球屏","LED小条屏","LED交通屏","LED像素屏","LED门头滚动屏","LED橱窗屏","LED定制屏","LED展览屏","LED圆柱屏","LED智慧屏","LED阅报栏","LED灯杆屏","LED玻璃橱窗屏","LED瀑布屏","LED地板屏"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("ccAND province:25 AND progid:3 AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 1);
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



//        if (list != null && list.size() > 0) {
//            ExecutorService executorService = Executors.newFixedThreadPool(80);
//            List<Future> futureList = new ArrayList<>();
//            for (NoticeMQ content : list) {
//                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
//            }
//            for (Future future : futureList) {
//                try {
//                    future.get();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//            executorService.shutdown();
//        }
    }

    //万方
    public void getWanFangSolrAllField(String date) throws IOException {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"信息系统","信息化建设","信息平台","信息化系统","信息管理平台","公务用车信息化","信息服务平台","档案信息化","公司信息化","信息化升级","信息化改造","信息化工程","综合信息平台","信息化集成","信息化技术","智能信息化","企业信息化","档案数字化","数字档案","卷宗数字化","资料数字化","数字图书","数字阅读","馆藏数字化","图书馆数字化","文化馆数字化","数据库","大数据","数据中心","数据仓库","数据采集","数据分析","数据处理","数据应用","数据服务","数据安全","数据共享","数据整合","数据治理","数据交换","数据专线","数据备份","数据传输","数据加工","数据迁移","数据存储","数据质量","数据整理","数据接口","数据取证","数据挖掘","数据接入","数据监测","电子数据","基础数据","源数据","计算数据","公司数据","存量数据","监测数据","银联数据","银行数据","影像数据","商务数据","税务局数据","集团数据","政府信息化","法院信息化","法庭信息化","政务平台","公安局信息化","检察院信息化","监狱信息化","政务信息系统","监狱系统","司法局信息化","执法信息化","分局信息化","通用信息系统","信息化","数据化","信息集成","信息采集","数字化","信息传输","信息备份","数字系统","信息分析","信息处理","信息应用","信息服务","信息安全","信息共享","信息整合","信息治理","信息交换","信息专线","信息加工","信息迁移","信息存储","信息质量","信息整理","信息接口","信息挖掘","数字采集","数字分析","数字处理","数字应用","数字服务","数字安全","数字共享","数字整合","数字治理","数字交换","数字专线","数字备份","数字传输","数字加工","数字迁移","数字存储","数字质量","数字整理","数字接口","数字挖掘","数据源","数字化建设","数据化工程","信息化管理","大数据应用","信息技术应用","企业大数据","行业信息化","信息化管理平台","数字化管理","政府数字"};
        String[] bbb = {"数据","信息","数字"};
        String[] hhy = {"制造","智纬科技","通信","互联网","运营商","系统集成"};
        List<String> blacks = LogUtils.readRule("smf");

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201208] AND (province:24 OR province:30) AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, null);
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
                                String zhaoBiaoUnit = data.getZhaoBiaoUnit();
                                if (StringUtils.isNotBlank(zhaoBiaoUnit)){
                                    String industry = NewRuleUtils.getIndustry(zhaoBiaoUnit);
                                    if (StringUtils.isNotBlank(industry)){
                                        String[] split = industry.split("-");
                                        if (split.length == 2){
                                            for (String hy : hhy) {
                                                if (hy.equals(split[1]) || "政府机构".equals(split[0]) || zhaoBiaoUnit.contains("广电")){
                                                    String secondLevel = split[1];
                                                    data.setKeyword(split[0]);
                                                    data.setF(secondLevel);
                                                    data.setKeywordTerm(key);
                                                    list1.add(data);
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
                        }
                    }
                }
            }));
        }

        for (String bb : bbb) {
            futureList1.add(executorService1.submit(() -> {
                String key = bb ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201208] AND (province:24 OR province:30) AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND title:\"" + bb + "\" ", key, null);
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
                                String zhaoBiaoUnit = data.getZhaoBiaoUnit();
                                if (StringUtils.isNotBlank(zhaoBiaoUnit)){
                                    String industry = NewRuleUtils.getIndustry(zhaoBiaoUnit);
                                    if (StringUtils.isNotBlank(industry)){
                                        String[] split = industry.split("-");
                                        if (split.length == 2){
                                            for (String hy : hhy) {
                                                if (hy.equals(split[1]) || "政府机构".equals(split[0]) || zhaoBiaoUnit.contains("广电")){
                                                    String secondLevel = split[1];
                                                    data.setKeyword(split[0]);
                                                    data.setF(secondLevel);
                                                    data.setKeywordTerm(key);
                                                    list1.add(data);
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
        for (String bb : bbb) {
            String key = bb ;
            arrayList.add(key);
        }

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeywordTerm();
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

    //华为终端
    public void getHuaWeiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"电脑","平板","音频","PC机","计算机","可穿戴","IOT","物联网"};
        String[] bbb = {"智能音频","智能物联","智能平板","智能路由","智能电视","智能穿戴","智能白板","智慧音频","智慧物联","智慧平板","智慧电视","智慧白板","掌上计算机","掌上电脑","游戏本","一体台式机","一体机电脑","液晶平板电视","液晶电视","台式计算机","台式机","台式电脑","手提电脑","商务本","全面屏电视","苹果计算机","苹果电脑","苹果笔记本","苹果ipad","平板电脑","平板笔记本","品牌电脑","路由器","联想台式机","联想计算机","联想电脑","联想笔记本","可穿戴设备","计算机购置","计算机采购","机房电脑","惠普台式机","惠普平板","惠普计算机","惠普电脑","惠普笔记本","华为计算机","华为电脑","华为笔记本","华硕台式机","华硕平板","华硕计算机","华硕电脑","华硕笔记本","购置电脑","高性能移动计算机","高性能笔记本","高清电视","电子白板","电视一体机","电视设备","电视机","电视购置","电视采购","电脑主机","电脑整机","电脑一体机","电脑设备","电脑购置","电脑采购","电脑笔记本","戴尔台式机","戴尔平板","戴尔计算机","戴尔电脑","戴尔笔记本","打印设备","打印机","彩色电视","采购计算机","采购电视","采购电脑","便携式计算机","便携式电脑","便携计算机","便携电脑","笔记本电脑","办公计算机","办公电脑","macbookpro电脑","MacBook","Lenovo台式机","Lenovo计算机","Lenovo电脑","Lenovo笔记本","HP台式机","HP平板","HP计算机","HP电脑","HP笔记本","DELL台式机","DELL平板","DELL计算机","DELL电脑","DELL笔记本","ASUS台式机","ASUS平板","ASUS计算机","ASUS电脑","ASUS笔记本"};
        String[] blacks = {"平板拖把", "LED平板灯", "平板集装箱", "平板探测器"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201207 TO 20201213] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND title:\"" + aa + "\" ", key, 1);
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
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201207 TO 20201213] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + bb + "\" ", key, 2);
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

//
//        ArrayList<String> arrayList = new ArrayList<>();
//        for (String aa : aaa) {
//            String key = aa ;
//            arrayList.add(key);
//        }
//        for (String bb : bbb) {
//            String key = bb ;
//            arrayList.add(key);
//        }
//
//        for (String str : arrayList) {
//            int total = 0;
//            for (NoticeMQ noticeMQ : list) {
//                String keyword = noticeMQ.getKeyword();
//                if (keyword.equals(str)) {
//                    total++;
//                }
//            }
//            if (total == 0) {
//                continue;
//            }
//            System.out.println(str + ": " + total);
//        }
//        System.out.println("全部数据量：" + list1.size());
//        System.out.println("去重之后的数据量：" + list.size());

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

    //平安信息
    public void getPingAnXinxiSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"智慧水务","智慧水利"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20170101 TO 20201218] AND progid:3 AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 1);
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

    //华讯网络
    public void getHuaXunSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"口腔颌面锥形束计算机体层摄影设备","口腔X射线数字化体层摄影设备","口腔CBCT","锥形束CT","牙科X射线机","牙科影像板扫描仪","口腔X射线机","口腔光学扫描仪","口腔CT","牙科CT","口腔影像设备","牙科影像设备","口腔摄影器","牙科摄影器","大视野CBCT","口腔摄像机","口腔内窥镜数码相机","口腔数字观察仪","口腔数字印模仪","口腔全景曲面断层X线机","口内X线成像设备","口腔内窥镜","锥状束CT","牙科数字成像诊疗系统","口腔数字成像诊疗系统","口腔数字化影像设备","口腔X线影像设备","牙科X线影像设备","牙科CBCT","锥形束计算机体层摄影设备","锥形束投照计算机重组断层摄影设备","锥形束影像CT","口腔X射线数字化体层摄影系统","口腔颌面锥形束计算机体层摄影系统","口腔X线影像系统","牙科X线影像系统","口腔摄影系统","口内X线成像系统","牙科摄影系统","口腔数字化影像系统"};

        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20170101 TO 20201218] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + aa + "\" ", key, 2);
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

    //岛津企业
    public void getDaoJinSolrAllField(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aa1 = {"拍片机","医用X线机","高频X线机","X线摄影系统","医用诊断X光","高频X光机","摄影X光机","医用x光机","数字x光机","摄影X射线机","高频X射线摄影机","X射线拍片机","X线拍片机","X射线机","体检透视机","医学拍片","医用拍片","医疗拍片","拍片仪","拍片设备","床旁机","床边X线机","床旁X光机","移动X光机","移动DR","床边机","骨科小C","C型臂","C形臂","C臂","小C臂","介入C臂","影增C臂","移动式C形臂","骨科小型C","小型C臂","移动式C形臂X射线机","胃肠机","多功能X线机","遥控X线机","胃肠系统","遥控X光机","胃肠诊断","遥控医用诊断X射线机","X光透视拍片机","多功能数字化胃肠X线机","多功能数字化胃肠造影X光机","数字化X射线遥控透视摄影系统","数字化遥控胃肠X光机","数字胃肠","数字化透视摄影系统","数字多功能X光","平板多功能X线透视","动态平板透视摄影系统","动态平板","透视摄影X射线机","数字化透视摄影X射线机","胃肠X射线机","医用诊断X射线透视摄影系统","X射线胃肠诊断床","数字化透视X射线机","医用诊断X射线透视摄影系统","胃肠X射线机","医学透视摄影","医用透视摄影","医疗透视摄影","胃肠机","透视摄影仪","透视摄影机","透视摄影设备","x光透视机","透视X射线机","数字X","数字化X","平板X","平板摄影","平板摄片","直接X","X线数字","数字化X射线成像系统","平板DR","医用诊断X射线机","数字X线摄影","计算机X线摄影","动态DR","U臂DR","数字化医用X射线影像系统","悬吊DR","医学X光","医学X线","医学X射线","医学DR","医用X光","医用X线","医用X射线","医用DR","医疗X光","医疗X线","医疗X射线","医疗DR","X光设备","X线机","X线设备","X射线机","X射线设备","DR仪","DR机","DR设备","血管机组","血管机","血管造影","大C","大型血管介入治疗","外周血管造影机","大型血管介入治疗系统","大型心血管介入治疗系统","平板血管机","平板血管造影机","大型平板心血管介入治疗系统","直接转换型平板血管机","直接转换式平板血管造影机","DSA","数字减影","血管造影X射线机","数字X线血管机","血管造影设备","减影仪","减影机","减影设备","剪影血管造影仪","正电子发射型计算机断层显像","正电子发射断层成像设备","骨密度","X线双能量","骨密度仪","骨密度检测仪","双能量X线","双能X线","X射线骨密度仪","双能X射线骨密度仪","骨密度机","骨密度设备"};
        String[] aa2 = {"DR","PET","PET-CT","PET/CT","PETCT"};
        String[] blacks = {"口腔X射线","乳腺X射线","周口X射线","牙科x光机","车载X射线机","携带式X射线机","微型X射线机","牙科X射线机","乳腺X射线机","口腔X射线机","口腔全景X射线机","口腔颌面全景X射线机","口腔数字化体层摄影X射线机","口腔颌面锥形束计算机体层摄影设备","肢体数字化体层摄影X射线机","肢体锥形束计算机体层摄影设备","X射线放射治疗机","X射线放射治疗系统","体检机","口腔CBCT","牙科影像板","牙科X射线机","CBCT","泌尿X射线机","牙科CBCT","医用小型X光机","X射线摄影床","X射线摄影床","遥测监护系统","心电遥测系统","远程监护系统","中央监护系统","中央监护仪","数字化X射线影像处理软件","X射线平板探测器","X射线CCD探测器","X射线动态平板探测器","数字平板探测器成像系统","移动DR","乳腺数字化体层摄影X射线机","透视摄影X射线机","数字化透视摄影X射线机","医用诊断X射线透视摄影系统","乳腺X射线摄影系统","X射线影像计算机辅助诊断软件","X射线发生装置","X射线血管造影影像处理软件","血管内超声诊断系统","血管内超声诊断仪","体外冲击波心血管治疗系统","X射线计算机断层成像系统","X射线计算机体层摄影设备","X射线摄影用影像板成像装置","影像板扫描仪","X射线立体定向放射外科治疗系统","X射线放射治疗机","X射线放射治疗系统","超声骨密度仪","放射性核素骨密度仪"};
        String[] bbb = {"X光机","X射线"};


        for (String a1 : aa1) {
            futureList1.add(executorService1.submit(() -> {
                String key = a1 ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201231] AND catid:[* TO 100] AND allcontent:\"" + a1 + "\" ", key, 1);
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

        for (String a2 : aa2) {
            futureList1.add(executorService1.submit(() -> {
                String key = a2 ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201231] AND catid:[* TO 100] AND title:\"" + a2 + "\" ", key, 1);
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

        for (String bb : bbb) {
            futureList1.add(executorService1.submit(() -> {
                String key = bb ;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201231] AND catid:[* TO 100] AND allcontent:\"" + bb + "\" ", key, 2);
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

//        if (list != null && list.size() > 0) {
//            ExecutorService executorService = Executors.newFixedThreadPool(80);
//            List<Future> futureList = new ArrayList<>();
//            for (NoticeMQ content : list) {
//                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave6(content)));
//            }
//            for (Future future : futureList) {
//                try {
//                    future.get();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//            executorService.shutdown();
//        }
    }


}
















