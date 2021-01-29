package com.qianlima.offline.rule02;

/**
 * 标的物四个标准
 */
public enum  BiaoDiWuRule {

    /**
     * 1、迈瑞接口地址：
     */
    MAI_RUI(1,"http://47.104.4.12:5001/to_json_v3/"),

    /**
     * 2、[模型识别侧重“ICT行业”]：http://47.104.4.12:2022/inspect
     */
    ICT(2,"http://47.104.4.12:2022/inspect"),

    /**
     * 3、[模型识别侧重“医疗行业”]：http://47.104.4.12:2023/inspect
     */
    YI_LIAO(3,"http://47.104.4.12:2023/inspect"),

    /**
     * 4、[模型识别没有侧重点]：http://47.104.4.12:2024/inspect
     */
    MODEL(4,"http://47.104.4.12:2024/inspect");

    private final String name;
    private int value; //枚举value字段

    private BiaoDiWuRule(int value,String name)
    {
        this.value= value;
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public Integer getValue(){
        return value;
    }



}