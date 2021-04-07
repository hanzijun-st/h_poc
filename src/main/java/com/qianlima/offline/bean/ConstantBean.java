package com.qianlima.offline.bean;


/**
 * 常量类
 */
public class ConstantBean {

    public static final String SELECT_TIME_ONE_NOW = "select contentid,progid,catid,updatetime,areaid,title,url from phpcms_content  where updatetime>? and status=99 ORDER BY updatetime limit ?";
    public static final String SELECT_TIME_TWO_NOW = "select contentid,progid,catid,updatetime,areaid,title,url from phpcms_content  where updatetime=? and status=99 ";

    public static final String SELECT_TIME_HISTORY = "select contentid,progid,catid,updatetime,areaid from phpcms_content  where updatetime>? and updatetime<= ? and status=99";
    //迈瑞用户id
    public static final String MINDRAY_USER_ID = "1";

    //企业会员使用的sql
    public static final String SELECT_UPLOG_ID = "SELECT id,uptime,optype,cid FROM `zdy_cms_uplog` where id > ? order by id asc limit ?";
    public static final String SELECT_PHPCMS_CONTENT = "SELECT contentid,areaid,catid,progid,status FROM `phpcms_content` where contentid = ? ";

    //项目类数据正文
    public static final String SELECT_ITEM_CONTENT_BY_CONTENTID = "SELECT content FROM phpcms_c_zb where contentid = ? ";

    /**
     * 词包规则分隔符
     */
    public static final String RULE_SEPARATOR = "、";
    public static final String RULE_SEPARATOR_01 = "&";

//    public static final String SELECT_ITEM_CONTENT_BY_CONTENTID = "SELECT content FROM phpcms_c_zb where contentid = ? ";


    public static final String SELECT_TIME_ONE_NOW_02 = "select contentid,progid,catid,updatetime,areaid,title,url from phpcms_content  where contentid = ?";
    /**
     * 词包规则分隔符(标签name)
     */
    public static final String RULE_SEPARATOR_NAME = ",";

    /**
     *  本地-下载数据量统计专用链接
     */
    public static final String FILL_URL_BD ="C:/Users/Administrator/Desktop/dataCount";


    public static final String FILL_URL ="/usr/local/ka/hzj";
}