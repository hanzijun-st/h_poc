package com.qianlima.offline.bean;

import lombok.Data;

@Data
public class NoticeMQGTX {
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
     * zhaoBiaoUnit
     */
    private String blZhaoBiaoUnit;

    /**
     * zhongBiaoUnit
     */
    private String zhongBiaoUnit;

    /**
     * zhaoBiaoUnit
     */
    private String blZhongBiaoUnit;

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
}
