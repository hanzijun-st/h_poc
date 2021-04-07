package com.qianlima.offline.service.han;

import com.qianlima.offline.bean.Params;

import java.util.List;
import java.util.Map;

/**
 * 通用接口
 * Created by Administrator on 2021/1/14.
 */
public interface CurrencyService {
    /**
     *  区分 1全部，2.招标 3.中标
     */
    String getProgidStr(String str);

    /**
     * 一个关键词搜索
     * @param params
     */
    void getOnePoc(Params params);



    /**
     * 行业标签
     */
    void getBiaoQian();

    void getPpei();

    /**
     * HttpGet 方法
     * @param contentId
     * @return
     */
    String getHttpGet(String contentId);


    /**
     * 通用插入中台数据库的操作
     * @param map
     * @param sql 对应的sql
     */
    void saveTyInto(Map<String, Object> map, String sql);

    /**
     *  本地
     * @param name
     * @param list
     */
    void readFileByNameBd(String name,List<String> list);

    /**
     * 测试服务器
     * @param name
     * @param list
     */
    void readFileByName(String name,List<String> list);

}
