package com.qianlima.offline.service.han;

/**
 * Created by Administrator on 2021/1/12.
 */
public interface AoLinBaSiService {

    void getAoLinBaSiAndSave();

    /**
     * 得到原来url链接
     */
    String getUrlOriginalLink(String num);

    /**
     * 佳电(上海)管理有限公司
     */
    void getJdgl();

    void getZw();
}
