package com.qianlima.offline.bean;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class Area {
    /**
     * 地区id
     */
    @Field("areaid")
    private Integer areaid;
    /**
     * 地区名称
     */
    @Field("name")
    private String name;
    /**
     * 地区父级id
     */
    @Field("parentid")
    private String parentid;
    /**
     * 地区层级递进父级id
     * 如：国家0              那么本地区就是省级地区
     * 如：国家0，省级        那么本地区就是市级地区
     * 如：国家0，省级，市级。那么本地区就是县级地区
     */
    @Field("arrparentid")
    private String arrparentid;


}
