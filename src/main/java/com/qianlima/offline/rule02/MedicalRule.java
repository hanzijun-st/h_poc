package com.qianlima.offline.rule02;

import com.qianlima.offline.bean.ConstantBean;
import org.apache.commons.lang3.StringUtils;

public class MedicalRule {

    private static String[] a = { "血站", "献血", "血液中心", "血液管理"};
    private static String[] b = { "急救中心", "紧急医疗", "120急救", "急救医疗", "医疗急救"};
    private static String[] c = { "疾控", "疾病预防", "防治院", "预防控制", "检疫站", "疾病控制", "病防治", "抗癌"};
    private static String[] d = { "卫生院", "社区卫生", "卫生服务", "卫计服务", "医疗中心", "公共卫生", "卫生保健", "精神卫生", "卫生中心"};
    private static String[] e = { "疗养院", "康复中心", "康复医院", "休养所", "疗养医院"};
    private static String[] f = { "幼保健院", "心血管病", "妇婴保健院", "妇幼保健院", "儿童医院", "病医院", "肿瘤医院", "科医院", "妇婴医院", "肛肠医院", "口腔医院", "妇幼保健医院", "精神病医院", "精神病院"};
    private static String[] g = { "中医院", "中医医院"};
    private static String[] h = { "综合医院", "中西结合医院", "中西医结合医院", "医院", "门诊"};

    private static String[] StrArrayName = { "医疗单位-血站", "医疗单位-急救中心", "医疗单位-疾控中心", "医疗单位-卫生院", "医疗单位-疗养院", "医疗单位-专科医院", "医疗单位-中医院", "医疗单位-综合医院" };

    private static String checkA(String company) {
        String result = "";
        for (String str : a) {
            if (company.contains(str)){
                result = StrArrayName[0]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }
        return result;
    }

    private static String checkB(String company) {
        String result = "";
        for (String str : b) {
            if (company.contains(str)){
                result = StrArrayName[1]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }
        return result;
    }

    private static String checkC(String company) {
        String result = "";
        for (String str : c) {
            if (company.contains(str)){
                result = StrArrayName[2]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }
        return result;
    }

    private static String checkD(String company) {
        String result = "";
        for (String str : d) {
            if (company.contains(str)){
                result = StrArrayName[3]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }
        return result;
    }

    private static String checkE(String company) {
        String result = "";
        for (String str : e) {
            if (company.contains(str)){
                result = StrArrayName[4]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }
        return result;
    }

    private static String checkF(String company) {
        String result = "";
        for (String str : f) {
            if (company.contains(str)){
                result = StrArrayName[5]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }
        return result;
    }

    private static String checkG(String company) {
        String result = "";
        for (String str : g) {
            if (company.contains(str)){
                result = StrArrayName[6]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }
        return result;
    }

    private static String checkH(String company) {
        String result = "";
        for (String str : h) {
            if (company.contains(str)){
                result = StrArrayName[7]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }
        return result;
    }

    /**
     * 执行事业机关规则
     */
    public static String ruleVerification(String company) {

        String resultTag = checkA(company) + checkB(company)+ checkC(company)+ checkD(company) + checkE(company)+ checkF(company)+ checkG(company);

        if (StringUtils.isBlank(resultTag)){
            resultTag = checkH(company);
        }

        if (StringUtils.isNotBlank(resultTag)){
            resultTag = resultTag.substring(0, resultTag.length() - 1);
        }

        return resultTag;
    }

}
