package com.qianlima.offline.bean;

/**
 * 标准接口返回值枚举类
 */
public enum UserTagEnum {

    MINDRAY("1", "迈瑞医疗标签"),

    IFLYTEK_ICT_JIAOYU("2", "科大讯飞-ICT-教育"),

    IFLYTEK_ICT_TINGJIAN("3", "科大讯飞-ICT-听见"),

    IFLYTEK_ICT_ZHENGFA("4", "科大讯飞-ICT-政法"),

    IFLYTEK_ICT_YILIAO("5", "科大讯飞-ICT-医疗"),

    IFLYTEK_ICT_SMARTCITY("6", "科大讯飞-ICT-智慧城市"),

    ALI_ICT("8", "阿里云-ICT"),

    HAIGE_CAR("9", "海格汽车"),

    CHUANGXIN_YILIAO("10", "创新医疗"),

    GENETRON_FIGURERPC("11", "泛生子基因-数字PCR"),

    GENETRON_JIYINCEXUYI("12", "泛生子基因-基因测序仪"),

    GENETRON_JIYINTUBIANJIANCEHE("13", "泛生子基因-基因突变检测试剂盒"),

    WEIERLI_HUANBAO("14", "维尔利环保"),

    PICC_CAICHANXIAN("15", "人保财-财产险"),

    PICC_GONGCHENGXIAN("16", "人保财-工程险"),

    PICC_HUOYUNXIAN("17", "人保财-货运险"),

    PICC_CHUANBOXIAN("18", "人保财-船舶险"),

    PICC_ZERENXIAN("19", "人保财-责任险"),

    PICC_XINYONGBAOZHENGXIAN("20", "人保财-信用保证险"),

    BEIDENG("21", "贝登医疗标签"),

    SHIYUAN("22", "视源电子"),

    PICC_NONGXIAN("23", "人保财-农险"),

    PICC_YIWAIJIANKANGXIAN("24", "人保财-意外健康险"),

    PICC_CHEXIAN("25", "人保财-车险"),

    PICC_QITABAOXIAN("26", "人保财-其他保险"),

    PICC_GONGCHENGJIANZHU("27", "人保财-工程建筑"),

    PICC_FUWU("28", "人保财-服务");


    private String code;

    private String message;

    UserTagEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
