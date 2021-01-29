package com.qianlima.offline.service.han;

/**
 * Created by Administrator on 2021/1/12.
 */
public interface TestService {
    /**
     * solr 中获取条件（用来查询中台数据）
     */
    void getBdw();
    /**
     * 最新标的物
     */
    void getNewBdw();

    void updateKeyword();

    /**
     * 测试接口是否调通
     */
    String getStr();

    /**
     * 测试数据库能否调通
     */
    String testData();

    /**
     * 中国重汽
     */
    void getZhongQi(Integer type) throws Exception;

    /**
     * 房天下
     * @param type
     */
    void getFangTianXia(Integer type);

    /**
     * 福建海博绿创
     * @param type
     */
    void getFuJianHaiBo(Integer type,String date);

    /**
     * 纵横大鹏无人机
     * @param type
     * @param date
     */
    void getZongHengDaPeng(Integer type, String date);

    void getZongHengDaPeng2(Integer type, String date);

    void getZongHengDaPeng3(Integer type, String date);
}
