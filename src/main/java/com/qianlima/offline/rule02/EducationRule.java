package com.qianlima.offline.rule02;

import com.qianlima.offline.bean.ConstantBean;
import org.apache.commons.lang3.StringUtils;

public class EducationRule {


    private static String[] a = { "气象学校", "商务学校", "经济学校", "医药学校", "士官学校", "金融学校", "财贸学校", "成人高等学校",  "大学", "学院", "艺校", "军校", "医校", "党校", "职校", "技校", "联校", "团校", "体校", "院校", "师范", "专科学校", "大专", "本科",  "专升本", "理科", "文科", "医科", "高专", "预科", "理工",  "高职", "高校", "职业学校", "职业技术学校", "技工学校", "电力学校", "音乐学校", "美术学校", "舞蹈学校", "邮电学校", "干部学校", "外国语学校", "工程学校", "工业学校", "财经学校", "警察学校", "警察训练学校", "旅游学校", "旅游管理学校", "商贸学校", "师范学校", "民航学校", "文化技术学校", "水电学校", "税务学校", "财政学校", "运输学校", "会计学校", "贸易学校", "商业学校", "交通学校", "林业学校", "信息学校", "物资学校", "公共事业学校", "工贸学校", "高级技术学校", "桥梁学校", "财经学校", "建设学校", "管理学校", "农业学校", "畜牧学校", "机电学校", "科技学校", "机械学校", "艺术学校", "化工学校", "专业学校", "护士学校", "卫生学校", "护理学校"};
    private static String[] b = { "中等职业", "中专", "中职", "中等专业", "职专", "中学", "实高", "实中", "高中", "初中", "职高", "职中", "附中"};
    private static String[] c = { "小学", "附小", "完小", "实小", "文小"};
    private static String[] d = { "幼儿园", "托儿所", "分园"};
    private static String[] e = { "职教中心", "教育中心", "自学考试", "成人教育", "远程教育", "专修", "培训中心", "进修", "学习", "教育基地"};
    private static String[] f = { "学校", "校园", "中心校", "分校", "校区", "学区"};

    private static String[] StrArrayName = { "教育单位-大学", "教育单位-中学", "教育单位-小学", "教育单位-幼儿园", "教育单位-培训", "教育单位-学校" };

    private static String checkA(String company) {

        String[] otherBlacks = { "附属", "医院", "科学院", "门诊"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                return "";
            }
        }

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

        String[] otherBlacks = {  "医院", "门诊" };
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                return "";
            }
        }

        String result = "";

        if (company.endsWith("中")){
            result = StrArrayName[1]+ ConstantBean.RULE_SEPARATOR;
        }

        if (StringUtils.isBlank(result)){
            for (String str : b) {
                if (company.contains(str)){
                    result = StrArrayName[1]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkC(String company) {

        String[] otherBlacks = {  "医院", "门诊" };
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                return "";
            }
        }
        String result = "";

        if (company.endsWith("小")){
            result = StrArrayName[2]+ ConstantBean.RULE_SEPARATOR;
        }
        if (StringUtils.isBlank(result)){
            for (String str : c) {
                if (company.contains(str)){
                    result = StrArrayName[2]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkD(String company) {

        String[] otherBlacks = {  "医院", "门诊" };
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                return "";
            }
        }

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

        String[] otherBlacks = {  "医院", "门诊" };
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                return "";
            }
        }
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

        String[] otherBlacks = {  "医院", "门诊" };
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                return "";
            }
        }

        String result = "";
        for (String str : f) {
            if (company.contains(str)){
                result = StrArrayName[5]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }
        return result;
    }

    /**
     * 执行事业机关规则
     */
    public static String ruleVerification(String company) {

        String resultTag = checkA(company) + checkB(company)+ checkC(company)+ checkD(company) + checkE(company);

        if (StringUtils.isBlank(resultTag)){
            resultTag = checkF(company);
        }

        if (StringUtils.isNotBlank(resultTag)){
            resultTag = resultTag.substring(0, resultTag.length() - 1);
        }

        return resultTag;
    }
}
