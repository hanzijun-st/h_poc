package com.qianlima.offline.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class NoticeMQStr implements Serializable {
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
    private String taskId;
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
     * update_time
     */
    private String updateTime;

    /**
     * f词
     */
    private String f;
}
