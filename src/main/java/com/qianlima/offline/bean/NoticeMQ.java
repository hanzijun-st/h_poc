package com.qianlima.offline.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class NoticeMQ implements Serializable {
    /**
     * 公告id
     */
    private Long contentid;
    /**
     * 个别需要需要关键词
     */
    private String key;
    /**
     * 任务id 详见数据库data_producer注释
     */
    private Integer taskId;

    /**
     * 标签
     */
    private String tags;
    /**
     * title
     */
    private String title;
    /**
     * title
     */
    private String keyword;

    /**
     * zhaoBiaoUnit
     */
    private String zhaoBiaoUnit;//自提招标单位

    /**
     * zhongBiaoUnit
     */
    private String zhongBiaoUnit;//自提中标单位

    /**
     * content
     */
    private String content;

    /**
     * type
     */
    private int type;
    /**
     * f词
     */
    private String f;


    private String url;

    //中标金额
    private String amount;

    //混合中标金额
    private String newAmountUnit;

    //招标预算金额
    private String budget;


    private String zhaoIndustry;

    private String updatetime;

    private String keywordTerm;

    private String blzhaoBiaoUnit;//百炼招标单位

    private String blzhongBiaoUnit;//百炼中标单位

    private String areaid;

    private String newZhongBiaoUnit;//混合中标单位

    private String zhaoFirstIndustry;//自提招标单位一级行业标签

    private String zhaoSecondIndustry;//自提招标单位二级行业标签

    private String heici;

    private String xmNumber;//项目编号

    private String newProvince;
    private String newCity;
    private String newCountry;

    //private String blBudget;//百炼招标预算

    //private String blAmountUnit;//百炼中标金额

    private String amountUnit;//自提中标金额
    private String zhaoRelationName;//自提招标单位联系人
    private String zhaoRelationWay;//自提招标单位联系方式

    private String zhongRelationName;//自提中标单位联系人
    private String zhongRelationWay;//自提中标单位联系方式

    private String progid;//信息类型

    private String agentUnit;//代理机构
    private String agentRelationName;//代理机构联系人
    private String agentRelationWay;//代理机构联系方式

    private String biddingType;//招标方式

    private String keys;//原标题
}
