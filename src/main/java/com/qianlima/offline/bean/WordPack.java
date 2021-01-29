package com.qianlima.offline.bean;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 标签词包表
 */
@Data
@Document(collection = "word_pack")
public class WordPack {
    @Id
    private String _id;

    /**
     * 词包名称
     */
    @Field("name")
    private String name;

    /**
     * 词
     */
    @Field("keyword")
    private String keyword;

    /**
     * 所属标签
     */
    @Field("tag_id")
    private String tagId;

    /**
     * 创建时间
     */
    @Field("create_time")
    private String createTime;

}
