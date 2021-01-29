package com.qianlima.offline.middleground;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.Area;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.YiDongMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.qianlima.offline.util.HttpClientUtil.getHttpClient;

@Service
@Slf4j
public class ICTLingYuService {

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    private YiDongMapping yiDongMapping;


    HashMap<Integer, Area> areaMap = new HashMap<>();

    //地区
    @PostConstruct
    public void init() throws IOException {

        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT * FROM phpcms_area");
        for (Map<String, Object> map : maps) {
            Area area = new Area();
            area.setAreaid(Integer.valueOf(map.get("areaid").toString()));
            area.setName(map.get("name").toString());
            area.setParentid(map.get("parentid").toString());
            area.setArrparentid(map.get("arrparentid").toString());
            areaMap.put(Integer.valueOf(map.get("areaid").toString()),area);
        }

        //获取所有领域关键词
        all = LogUtils.readRule("buLuRule/keyword");
        //先获取要去除的词
        heici = LogUtils.readRule("buLuRule/x");

        for (String s : heici) {
            if(s.contains("a:")){
                a.add(s.replace("a:",""));
            }else if(s.contains("b:")){
                b.add(s.replace("b:",""));
            }else if(s.contains("c:")){
                c.add(s.replace("c:",""));
            }else if(s.contains("d:")){
                d.add(s.replace("d:",""));
            }else if(s.contains("x:")){
                x.add(s.replace("x:",""));
            }else if(s.contains("x2:")){
                x2.add(s.replace("x2:",""));
            }
        }

        //b+c组合
        for (String s : b) {
            for (String s1 : c) {
                bc.add(s+s1);
            }
        }
    }

    public void byTitleAndZhaoLY() throws IOException {

//        List<Map> lists = new ArrayList<>();
//        Set<String> ids=new HashSet<>();
//        ClassPathResource classPathResource = new ClassPathResource("id/id.txt");
//        InputStream inputStream = classPathResource.getInputStream();
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
//        String line = bufferedReader.readLine();
//        while (StringUtils.isNotBlank(line)) {//BufferedReader有readLine()，可以实现按行读取
//            ids.add(line);
//            line = bufferedReader.readLine();
//        }
//        bufferedReader.close();
//        inputStream.close();

        List<Map<String, Object>> resultMaps = bdJdbcTemplate.queryForList(" SELECT id,content_id,baiLian_amount_unit,baiLian_budget,title,zhao_biao_unit FROM `loiloi_data` WHERE baiLian_budget >= 100000000 ");

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        List<Future> futureList = new ArrayList<>();
        for (Map<String, Object> resultMap : resultMaps) {
            futureList.add(executorService.submit(() -> {
                try {

//                    String id = resultMap.get("id") != null ? resultMap.get("id").toString() : "";
                    String id = resultMap.get("content_id") != null ? resultMap.get("content_id").toString() : "";
                    String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";
                    String zhaoBiao = resultMap.get("zhao_biao_unit") != null ? resultMap.get("zhao_biao_unit").toString() : "";
                    //中标金额 keyword
//                    String amount = resultMap.get("baiLian_amount_unit") != null ? resultMap.get("baiLian_amount_unit").toString() : "";
                    //招标预算 code
                    String amount = resultMap.get("baiLian_budget") != null ? resultMap.get("baiLian_budget").toString() : "";


                    String hyFirstIndustry =  null;
                    String hySecondIndustry =  null;

                    Map<String, String> stringStringMap = searchingHyAllData(zhaoBiao);
                    hyFirstIndustry =  stringStringMap.get("firstIndustry");
                    hySecondIndustry =  stringStringMap.get("secondIndustry");

                    bdJdbcTemplate.update("UPDATE loiloi_data set yldw = ?,S = ? where content_id = ?",hyFirstIndustry,hySecondIndustry,id);
                    log.info("contentId:{} =========== KA行业标签数据标注处理成功！！！ ",id);

                    String yes = "";

                    String[] date = {"201001","201002","201003","201004","201005","201006","201007","201008","201009","201010","201011","201012","201101","201102","201103","201104","201105","201106","201107","201108","201109","201110","201111","201112","201201","201202","201203","201204","201205","201206","201207","201208","201209","201210","201211","201212","201301","201302","201303","201304","201305","201306","201307","201308","201309","201310","201311","201312","201401","201402","201403","201404","201405","201406","201407","201408","201409","201410","201411","201412","201501","201502","201503","201504","201505","201506","201507","201508","201509","201510","201511","201512","201601","201602","201603","201604","201605","201606","201607","201608","201609","201610","201611","201612","201701","201702","201703","201704","201705","201706","201707","201708","201709","201710","201711","201712","201801","201802","201803","201804","201805","201806","201807","201808","201809","201810","201811","201812","201901","201902","201903","201904","201905","201906","201907","201908","201909","201910","201911","201912","202001","202002","202003","202004","202005","202006","202007","202008","202009","202010","202011","202012"};

                    String[] blacks = {"软件采购","硬件采购","设施采购","咨询服务","软件开发项目","设备项目","系统项目","系统建设","软件项目","硬件项目"};

                    String[] aaa = {"勘察设计","设计招标","设计服务","设计中标","项目设计","预算编制","初步设计","监理"};

                    //日期
                    boolean flags = true;
                    if (yes == "" && amount.length() != -1 && amount.length() >= 6){
                        for (String dd : date) {
                            if (amount.substring(0,6).equals(dd)){
                                yes = "N";
                                flags = false;
                                break;
                            }
                        }
                    }
                    if (yes == "" && StringUtils.isNotBlank(title)){
                        for (String aa : aaa) {
                            if (title.contains(aa)){
                                yes = "N";
                                flags = false;
                                break;
                            }
                        }
                    }
                    if (yes == "" && amount.contains(".")){
                        String substring = amount.substring(0, amount.indexOf("."));
                        if (substring.length() > 9){
                            yes = "N";
                        }
                    }else if (yes == "" && amount.length() > 9){
                        yes = "N";
                    }

//                    String[] splitkey = keys.split("::");
//                    String id = splitkey[0];
//                    String title = splitkey[1];
//                    String zhaoBiao = splitkey.length == 3?splitkey[2] : null;

                    String contentAbstract = "";

                    if (title != null) {
                        //1：数据准备工作
                        //检索括号，中括号，引号(中文和英文)，最后删除其它的符号，比如：，.。:
                        title = titleHandle(title);
                        contentAbstract = titleHandle(contentAbstract);
                        //将标题中含有 AI 但是前后有字母的删掉
                        title = checkString(title);
                        contentAbstract = checkString(contentAbstract);

                        //2：黑词过滤+场景词过滤
                        //黑词过滤
                        for (String s : x) {
                            if (title.contains(s)) {
                                title = title.replace(s, "");
                            }
                            if (contentAbstract.contains(s)) {
                                contentAbstract = contentAbstract.replace(s, "");
                            }
                        }
                        //a：检索”场景词过滤“文件中的“关键词a” 判断后面是否紧跟”关键词d“
                        for (String s : a) {
                            //标题过滤
                            if (title.contains(s)) {
                                boolean flag = false;
                                List<String> ad = new ArrayList<>();
                                //a+d组合
                                for (String s1 : d) {
                                    ad.add(s + s1);
                                }

                                for (String s1 : ad) {
                                    if (title.contains(s1)) {
                                        flag = true;
                                        break;
                                    }
                                }
                                if (flag == false) {
                                    title = title.replace(s, "");
                                }
                            }
                            //标的物过滤
                            if (contentAbstract.contains(s)) {
                                boolean flag = false;
                                List<String> ad = new ArrayList<>();
                                //a+d组合
                                for (String s1 : d) {
                                    ad.add(s + s1);
                                }

                                for (String s1 : ad) {
                                    if (contentAbstract.contains(s1)) {
                                        flag = true;
                                        break;
                                    }
                                }
                                if (flag == false) {
                                    contentAbstract = contentAbstract.replace(s, "");
                                }
                            }
                        }

                        //b：检索("关键词b"+"关键词c"),对其进行隐藏；
                        for (String s : bc) {
                            if (title.contains(s)) {
                                title = title.replace(s, "");
                            }
                            if (contentAbstract.contains(s)) {
                                contentAbstract = contentAbstract.replace(s, "");
                            }
                        }
                    }


                    //3：标签处理 + 分类处理
                    StringBuilder getKeyword = new StringBuilder();
                    StringBuilder   getLabelOne = new StringBuilder();
                    StringBuilder getLabelTwo = new StringBuilder();

                    // 一级领域
                    String getClassifyOne = null;
                    // 二级领域
                    String getClassifyTwo = null;


                    for (String s : all) {
                        String[] split = s.split(":");
                        //关键词
                        String keyword = split[0];
                        //二级标签(二级分类)
                        String label = split[1];
                        //领域，（一级标签，一级分类）
                        String field  = split[2];
                        //标题匹配
                        if(title.contains(keyword)){
                            getKeyword.append("标题："+keyword+"，");
                            if(!field.equals("信息化系统/平台") && !field.equals("智能/智慧项目")){
                                //不等于 "信息化平台" 和 "智能智慧人工智能" 的领域走正常流程
                                //分类匹配（一对一）
                                if(getClassifyOne == null && getClassifyTwo == null){
                                    getClassifyOne = field;
                                    getClassifyTwo = label;
                                }
                                //标签匹配（多对多）
                                if(StringUtils.isBlank(getLabelOne)){
                                    getLabelOne.append(field);
                                    getLabelTwo.append(label);
                                }else{
                                    if (getLabelOne.toString().contains(field)) {
                                        if(StringUtils.isNotBlank(getLabelTwo) && !getLabelTwo.toString().contains(label)){
                                            getLabelTwo.append(","+label);
                                        }
                                    }else{
                                        getLabelOne.append(";"+field);
                                        getLabelTwo.append(";"+label);
                                    }
                                }
                            }else{
                                //等于的话判断是否是特殊分类
                                if(label.equals("信息化行业分类")){
                                    //获取先号行业
                                    String industryJson = getIndustry(zhaoBiao);
                                    String industry =  null;
                                    if (StringUtils.isNotBlank(industryJson)) {
                                        JSONObject jsonObject =  JSON.parseObject(industryJson);
                                        JSONObject data1 = jsonObject.getJSONObject("data");
                                        if(data1 != null){
                                            industry =  data1.getString("firstLevel");
                                        }
                                        if(StringUtils.isBlank(industry) || industry.equals("行业待分类")){
                                            industry = "通用信息化";
                                        }else{
                                            if(industry.equals("政府机构")){
                                                industry = "政务信息化";
                                            }else if(industry.equals("教育单位")){
                                                industry = "教育信息化";
                                            }else if(industry.equals("医疗单位")){
                                                industry = "医疗信息化";
                                            }else if(industry.equals("金融企业")){
                                                industry = "金融信息化";
                                            }else {
                                                industry = "通用信息化";
                                            }
                                        }
                                    }else{
                                        industry = "通用信息化";
                                    }

                                    if(getClassifyOne == null && getClassifyTwo == null){
                                        getClassifyOne = field;
                                        getClassifyTwo = industry;
                                    }

                                    //标签匹配（多对多）
                                    if(StringUtils.isBlank(getLabelOne)){
                                        getLabelOne.append(field);
                                        getLabelTwo.append(industry);
                                    }else{
                                        if (getLabelOne.toString().contains(field)) {
                                            if (!getLabelTwo.toString().contains(industry)) {
                                                getLabelTwo.append(","+industry);
                                            }
                                        }else{
                                            getLabelOne.append(";"+field);
                                            getLabelTwo.append(";"+industry);
                                        }
                                    }
                                }else if(label.equals("智慧")){
                                    //获取先号行业
                                    String industryJson = getIndustry(zhaoBiao);
                                    String industry =  null;
                                    if (StringUtils.isNotBlank(industryJson)) {
                                        JSONObject jsonObject =  JSON.parseObject(industryJson);
                                        JSONObject data1 = jsonObject.getJSONObject("data");
                                        if(data1 != null){
                                            industry =  data1.getString("firstLevel");
                                        }
                                        if(StringUtils.isBlank(industry) || industry.equals("行业待分类")){
                                            industry = "智慧城市";
                                        }else {
                                            if(industry.equals("政府机构")){
                                                industry = "智慧政务";
                                            }else if(industry.equals("教育单位")){
                                                industry = "智慧教育";
                                            }else if(industry.equals("医疗单位")){
                                                industry = "智慧医疗";
                                            }else if(industry.equals("金融企业")){
                                                industry = "智慧金融";
                                            }else {
                                                industry = "智慧城市";
                                            }
                                        }
                                    }else {
                                        industry = "智慧城市";
                                    }
                                    if(getClassifyOne == null && getClassifyTwo == null){
                                        getClassifyOne = field;
                                        getClassifyTwo = industry;
                                    }
                                    //标签匹配（多对多）
                                    if(StringUtils.isBlank(getLabelOne)){
                                        getLabelOne.append(field);
                                        getLabelTwo.append(industry);
                                    }else{
                                        if (getLabelOne.toString().contains(field)) {
                                            if (!getLabelTwo.toString().contains(industry)) {
                                                getLabelTwo.append(","+industry);
                                            }
                                        }else{
                                            getLabelOne.append(";"+field);
                                            getLabelTwo.append(";"+industry);
                                        }
                                    }
                                }else{
                                    //不等于 "信息化行业分类" 和 "智慧" 的领域走正常流程
                                    //分类匹配（一对一）
                                    if(getClassifyOne == null && getClassifyTwo == null){
                                        getClassifyOne = field;
                                        getClassifyTwo = label;
                                    }
                                    //标签匹配（多对多）
                                    if(StringUtils.isBlank(getLabelOne)){
                                        getLabelOne.append(field);
                                        getLabelTwo.append(label);
                                    }else{
                                        if (getLabelOne.toString().contains(field)) {
                                            if(StringUtils.isNotBlank(getLabelTwo) && !getLabelTwo.toString().contains(label)){
                                                getLabelTwo.append(","+label);
                                            }
                                        }else{
                                            getLabelOne.append(";"+field);
                                            getLabelTwo.append(";"+label);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (StringUtils.isBlank(getLabelOne) && StringUtils.isNotBlank(contentAbstract)) {
                        for (String s : all) {
                            String[] split = s.split(":");
                            //关键词
                            String keyword = split[0];
                            //二级标签(二级分类)
                            String label = split[1];
                            //领域，（一级标签，一级分类）
                            String field  = split[2];
                            //标题匹配
                            if(contentAbstract.contains(keyword)){
                                getKeyword.append("标的物："+keyword+"，");
                                if(!field.equals("信息化系统/平台") && !field.equals("智能/智慧项目")){
                                    //不等于 "信息化平台" 和 "智能智慧人工智能" 的领域走正常流程
                                    //分类匹配（一对一）
                                    if(getClassifyOne == null && getClassifyTwo == null){
                                        getClassifyOne = field;
                                        getClassifyTwo = label;
                                    }
                                    //标签匹配（多对多）
                                    if(StringUtils.isBlank(getLabelOne)){
                                        getLabelOne.append(field);
                                        getLabelTwo.append(label);
                                    }else{
                                        if (getLabelOne.toString().contains(field)) {
                                            if(StringUtils.isNotBlank(getLabelTwo) && !getLabelTwo.toString().contains(label)){
                                                getLabelTwo.append(","+label);
                                            }
                                        }else{
                                            getLabelOne.append(";"+field);
                                            getLabelTwo.append(";"+label);
                                        }
                                    }
                                }else{
                                    //等于的话判断是否是特殊分类
                                    if(label.equals("信息化行业分类")){
                                        //获取先号行业
                                        String industryJson = getIndustry(zhaoBiao);
                                        String industry =  null;
                                        if (StringUtils.isNotBlank(industryJson)) {
                                            JSONObject jsonObject =  JSON.parseObject(industryJson);
                                            JSONObject data1 = jsonObject.getJSONObject("data");
                                            if(data1 != null){
                                                industry =  data1.getString("firstLevel");
                                            }
                                            if(StringUtils.isBlank(industry) || industry.equals("行业待分类")){
                                                industry = "通用信息化";
                                            }else{
                                                if(industry.equals("政府机构")){
                                                    industry = "政务信息化";
                                                }else if(industry.equals("教育单位")){
                                                    industry = "教育信息化";
                                                }else if(industry.equals("医疗单位")){
                                                    industry = "医疗信息化";
                                                }else if(industry.equals("金融企业")){
                                                    industry = "金融信息化";
                                                }else {
                                                    industry = "通用信息化";
                                                }
                                            }
                                        }else{
                                            industry = "通用信息化";
                                        }

                                        if(getClassifyOne == null && getClassifyTwo == null){
                                            getClassifyOne = field;
                                            getClassifyTwo = industry;
                                        }

                                        //标签匹配（多对多）
                                        if(StringUtils.isBlank(getLabelOne)){
                                            getLabelOne.append(field);
                                            getLabelTwo.append(industry);
                                        }else{
                                            if (getLabelOne.toString().contains(field)) {
                                                if (!getLabelTwo.toString().contains(industry)) {
                                                    getLabelTwo.append(","+industry);
                                                }
                                            }else{
                                                getLabelOne.append(";"+field);
                                                getLabelTwo.append(";"+industry);
                                            }
                                        }
                                    }else if(label.equals("智慧")){
                                        //获取先号行业
                                        String industryJson = getIndustry(zhaoBiao);
                                        String industry =  null;
                                        if (StringUtils.isNotBlank(industryJson)) {
                                            JSONObject jsonObject =  JSON.parseObject(industryJson);
                                            JSONObject data1 = jsonObject.getJSONObject("data");
                                            if(data1 != null){
                                                industry =  data1.getString("firstLevel");
                                            }
                                            if(StringUtils.isBlank(industry) || industry.equals("行业待分类")){
                                                industry = "智慧城市";
                                            }else {
                                                if(industry.equals("政府机构")){
                                                    industry = "智慧政务";
                                                }else if(industry.equals("教育单位")){
                                                    industry = "智慧教育";
                                                }else if(industry.equals("医疗单位")){
                                                    industry = "智慧医疗";
                                                }else if(industry.equals("金融企业")){
                                                    industry = "智慧金融";
                                                }else {
                                                    industry = "智慧城市";
                                                }
                                            }
                                        }else {
                                            industry = "智慧城市";
                                        }
                                        if(getClassifyOne == null && getClassifyTwo == null){
                                            getClassifyOne = field;
                                            getClassifyTwo = industry;
                                        }
                                        //标签匹配（多对多）
                                        if(StringUtils.isBlank(getLabelOne)){
                                            getLabelOne.append(field);
                                            getLabelTwo.append(industry);
                                        }else{
                                            if (getLabelOne.toString().contains(field)) {
                                                if (!getLabelTwo.toString().contains(industry)) {
                                                    getLabelTwo.append(","+industry);
                                                }
                                            }else{
                                                getLabelOne.append(";"+field);
                                                getLabelTwo.append(";"+industry);
                                            }
                                        }
                                    }else{
                                        //不等于 "信息化行业分类" 和 "智慧" 的领域走正常流程
                                        //分类匹配（一对一）
                                        if(getClassifyOne == null && getClassifyTwo == null){
                                            getClassifyOne = field;
                                            getClassifyTwo = label;
                                        }
                                        //标签匹配（多对多）
                                        if(StringUtils.isBlank(getLabelOne)){
                                            getLabelOne.append(field);
                                            getLabelTwo.append(label);
                                        }else{
                                            if (getLabelOne.toString().contains(field)) {
                                                if(StringUtils.isNotBlank(getLabelTwo) && !getLabelTwo.toString().contains(label)){
                                                    getLabelTwo.append(","+label);
                                                }
                                            }else{
                                                getLabelOne.append(";"+field);
                                                getLabelTwo.append(";"+label);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(StringUtils.isNotBlank(getLabelOne)){
                        String industryJson = getIndustry(zhaoBiao);
                        String kaFirstLevelIndustry =  null;
                        String kaSecondLevelIndustry =  null;
                        if (StringUtils.isNotBlank(industryJson)) {
                            JSONObject jsonObject = JSON.parseObject(industryJson);
                            JSONObject data1 = jsonObject.getJSONObject("data");
                            if (data1 != null) {
                                kaFirstLevelIndustry = data1.getString("firstLevel");
                                kaSecondLevelIndustry = data1.getString("secondLevel");
                            }
                        }

                        String ydFirstLevelIndustry =  null;
                        String ydSecondLevelIndustry =  null;
                        if(StringUtils.isNotBlank(kaFirstLevelIndustry) && StringUtils.isNotBlank(kaSecondLevelIndustry)){
                            Map<String, String> zhaoBiaoUnit = yiDongMapping.getYiDongIndustry(kaFirstLevelIndustry, kaSecondLevelIndustry, zhaoBiao);
                            ydFirstLevelIndustry = zhaoBiaoUnit.get("firstIndustry");
                            ydSecondLevelIndustry = zhaoBiaoUnit.get("secondIndustry");
                        }

                        bdJdbcTemplate.update("UPDATE loiloi_data set syjg = ?,sygs = ? where content_id = ?",getClassifyOne,getClassifyTwo,id);
                        log.info("contentId:{} =========== 领域数据标注处理成功！！！ ",id);


                        if (yes == "" && amount.contains(".")){
                            String substring = amount.substring(0, amount.indexOf("."));
                            if (substring.length() == 9){
                                if (hyFirstIndustry.equals("商业公司")){
                                    if (getClassifyOne.equals("视频应用和媒体") || getClassifyOne.equals("智能/智慧项目") || getClassifyOne.equals("数据/数据库") || getClassifyOne.equals("信息化系统/平台")){
                                        yes = "Y";
                                    }else if (getClassifyOne.equals("云计算类项目")) {
                                        for (String black : blacks) {
                                            if (StringUtils.isNotBlank(title) && title.contains(black)) {
                                                yes = "N";
                                                flags = false;
                                                break;
                                            } else {
                                                yes = "Y";
                                            }
                                        }
                                    }else if (getClassifyOne.equals("硬件/运维服务") && hySecondIndustry.equals("轨道交通")) {
                                        yes = "Y";
                                    }else {
                                        yes = "N";
                                    }
                                }
                                if (hyFirstIndustry.equals("政府机构")){
                                    if (getClassifyOne.equals("云计算类项目") || getClassifyOne.equals("视频应用和媒体") || getClassifyOne.equals("智能/智慧项目")){
                                        yes = "Y";
                                    }else if (getClassifyOne.equals("数据/数据库") || getClassifyOne.equals("数字化产品") || getClassifyOne.equals("信息化系统/平台")){
                                        yes = "Y";
                                    }else {
                                        yes = "N";
                                    }
                                }
                                if (hyFirstIndustry.equals("教育单位")){
                                    if (getClassifyOne.equals("智能/智慧项目")){
                                        yes = "Y";
                                    }else {
                                        yes = "N";
                                    }
                                }
                                if (hyFirstIndustry.equals("金融企业")){
                                    if ((getClassifyOne.equals("视频应用和媒体") && title.contains("雪亮工程")) || getClassifyOne.equals("智能/智慧项目")){
                                        yes = "Y";
                                    }else {
                                        yes = "N";
                                    }
                                }
                                if (hyFirstIndustry.equals("医疗单位")){
                                    yes = "N";
                                }
                            }else if (substring.length() > 9){
                                yes = "N";
                            }
                        }else if(yes == "" && amount.length() == 9) {
                            if (hyFirstIndustry.equals("商业公司")){
                                if (getClassifyOne.equals("视频应用和媒体") || getClassifyOne.equals("智能/智慧项目") || getClassifyOne.equals("数据/数据库") || getClassifyOne.equals("信息化系统/平台")){
                                    yes = "Y";
                                }else if (getClassifyOne.equals("云计算类项目")) {
                                    for (String black : blacks) {
                                        if (StringUtils.isNotBlank(title) && title.contains(black)) {
                                            yes = "N";
                                            flags = false;
                                            break;
                                        } else {
                                            yes = "Y";
                                        }
                                    }
                                }else if (getClassifyOne.equals("硬件/运维服务") && hySecondIndustry.equals("轨道交通")) {
                                    yes = "Y";
                                }else {
                                    yes = "N";
                                }
                            }
                            if (hyFirstIndustry.equals("政府机构")){
                                if (getClassifyOne.equals("云计算类项目") || getClassifyOne.equals("视频应用和媒体") || getClassifyOne.equals("智能/智慧项目")){
                                    yes = "Y";
                                }else if (getClassifyOne.equals("数据/数据库") || getClassifyOne.equals("数字化产品") || getClassifyOne.equals("信息化系统/平台")){
                                    yes = "Y";
                                }else {
                                    yes = "N";
                                }
                            }
                            if (hyFirstIndustry.equals("教育单位")){
                                if (getClassifyOne.equals("智能/智慧项目")){
                                    yes = "Y";
                                }else {
                                    yes = "N";
                                }
                            }
                            if (hyFirstIndustry.equals("金融企业")){
                                if ((getClassifyOne.equals("视频应用和媒体") && title.contains("雪亮工程")) || getClassifyOne.equals("智能/智慧项目")){
                                    yes = "Y";
                                }else {
                                    yes = "N";
                                }
                            }
                            if (hyFirstIndustry.equals("医疗单位")){
                                yes = "N";
                            }
                        }else if(yes== "" || amount.length() > 9){
                            yes = "N";
                        }
                        bdJdbcTemplate.update("UPDATE loiloi_data set code = ? where content_id = ?",yes,id);
                        log.info("contentId:{} =========== 大金额数据标记疑似处理成功！！！ ",id);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }));
        }

        for (Future future1 : futureList) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();

        log.info("全部数据跑完了~~~~~~~~~~~");
    }



    //领域关键词
    List<String> all = new ArrayList<>();
    //要去除的词
    List<String> heici = new ArrayList<>();
    //具体关键词
    List<String> a = new ArrayList<>();
    List<String> b = new ArrayList<>();
    List<String> c = new ArrayList<>();
    List<String> d = new ArrayList<>();
    List<String> x = new ArrayList<>();
    List<String> x2 = new ArrayList<>();
    List<String> bc = new ArrayList<>();

    /**
     * 判断是否是字母
     * @param str 传入字符串
     * @return 是字母返回true，否则返回false
     */
    public String checkString(String str) {
        str = str.toUpperCase();
        String[] keywords = new String[]{"AI","OA","VR","APP","IDC","ICT","MIS","VPN","OCR"};
        for (String keyword : keywords) {
            boolean flag = true;
            int key = str.indexOf(keyword);
            if(key != -1){
                if(key != 0){
                    String substring1 = str.substring(key - 1, key);
                    Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
                    Matcher m = p.matcher(substring1);
                    if (m.find() == false) flag = false;
                }
                if(key+keyword.length() <= str.length()){
                    String substring1 = str.substring(key+keyword.length(), key+keyword.length()+1);
                    Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
                    Matcher m = p.matcher(substring1);
                    if (m.find() == false) flag = false;
                }
            }
            if(flag == false){
                str = str.replace(keyword.toUpperCase(),"");
            }
        }
        return str.toUpperCase();
    }

    String[] diqv = new String[]{
            "安徽","北京","福建","甘肃","广东","广西","贵州","海南","河北","河南","黑龙江","湖北","湖南","吉林","江苏","江西","辽宁","内蒙古","宁夏","青海","山东","山西","陕西","上海","四川","天津","西藏","新疆","云南","全国","浙江","重庆","安庆","蚌埠","巢湖","池州","滁州","阜阳","合肥","淮北","淮南","黄山","六安","马鞍山","宿州","铜陵","芜湖","宣城","亳州","北京","福州","龙岩","南平","宁德","莆田","泉州","三明","厦门","漳州","白银","定西","嘉峪关","金昌","酒泉","兰州","陇南","平凉","庆阳","天水","武威","张掖","潮州","东莞","佛山","广州","河源","惠州","江门","揭阳","茂名","梅州","清远","汕头","汕尾","韶关","深圳","阳江","云浮","湛江","肇庆","中山","珠海","百色","北海","崇左","防城港","桂林","贵港","河池","贺州","来宾","柳州","南宁","钦州","梧州","玉林","安顺","毕节","贵阳","六盘水","铜仁","遵义","澄迈县","定安县","东方","海口","临高县","琼海","三亚","屯昌县","万宁","文昌","五指山","儋州","保定","沧州","承德","邯郸","衡水","廊坊","秦皇岛","石家庄","唐山","邢台","张家口","安阳","鹤壁","济源","焦作","开封","洛阳","南阳","平顶山","三门峡","商丘","新乡","信阳","许昌","郑州","周口","驻马店","漯河","濮阳","大庆","大兴安岭","哈尔滨","鹤岗","黑河","鸡西","佳木斯","牡丹江","七台河","齐齐哈尔","双鸭山","绥化","伊春","鄂州","黄冈","黄石","荆门","荆州","潜江","神农架林区","十堰","随州","天门","武汉","仙桃","咸宁","襄阳","孝感","宜昌","常德","长沙","郴州","衡阳","怀化","娄底","邵阳","湘潭","益阳","永州","岳阳","张家界","株洲","白城","白山","长春","吉林","辽源","四平","松原","通化","常州","淮安","连云港","南京","南通","苏州","宿迁","泰州","无锡","徐州","盐城","扬州","镇江","抚州","赣州","吉安","景德镇","九江","南昌","萍乡","上饶","新余","宜春","鹰潭","鞍山","本溪","朝阳","大连","丹东","抚顺","阜新","葫芦岛","锦州","辽阳","盘锦","沈阳","铁岭","营口","阿拉善盟","巴彦淖尔盟","包头","赤峰","鄂尔多斯","呼和浩特","呼伦贝尔","通辽","乌海","乌兰察布盟","锡林郭勒盟","兴安盟","固原","石嘴山","吴忠","银川","海东","西宁","滨州","德州","东营","菏泽","济南","济宁","莱芜","聊城","临沂","青岛","日照","泰安","威海","潍坊","烟台","枣庄","淄博","长治","大同","晋城","晋中","临汾","吕梁","朔州","太原","忻州","阳泉","运城","安康","宝鸡","汉中","商洛","铜川","渭南","西安","咸阳","延安","榆林","上海","巴中","成都","达州","德阳","广安","广元","乐山","眉山","绵阳","南充","内江","攀枝花","遂宁","雅安","宜宾","资阳","自贡","泸州","天津","阿里","昌都","拉萨","林芝","那曲","日喀则","山南","阿克苏","阿拉尔","哈密","和田","喀什","克拉玛依","石河子","图木舒克","吐鲁番","乌鲁木齐","五家渠","保山","昆明","丽江","临沧","曲靖","思茅","玉溪","昭通","杭州","湖州","嘉兴","金华","丽水","宁波","绍兴","台州","温州","舟山","衢州","重庆","中卫","沙坡头区","安庆市","蚌埠市","巢湖市","池州市","滁州市","阜阳市","合肥市","淮北市","淮南市","黄山市","六安市","马鞍山市","宿州市","铜陵市","芜湖市","宣城市","亳州市","北京市","福州市","龙岩市","南平市","宁德市","莆田市","泉州市","三明市","厦门市","漳州市","白银市","定西市","嘉峪关市","金昌市","酒泉市","兰州市","陇南市","平凉市","庆阳市","天水市","武威市","张掖市","潮州市","东莞市","佛山市","广州市","河源市","惠州市","江门市","揭阳市","茂名市","梅州市","清远市","汕头市","汕尾市","韶关市","深圳市","阳江市","云浮市","湛江市","肇庆市","中山市","珠海市","百色市","北海市","崇左市","防城港市","桂林市","贵港市","河池市","贺州市","来宾市","柳州市","南宁市","钦州市","梧州市","玉林市","安顺市","毕节市","贵阳市","六盘水市","铜仁市","遵义市","澄迈县市","定安县市","东方市","海口市","临高县市","琼海市","三亚市","屯昌县市","万宁市","文昌市","五指山市","儋州市","保定市","沧州市","承德市","邯郸市","衡水市","廊坊市","秦皇岛市","石家庄市","唐山市","邢台市","张家口市","安阳市","鹤壁市","济源市","焦作市","开封市","洛阳市","南阳市","平顶山市","三门峡市","商丘市","新乡市","信阳市","许昌市","郑州市","周口市","驻马店市","漯河市","濮阳市","大庆市","大兴安岭市","哈尔滨市","鹤岗市","黑河市","鸡西市","佳木斯市","牡丹江市","七台河市","齐齐哈尔市","双鸭山市","绥化市","伊春市","鄂州市","黄冈市","黄石市","荆门市","荆州市","潜江市","神农架林区市","十堰市","随州市","天门市","武汉市","仙桃市","咸宁市","襄阳市","孝感市","宜昌市","常德市","长沙市","郴州市","衡阳市","怀化市","娄底市","邵阳市","湘潭市","益阳市","永州市","岳阳市","张家界市","株洲市","白城市","白山市","长春市","吉林市","辽源市","四平市","松原市","通化市","常州市","淮安市","连云港市","南京市","南通市","苏州市","宿迁市","泰州市","无锡市","徐州市","盐城市","扬州市","镇江市","抚州市","赣州市","吉安市","景德镇市","九江市","南昌市","萍乡市","上饶市","新余市","宜春市","鹰潭市","鞍山市","本溪市","朝阳市","大连市","丹东市","抚顺市","阜新市","葫芦岛市","锦州市","辽阳市","盘锦市","沈阳市","铁岭市","营口市","阿拉善盟市","巴彦淖尔盟市","包头市","赤峰市","鄂尔多斯市","呼和浩特市","呼伦贝尔市","通辽市","乌海市","乌兰察布盟市","锡林郭勒盟市","兴安盟市","固原市","石嘴山市","吴忠市","银川市","海东市","西宁市","滨州市","德州市","东营市","菏泽市","济南市","济宁市","莱芜市","聊城市","临沂市","青岛市","日照市","泰安市","威海市","潍坊市","烟台市","枣庄市","淄博市","长治市","大同市","晋城市","晋中市","临汾市","吕梁市","朔州市","太原市","忻州市","阳泉市","运城市","安康市","宝鸡市","汉中市","商洛市","铜川市","渭南市","西安市","咸阳市","延安市","榆林市","上海市","巴中市","成都市","达州市","德阳市","广安市","广元市","乐山市","眉山市","绵阳市","南充市","内江市","攀枝花市","遂宁市","雅安市","宜宾市","资阳市","自贡市","泸州市","天津市","阿里市","昌都市","拉萨市","林芝市","那曲市","日喀则市","山南市","阿克苏市","阿拉尔市","哈密市","和田市","喀什市","克拉玛依市","石河子市","图木舒克市","吐鲁番市","乌鲁木齐市","五家渠市","保山市","昆明市","丽江市","临沧市","曲靖市","思茅市","玉溪市","昭通市","杭州市","湖州市","嘉兴市","金华市","丽水市","宁波市","绍兴市","台州市","温州市","舟山市","衢州市","重庆市","中卫市","沙坡头区市"
    };

    /**
     * 断括号中的内容是否为地域名称(省级),如(北京)(中国)"深圳"
     */
    public String titleHandle(String title){
        if(title.contains("(") && title.contains(")")){
            for (String s : diqv) {
                String key = "("+s+")";
                if(title.contains(key)){
                    title = title.replace(key,"");
                }else{
                    title = title.replace("(","").replace(")","");
                }
            }
        }

        if(title.contains("（") && title.contains("）")){
            for (String s : diqv) {
                String key = "（"+s+"）";
                if(title.contains(key)){
                    title = title.replace(key,"");
                }else{
                    title = title.replace("（","").replace("）","");
                }
            }
        }

        if(title.contains("“") && title.contains("”")){
            for (String s : diqv) {
                String key = "“"+s+"”";
                if(title.contains(key)){
                    title = title.replace(key,"");
                }else{
                    title = title.replace("“","").replace("”","");
                }
            }
        }

        title = title.replace(",","").replace(".","").replace("。","")
                .replace("、","").replace(":","").replace("|","")
                .replace("{","").replace("}","").replace("：","");
        return title;
    }


    //获取先号行业数据
    public String getIndustry(String str) {
        String result = null;

        //创建默认的httpClient实例
        CloseableHttpClient httpClient = getHttpClient();
        try {
            //用get方法发送http请求
            HttpPost post = new HttpPost("http://cusdata.qianlima.com/api/ka/industry");

            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("unit", str));
            // 构造一个form表单式的实体
            post.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
            CloseableHttpResponse httpResponse = null;
            //发送请求
            httpResponse = httpClient.execute(post);
            try {
                //response实体
                HttpEntity entity = httpResponse.getEntity();
                if (null != entity) {
                    result = EntityUtils.toString(entity);
                }
            } finally {
                httpResponse.close();
            }
        } catch (Exception e) {
            return null;
        }
        return result;
    }


    //KA自用行业___根据contentid匹配行业标签

    private Map<String, String> searchingHyAllData(String zhaobiaounit) throws IOException {

        HashMap<String, String> map = new HashMap<>();

        HttpClient client = new DefaultHttpClient();
        HttpResponse response = null;
        // --KA自用行业
        // http://cusdata.qianlima.com/api/ka/industry?unit=上海市公安局国际机场分局
        String url = "http://cusdata.qianlima.com/api/ka/industry?unit="+zhaobiaounit+"";
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");

        response = client.execute(post);
        String ret = null;
        ret = EntityUtils.toString(response.getEntity(), "UTF-8");

        JSONObject parseObject= JSON.parseObject(ret);
        JSONObject data = parseObject.getJSONObject("data");
        String firstLevel = data.getString("firstLevel");
        String secondLevel = data.getString("secondLevel");
        if (firstLevel != "" && firstLevel != null){
            map.put("firstIndustry", firstLevel);
            map.put("secondIndustry", secondLevel);
        }

        return map;
    }

}
















