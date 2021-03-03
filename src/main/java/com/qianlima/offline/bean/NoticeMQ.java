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
    private String zhaoBiaoUnit;

    /**
     * zhongBiaoUnit
     */
    private String zhongBiaoUnit;

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

    private String blzhaoBiaoUnit;

    private String blzhongBiaoUnit;

    private String areaid;

    private String zhongRelationWay;
}
