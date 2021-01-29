package com.qianlima.offline.bean;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * @author shenjiqiang
 * @Title: Enterprise
 */
@Data
@Document(collection = "biaodiwu")
public class BiaoDiWu implements Serializable {

    /**
     * contentId
     */
    @Field("_id")
    private Long id;

    /**
     * filterContent 标的物
     */
    @Field("filterContent")
    private String filterContent;

    /**
     * updatetime	更新时间
     */
    @Field("updatetime")
    private String updatetime;

}