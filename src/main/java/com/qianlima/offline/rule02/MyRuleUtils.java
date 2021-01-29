package com.qianlima.offline.rule02;

import com.qianlima.offline.bean.ConstantBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyRuleUtils {

    private static final String[] P0 = { "政府机构-教育", "教育单位-大学", "教育单位-中学", "教育单位-小学", "教育单位-幼儿园", "教育单位-培训"};
    private static final String[] P1 = { "教育单位-学校"};
    private static final String[] P2 = { "政府机构-医疗","医疗单位-血站", "医疗单位-急救中心", "医疗单位-疾控中心", "医疗单位-卫生院", "医疗单位-疗养院", "医疗单位-专科医院", "医疗单位-中医院"};
    private static final String[] P3 = { "医疗单位-综合医院"};
    private static final String[] P4 = { "政府机构-金融", "政府机构-工业和信息化", "金融企业-银行", "金融企业-证券", "金融企业-保险", "金融企业-信托", "金融企业-合作社", "政府机构-市场监督", "政府机构-应急管理"};
    private static final String[] P5 = { "政府机构-水利水电", "政府机构-国防", "政府机构-公安", "政府机构-检法司", "政府机构-政法委", "政府机构-纪委", "政府机构-宣传", "政府机构-组织", "政府机构-海关", "政府机构-外交", "政府机构-发展和改革",  "政府机构-民族事务", "政府机构-人力资源和社会保障", "政府机构-税务", "政府机构-民政", "政府机构-市政", "政府机构-生态环境", "政府机构-气象", "政府机构-能源", "政府机构-交通运输", "政府机构-文化和旅游"};
    private static final String[] P6 = { "政府机构-财政", "政府机构-农业农村", "政府机构-自然资源", "政府机构-住房和城乡建设"};
    private static final String[] P7 = {  "政府机构-科学技术"};
    private static final String[] P8 = { "政府机构-地方政务"};
    private static final String[] P9 = { "政府机构-其他", "商业公司-石油化工"};
    private static final String[] P10 = { "商业公司-采矿", "商业公司-电力", "商业公司-电气", "商业公司-燃气热力", "商业公司-水利", "商业公司-管网", "商业公司-新能源", "商业公司-物流仓储", "商业公司-机场港口", "商业公司-轨道交通"};
    private static final String[] P11 = { "商业公司-烟草", "商业公司-广电", "商业公司-传媒"};
    private static final String[] P12 = { "商业公司-城市交通", "商业公司-制造", "商业公司-零售批发", "商业公司-汽车", "商业公司-消防安防", "商业公司-运营商", "商业公司-系统集成", "商业公司-环保", "商业公司-农业", "商业公司-林业", "商业公司-渔业", "商业公司-畜牧", "商业公司-体育", "商业公司-文化", "商业公司-旅游", "商业公司-教育服务", "商业公司-医疗服务"};
    private static final String[] P13 = { "商业公司-互联网", "商业公司-通信"};
    private static final String[] P14 = { "商业公司-工程建筑"};
    private static final String[] P15 = { "商业公司-装饰装修", "商业公司-房地产", "商业公司-生活服务"};
    private static final String[] P16 = { "商业公司-智慧科技", "金融企业-平台公司", "商业公司-其他"};


    public String getIndustry(String company) {

        String government = GovernmentRule.ruleVerification(company);
        String medical = MedicalRule.ruleVerification(company);
        String education = EducationRule.ruleVerification(company);
        String finacial = FinacialRule.ruleVerification(company);
        String business = BusinessRule.ruleVerification(company);

        String governmentBlacks = medical + education + finacial + business;
        String educationBlacks = business;

        if (StringUtils.isNotBlank(government)){
            if (StringUtils.isNotBlank(governmentBlacks)){
                government = "";
            }
        }

        if (StringUtils.isNotBlank(education)){
            if (StringUtils.isNotBlank(educationBlacks)){
                education = "";
            }
        }

        String allIndustry = government + medical + education + finacial + business;

        // 先匹配全部的单位信息
        String industry = "";

        boolean flag = true;
        // 校验第一梯队的数据, 获取行业信息
        for (String key : P0) {
            if (allIndustry.contains(key)) {
                industry += key + ConstantBean.RULE_SEPARATOR;
                flag = false;
            }
        }
        // 第一梯队校验失败后, 校验第二梯队的数据, 获取行业信息
        if (flag) {
            for (String key : P1) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }
        // 第一、二梯队校验失败后, 校验第三梯队的数据, 获取行业信息
        if (flag) {
            for (String key : P2) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }

        if (flag) {
            for (String key : P3) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }

        if (flag) {
            for (String key : P4) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }

        if (flag) {
            for (String key : P5) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }

        if (flag) {
            for (String key : P6) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }

        if (flag) {
            for (String key : P7) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }

        if (flag) {
            for (String key : P8) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }

        if (flag) {
            for (String key : P9) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }

        if (flag) {
            for (String key : P10) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }

        // 第一梯队校验失败后, 校验第二梯队的数据, 获取行业信息
        if (flag) {
            for (String key : P11) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }
        // 第一、二梯队校验失败后, 校验第三梯队的数据, 获取行业信息
        if (flag) {
            for (String key : P12) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }

        if (flag) {
            for (String key : P13) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }

        if (flag) {
            for (String key : P14) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }

        if (flag) {
            for (String key : P15) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                    flag = false;
                }
            }
        }
        if (flag) {
            for (String key : P16) {
                if (allIndustry.contains(key)) {
                    industry += key + ConstantBean.RULE_SEPARATOR;
                }
            }
        }

        if (StringUtils.isNotBlank(industry)){
            industry = industry.substring(0, industry.length() - 1);
        }

        if (StringUtils.isNotBlank(industry)){
            String[] split = industry.split("&");
            return split[0];
        }
        return "行业待分类";
    }
}
