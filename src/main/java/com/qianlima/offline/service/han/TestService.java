package com.qianlima.offline.service.han;

/**
 * Created by Administrator on 2021/1/12.
 */
public interface TestService {
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

    /**
     * 合肥航联
     * @param type
     * @param date
     */
    void getHeFeiHangLian(Integer type, String date);

    void getBeiJingGuanrui(Integer type, String date);

    void getYuxin3(Integer type, String date) throws Exception;

    /**
     * 联系查询
     */
    void getLianx(Integer type);

    /**
     * 文思海辉
     * @param type
     * @param date
     */
    void getWenSiHaiHuib(Integer type, String date) throws Exception;

    /**
     * 文思海辉第二回合-规则二
     * @param type
     * @param date
     */
    void getWenSiHaiHuib2_2(Integer type, String date);

    /**
     * 奥林巴斯第二回合=规则二
     * @param type
     * @param date
     */
    void getAolinbasi2(Integer type, String date);

    /**
     * 奥林巴斯第二回合-规则三
     * @param type
     * @param date
     */
    void getAolinbasi2_3(Integer type, String date);

    void getAolinbasi2_qw(Integer type, String date);

    /**
     * 贝登2016年数据
     * @param type
     * @param date
     */
    void getBeiDeng2016(Integer type, String date) throws Exception;

    /**
     * 贝登---第二回合2016年数据
     * @param type
     * @param date
     * @throws Exception
     */
    void getBeiDeng20162(Integer type, String date) throws Exception;
}
