package com.qianlima.offline.util;

import com.qianlima.offline.bean.ConstantBean;
import com.qianlima.offline.bean.UserTagEnum;
import com.qianlima.offline.bean.WordPack;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class PICCRule {

    @Resource
    private MongoTemplate mongoTemplate;

    @Autowired
    private TagsUtil tagsUtil;

    public static List<String> a1 = new ArrayList<>();
    public static List<String> a11 = new ArrayList<>();
    public static List<String> b1 = new ArrayList<>();
    public static List<String> b11 = new ArrayList<>();
    public static List<String> c1 = new ArrayList<>();
    public static List<String> c11 = new ArrayList<>();
    public static List<String> d1 = new ArrayList<>();
    public static List<String> d11 = new ArrayList<>();
    public static List<String> e1 = new ArrayList<>();
    public static List<String> e11 = new ArrayList<>();
    public static List<String> f1 = new ArrayList<>();
    public static List<String> f11 = new ArrayList<>();
    public static List<String> g1 = new ArrayList<>();
    public static List<String> g11 = new ArrayList<>();
    public static List<String> h1 = new ArrayList<>();
    public static List<String> h11 = new ArrayList<>();
    public static List<String> i1 = new ArrayList<>();
    public static List<String> i11 = new ArrayList<>();
    public static List<String> j11 = new ArrayList<>();
    public static List<String> common = new ArrayList<>(); //通用词
    public static List<String> x1 = new ArrayList<>(); //黑词
    public static List<String> x2 = new ArrayList<>(); //黑词
    public static List<String> y1 = new ArrayList<>(); //黑词
    public static List<String> z1 = new ArrayList<>();
    public static List<String> tags = new ArrayList<>();

    private String[] o = {"001400010001", "001400010002", "001400010003", "001400020001", "001400020002", "001400020003", "001400020004", "001400020005", "001400020006", "001400020007", "00070001", "00070002", "00070003", "00070004", "00070005", "00070006", "00070007", "00070008", "00070009", "00070010", "00070011"};

    //初始化关键词
    public void initWordComb() {
        if (x1.size() < 1) {
            synchronized (this) {
                if (x1.size() < 1) {
                    log.info("初始化人保财规则");
                    a1.addAll(getKeyWord("a1"));
                    a11.addAll(getKeyWord("a11"));
                    b1.addAll(getKeyWord("b1"));
                    b11.addAll(getKeyWord("b11"));
                    c1.addAll(getKeyWord("c1"));
                    c11.addAll(getKeyWord("c11"));
                    d1.addAll(getKeyWord("d1"));
                    d11.addAll(getKeyWord("d11"));
                    e1.addAll(getKeyWord("e1"));
                    e11.addAll(getKeyWord("e11"));
                    f1.addAll(getKeyWord("f1"));
                    f11.addAll(getKeyWord("f11"));
                    g1.addAll(getKeyWord("g1"));
                    g11.addAll(getKeyWord("g11"));
                    h1.addAll(getKeyWord("h1"));
                    h11.addAll(getKeyWord("h11"));
                    i1.addAll(getKeyWord("i1"));
                    i11.addAll(getKeyWord("i11"));
                    j11.addAll(getKeyWord("j11"));
                    common.addAll(getKeyWord("m1"));
                    x1.addAll(getKeyWord("p1"));
                    x2.addAll(getKeyWord("p2"));
                    y1.addAll(getKeyWord("y1"));
                    z1.addAll(getKeyWord("z1"));
                    tags.addAll(Arrays.asList(o));
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
        queryObject.put("tag_id", "15,16,17,18,19,20,23,24,25,26,27,28");
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
     * 人保财-财产险
     */
    private String checkChaiChanXian(String title) {
        String result = "";
        boolean flag = true;
        for (String a : a1) {
            if (title.contains(a)) {
                result = UserTagEnum.PICC_CAICHANXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }
        if (flag) {
            out:
            for (String a : a11) {
                for (String s : common) {
                    if (title.contains(a + s)) {
                        result = UserTagEnum.PICC_CAICHANXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                        break out;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 人保财-工程险
     */
    private String checkGongChengXian(String title) {
        String result = "";
        boolean flag = true;
        for (String a : b1) {
            if (title.contains(a)) {
                result = UserTagEnum.PICC_GONGCHENGXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }
        if (flag) {
            out:
            for (String a : b11) {
                for (String s : common) {
                    if (title.contains(a + s)) {
                        result = UserTagEnum.PICC_GONGCHENGXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                        break out;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 人保财-货运险
     */
    private String checkHuoYunXian(String title) {
        String result = "";
        boolean flag = true;
        for (String a : c1) {
            if (title.contains(a)) {
                result = UserTagEnum.PICC_HUOYUNXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }
        if (flag) {
            out:
            for (String a : c11) {
                for (String s : common) {
                    if (title.contains(a + s)) {
                        result = UserTagEnum.PICC_HUOYUNXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                        break out;
                    }
                }
            }
        }
        return result;
    }


    /**
     * 人保财-船舶险
     */
    private String checkChuanBoXian(String title) {
        String result = "";
        boolean flag = true;
        for (String a : d1) {
            if (title.contains(a)) {
                result = UserTagEnum.PICC_CHUANBOXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }
        if (flag) {
            out:
            for (String a : d11) {
                for (String s : common) {
                    if (title.contains(a + s)) {
                        result = UserTagEnum.PICC_CHUANBOXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                        break out;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 人保财-责任险
     */
    private String checkZeRenXian(String title) {
        String result = "";
        boolean flag = true;
        for (String a : e1) {
            if (title.contains(a)) {
                result = UserTagEnum.PICC_ZERENXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }
        if (flag) {
            out:
            for (String a : e11) {
                for (String s : common) {
                    if (title.contains(a + s)) {
                        result = UserTagEnum.PICC_ZERENXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                        break out;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 人保财-信用保证险
     */
    private String checkXinYongBaoZhengXian(String title) {
        String result = "";
        boolean flag = true;
        for (String a : f1) {
            if (title.contains(a)) {
                result = UserTagEnum.PICC_XINYONGBAOZHENGXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }
        if (flag) {
            out:
            for (String a : f11) {
                for (String s : common) {
                    if (title.contains(a + s)) {
                        result = UserTagEnum.PICC_XINYONGBAOZHENGXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                        break out;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 人保财-农险
     */
    private String checkNongXian(String title) {
        String result = "";
        boolean flag = true;
        for (String a : g1) {
            if (title.contains(a)) {
                result = UserTagEnum.PICC_NONGXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }
        if (flag) {
            out:
            for (String a : g11) {
                for (String s : common) {
                    if (title.contains(a + s)) {
                        result = UserTagEnum.PICC_NONGXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                        break out;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 人保财-意外健康险
     */
    private String checkYiWaiJianKangXian(String title) {
        String result = "";
        boolean flag = true;
        for (String a : h1) {
            if (title.contains(a)) {
                result = UserTagEnum.PICC_YIWAIJIANKANGXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }
        if (flag) {
            out:
            for (String a : h11) {
                for (String s : common) {
                    if (title.contains(a + s)) {
                        result = UserTagEnum.PICC_YIWAIJIANKANGXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                        break out;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 人保财-车险
     */
    private String checkCheXian(String title) {
        String result = "";
        boolean flag = true;
        for (String a : i1) {
            if (title.contains(a)) {
                result = UserTagEnum.PICC_CHEXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }
        if (flag) {
            out:
            for (String a : i11) {
                for (String s : common) {
                    if (title.contains(a + s)) {
                        result = UserTagEnum.PICC_CHEXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                        break out;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 人保财-其他保险
     */
    private String checkQiTaBaoXian(String title) {
        String result = "";
        out:
        for (String a : j11) {
            for (String s : common) {
                if (title.contains(a + s)) {
                    result = UserTagEnum.PICC_QITABAOXIAN.getMessage() + ConstantBean.RULE_SEPARATOR;
                    break out;
                }
            }
        }
        return result;
    }


    /**
     * 人保财-服务
     */
    private String checkFuWu(String title) {
        String result = "";
        for (String a : z1) {
            if (title.contains(a)) {
                result = UserTagEnum.PICC_FUWU.getMessage() + ConstantBean.RULE_SEPARATOR;
                break;
            }
        }
        return result;
    }

    /**
     * 人保财-工程建筑
     */
    private String checkGongChengJianZhu(String title) {
        String result = "";
        for (String a : y1) {
            if (title.contains(a)) {
                result = UserTagEnum.PICC_GONGCHENGJIANZHU.getMessage() + ConstantBean.RULE_SEPARATOR;
                break;
            }
        }
        if (StringUtils.isNotBlank(result)) {
            for (String s : x2) {
                if (title.contains(s)) {
                    result = "";
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 人保财执行规则
     */
    public String ruleVerification(String title, String infoId, boolean checkStatus) {
        boolean result = true;
        String resultTag = "";
        if (StringUtils.isBlank(title) || "无".equals(title) || title.length() <= 5) {
            return resultTag;
        }
        title = title.toUpperCase();
        // 获取词包标签(优先匹配：财产险、工程险、货运险、船舶险、责任险、信用保证险、农险、意外健康险、车险), 前面的标签都匹配不到, 匹配最后一个标签
        resultTag = checkChaiChanXian(title) + checkGongChengXian(title) + checkHuoYunXian(title) + checkChuanBoXian(title) + checkZeRenXian(title) + checkXinYongBaoZhengXian(title) + checkNongXian(title) + checkYiWaiJianKangXian(title) + checkCheXian(title);
        if (StringUtils.isBlank(resultTag)) {
            resultTag = checkQiTaBaoXian(title);
        }
        // 如果符合保险规格对应的词包数据, 需要判断对应的数据标题是否包含黑词
        if (StringUtils.isNotBlank(resultTag)) {
            resultTag = resultTag.substring(0, resultTag.length() - 1);
            for (String s : x1) {
                if (title.contains(s)) {
                    log.info("人保财规则对应的保险标签匹配失败，info_id：{}，结果:{} 全文中匹配到了黑词:{}", infoId, resultTag, s);
                    result = false;
                    resultTag = "";
                    break;
                }
            }
            if (result) {
                log.info("人保财规则匹配成功，info_id：{}，结果:{} 该条数据入库", infoId, resultTag);
                return resultTag;
            }
        }
        // 判断工程建筑、服务类标签数据（需要先获取中台接口, 得到对应的标签列表）
        if (StringUtils.isBlank(resultTag) || result == false) {
            boolean flag = false;
            resultTag += checkFuWu(title);
            // 工程建筑类数据，需获取标签
            List<String> tagList = tagsUtil.getTagsByContentId(infoId, checkStatus);
            if (tagList != null && tagList.size() > 0) {
                for (String tag : tags) {
                    if (tagList.contains(tag)) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    resultTag += checkGongChengJianZhu(title);
                }
            }
            if (StringUtils.isNotBlank(resultTag)) {
                resultTag = resultTag.substring(0, resultTag.length() - 1);
                return resultTag;
            }
        }
        log.info("人保财规则匹配失败，info_id：{}", infoId);
        return resultTag;
    }

}
