package com.qianlima.offline.util;

import com.alibaba.fastjson.JSONArray;
import com.qianlima.offline.bean.WordPack;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FanShengZiRule {

    @Resource
    private MongoTemplate mongoTemplate;

    public static List<String> a = new ArrayList<>();
    public static List<String> b = new ArrayList<>();
    public static List<String> c = new ArrayList<>();
    public static List<String> d = new ArrayList<>();

    //初始化关键词A 以及黑词
    public void initWordComb() {
        if (a.size() < 1) {
            synchronized (this) {
                if (a.size() < 1) {
                    log.info("初始化泛生子基因规则");
                    a.addAll(getKeyWord("a1"));
                    b.addAll(getKeyWord("b1"));
                    c.addAll(getKeyWord("c1"));
                    d.addAll(getKeyWord("d1"));
                }
            }
        }
    }

    public List<String> getKeyWord(String name) {
        List<String> list = new ArrayList<>();

        Document fieldsObject = new Document();
        fieldsObject.put("_id", false);
        fieldsObject.put("keyword", true);

        Document queryObject = new Document();
        queryObject.put("name", name);
        queryObject.put("tag_id", "11,12,13");
        Query query = new BasicQuery(queryObject, fieldsObject);
        List<WordPack> wordPacks = mongoTemplate.find(query, WordPack.class);
        for (WordPack wordPack : wordPacks) {
            if (wordPack != null && StringUtils.isNotBlank(wordPack.getKeyword())) {
                list.add(wordPack.getKeyword().toUpperCase());
            }
        }
        return list;
    }

    /**
     * 泛生子基因-数字PCR(全文检索关键词 a)
     */
    private String checkFigureRpc(String contentAndTitle) {
        boolean contains = false;
        for (String s : a) {
            if (contentAndTitle.contains(s)){
                contains = true;
                break;
            }
        }
        if (contains == true) {
            return "泛生子基因-数字PCR";
        }
        return "";
    }

    /**
     * 泛生子基因-基因测序仪(全文检索关键词 b)
     */
    private String checkJiYinCeXuYi(String contentAndTitle) {
        boolean contains = false;
        for (String s : b) {
            if (contentAndTitle.contains(s)){
                contains = true;
                break;
            }
        }
        if (contains == true) {
            return "泛生子基因-基因测序仪";
        }
        return "";
    }

    /**
     * 泛生子基因-基因突变检测试剂盒(全文检索关键词 c OR 中标单位检索 d 词)
     */
    private String checkJiYinTuBianJanCeHe(String contentAndTitle, String zhongBiaoUnit) {
        boolean contains = false;
        for (String s : c) {
            if (contentAndTitle.contains(s)){
                contains = true;
                break;
            }
        }
        if (contains == false){
            for (String s : d) {
                if (zhongBiaoUnit.contains(s)){
                    contains = true;
                    break;
                }
            }
        }
        if (contains == true) {
            return "泛生子基因-基因突变检测试剂盒";
        }
        return "";
    }


    public String ruleVerification(String title, String content, JSONArray zhongBiaoUnit, String infoId) {

        String resultTag = "";
        String zhongbiaoUnitStr = "";
        if (StringUtils.isBlank(title) || "无".equals(title) || StringUtils.isBlank(content)) {
            return resultTag;
        }
        //多个招标单位时，此处也需要改为多个。
        if (zhongBiaoUnit != null && zhongBiaoUnit.size() > 0) {
            for (int i = 0; i < zhongBiaoUnit.size(); i++) {
                zhongbiaoUnitStr += zhongBiaoUnit.getString(i);
                zhongbiaoUnitStr += "&";
            }
        }
        content = content + "&" + title; //将内容和标题融合
        content = content.toUpperCase();
        zhongbiaoUnitStr = zhongbiaoUnitStr.toUpperCase();

        // 数字PCR(全文检索关键词 a)
        String tagOne = checkFigureRpc(content);
        if (StringUtils.isNotBlank(tagOne)) {
            resultTag += tagOne;
            resultTag += "&";
        }
        // 基因测序仪(全文检索关键词 b)
        String tagTwo = checkJiYinCeXuYi(content);
        if (StringUtils.isNotBlank(tagTwo)) {
            resultTag += tagTwo;
            resultTag += "&";
        }
        // 基因突变检测试剂盒(全文检索关键词 c OR 中标单位检索 d 词)
        String tagThree = checkJiYinTuBianJanCeHe(content, zhongbiaoUnitStr);
        if (StringUtils.isNotBlank(tagThree)) {
            resultTag += tagThree;
            resultTag += "&";
        }

        if (StringUtils.isNotBlank(resultTag)){
            resultTag = resultTag.substring(0, resultTag.length() - 1);
            log.info("泛生子基因规则匹配成功，info_id：{}，结果:{} 该条数据入库", infoId, resultTag);
        } else {
            log.info("泛生子基因规则匹配失败，info_id：{}", infoId);
        }
        return resultTag;
    }
}
