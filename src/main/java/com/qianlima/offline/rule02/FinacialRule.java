package com.qianlima.offline.rule02;

import com.qianlima.offline.bean.ConstantBean;
import org.apache.commons.lang3.StringUtils;

public class FinacialRule {

    private static String[] a = { "银行", "分行", "支行", "央行", "农商行", "储蓄"};
    private static String[] b = { "期货", "股票", "证券", "国债", "外汇", "交易所"};
    private static String[] c = { "国联人寿", "人保财险", "人寿保险", "保险经纪", "保险股份", "保险有限", "保险公司", "保险集团", "中国人寿", "保险责任", "保险（集团", "保险(集团", "太平人寿", "太平财险"};
    private static String[] d = { "信托", "私募", "基金"};
    private static String[] e = { "经济合作社", "信用社", "信用合作联社", "经济联合社", "联社", "联合社", "供销合作社", "合作社", "经济社", "银联"};
    private static String[] f = { "资产", "支付", "结算", "清算", "理财", "资金", "信用卡", "资产管理", "资产运营", "资产经营", "投资", "融资", "财务", "信贷", "贷款", "资本", "资产发展", "金融", "资管"};

    private static String[] StrArrayName = {"金融企业-银行", "金融企业-证券", "金融企业-保险", "金融企业-信托", "金融企业-合作社", "金融企业-资本运作" };

    private static String checkA(String company) {
        String result = "";
        for (String str : a) {
            if (company.contains(str)){
                result = StrArrayName[0]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }
        if (StringUtils.isNotBlank(result)){
            if (company.endsWith("监管分局")){
                result = "";
            }
        }
        return result;
    }

    private static String checkB(String company) {

        String[] otherBlacks = { "证券犯罪"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                return "";
            }
        }


        String result = "";
        boolean flag = false;
        String[] commonBlacks = { "公司", "股份", "集团", "所"};
        for (String commonBlack : commonBlacks) {
            if (company.contains(commonBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
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
        boolean flag = false;
        String[] commonBlacks = { "公司", "股份", "集团"};
        for (String commonBlack : commonBlacks) {
            if (company.contains(commonBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : d) {
                if (company.contains(str)){
                    result = StrArrayName[3]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
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
        boolean flag = false;
        String[] commonBlacks = { "公司", "股份", "集团"};
        for (String commonBlack : commonBlacks) {
            if (company.contains(commonBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : f) {
                if (company.contains(str)){
                    result = StrArrayName[5]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
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
