package com.qianlima.offline.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class WeierliEnviRule {


    private static List<String> wordKeys = new ArrayList<>(); //组合词
    private static List<String> x = new ArrayList<>(); //黑词

    private static String[] wordKey = {"餐厨", "餐厨垃圾", "厨余", "工业废水", "渗滤液", "市政污水", "脱硫废水"};
    private static String[] xs = {"拍卖", "物业", "收运", "运营", "施工", "设计", "勘察", "监理", "收集", "清运", "标识", "印刷", "防水PVC", "垃圾桶", "流标", "废标", "中止", "服务外包", "垃圾车", "垃圾箱", "环卫车", "后勤管理", "土方", "土地", "餐厨车", "供餐", "餐饮", "安装", "维护", "维保", "维修", "厨具", "奖品", "保洁", "调试布袋", "车辆", "垃圾分类", "餐厨用具", "转运", "运输", "餐厅", "食堂", "就地处理", "装运", "渗透膜", "分拣站", "故障", "储柜", "安置点", "管线铺设", "编制", "风险评估", "外运", "箱涵", "现场管理", "测量服务", "密集柜", "装载机", "防尘网", "叉车", "劳务", "修理", "脱水机", "药剂", "罐体", "代理机构", "评价服务", "土建", "合同", "排污证", "所得税", "绿化", "地勘", "垃圾袋", "场地平整", "路线迁改", "租赁", "检测服务", "整改", "报告书", "保证金", "委托检测", "财务", "排放监测", "资格预审", "九阳豆浆机", "搅拌器", "风机修复", "运维费", "家具", "白蚁防治", "检修", "消防验收", "大修", "井盖", "路灯", "竣工"};

    public static void initWordComb() {
        if (wordKeys.size() < 1) {
            wordKeys.addAll(Arrays.asList(wordKey));
            x.addAll(Arrays.asList(xs));
        }
    }

    /**
     * 步骤	判断以上步骤的数据标题是否包含"x"词,是则标识并删除该数据，最终产出数据为维尔利环保规则数据
     */
    public static String ruleVerification(String title, String infoId) {
        String resultTag = "";
        if (StringUtils.isBlank(title) || "无".equals(title)) {
            return resultTag;
        }
        title = title.toUpperCase();
        boolean contains = false;

        //1. （标题精准匹配"关键词"）
        for (String s : wordKeys) {
            contains = title.contains(s);
            if (contains == true) {
                resultTag = s;
                break;
            }
        }
        //2. 条件成立后，判断该数据的标题中是否包含黑词
        if (contains == true) {
            for (String s : x) {
                contains = title.contains(s);
                if (contains == true) {
                    resultTag = "";
                    log.info("维尔利环保规则匹配失败，info_id：{}，全文中匹配到了黑词", infoId);
                    break;
                }
            }
            if (contains == false) {
                log.info("维尔利环保规则匹配成功，info_id：{}，该条数据入库", infoId);
            }
        }
        return resultTag;
    }
}
