package com.qianlima.offline.rule02;

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
public class KeDaXunFeiRule {
    @Resource
    private MongoTemplate mongoTemplate;

    public static List<String> a1 = new ArrayList<>();
    public static List<String> a2 = new ArrayList<>();
    public static List<String> a3 = new ArrayList<>();
    public static List<String> a4 = new ArrayList<>();
    public static List<String> a5 = new ArrayList<>();
    public static List<String> x1 = new ArrayList<>(); //黑词


    //初始化关键词A 以及相关词组组合
    public void initWordComb() {
        if (x1.size() < 1) {
            synchronized (this) {
                if (x1.size() < 1) {
                    log.info("初始化科大讯飞规则");
                    a1.addAll(getKeyWord("a1"));
                    a2.addAll(getKeyWord("a2"));
                    a3.addAll(getKeyWord("a3"));
                    a4.addAll(getKeyWord("a4"));
                    a5.addAll(getKeyWord("a5"));
                    x1.addAll(getKeyWord("x1"));
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
        queryObject.put("tag_id", "2,3,4,5,6");
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
     * ICT-教育
     */
    private String checkJiaoYu(String title) {
        String result = "";
        for (String a : a1) {
            if (title.contains(a)) {
                result = a ;
                break ;
            }
        }
        return result;
    }

    /**
     * ICT-医疗
     */
    private String checkYiLiao(String title) {
        String result = "";
        for (String a : a2) {
            if (title.contains(a)) {
                result = a ;
                break ;
            }
        }
        return result;
    }

    /**
     * ICT-政法
     */
    private String checkZhengFa(String title) {
        String result = "";
        for (String a : a3) {
            if (title.contains(a)) {
                result = a ;
                break ;
            }
        }
        return result;
    }

    /**
     * ICT-听见
     */
    private String checkTingJian(String title) {
        String result = "";
        for (String a : a4) {
            if (title.contains(a)) {
                result = a ;
                break ;
            }
        }
        return result;
    }

    /**
     * ICT-智慧城市
     */
    private String checkZhiHuiCity(String title) {
        String result = "";
        for (String a : a5) {
            if (title.contains(a)) {
                result = a ;
                break ;
            }
        }
        return result;
    }

    /**
     * 科大讯飞执行规则
     */
    public String ruleVerification(String title, String infoId) {
        String resultTag = "";
        if (StringUtils.isBlank(title) || "无".equals(title) || title.length() <= 5) {
            return resultTag;
        }
        title = title.toUpperCase();

        String jiaoYu = checkJiaoYu(title);
        if (StringUtils.isNotBlank(jiaoYu)) {
            resultTag += jiaoYu + "&";
        }
        String yiLiao = checkYiLiao(title);
        if (StringUtils.isNotBlank(yiLiao)) {
            resultTag += yiLiao + "&";
        }
        String zhengFa = checkZhengFa(title);
        if (StringUtils.isNotBlank(zhengFa)) {
            resultTag += zhengFa + "&";
        }
        String tingJian = checkTingJian(title);
        if (StringUtils.isNotBlank(tingJian)) {
            resultTag += tingJian + "&";
        }
        String zhiHuiCity = checkZhiHuiCity(title);
        if (StringUtils.isNotBlank(zhiHuiCity)) {
            resultTag += zhiHuiCity + "&";
        }
        boolean result = true;

        if (StringUtils.isNotBlank(resultTag)) {
            // 判断标题中是否包含黑词
            resultTag = resultTag.substring(0, resultTag.length() - 1);
            for (String s : x1) {
                if (title.contains(s)) {
                    log.info("科大讯飞规则匹配失败，info_id：{}，结果:{} 全文中匹配到了黑词:{}", infoId, resultTag, s);
                    result = false;
                    resultTag = "";
                    break;
                }
            }
            if (result) {
                log.info("科大讯飞规则匹配成功，info_id：{}，结果:{} 该条数据入库", infoId, resultTag);
            }
        } else {
            log.info("科大讯飞规则匹配失败，info_id：{}", infoId);
        }
        return resultTag;
    }


}
