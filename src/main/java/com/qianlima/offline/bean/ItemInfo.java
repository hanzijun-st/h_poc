package com.qianlima.offline.bean;

import lombok.Data;

/**
 * 项目类数据表
 */
@Data
public class ItemInfo {

    private String _id;
    /**
     * 信息id
     */
    private String itemId;
    /**
     * 信息标题
     */
    private String itemTitle;
    /**
     * 信息内容
     */
    private String itemContent;
    /**
     * 信息地区--省
     */
    private String areaProvince;
    /**
     * 信息地区--市
     */
    private String areaCity;
    /**
     * 信息地区--区县
     */
    private String areaCountry;
    /**
     * 信息地区--地区id
     */
    private String areaId;
    /**
     * 信息类型 301；601
     */
    private String itemType;
    /**
     * 进展阶段
     */
    private String itemStage;
    /**
     * 信息来源（千里马url）
     */
    private String itemQianlimaUrl;
    /**
     * 发布时间 格式：yyyy-MM-dd HH:mm:ss
     */
    private String itemPublishTime;
}