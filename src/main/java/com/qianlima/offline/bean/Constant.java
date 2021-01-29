package com.qianlima.offline.bean;

public class Constant {

    public static final String SELECT_EXISTS_RESULT = "select content_id from data_producer where content_id =? and task_id=?";
    public static final String INSERT_RESULT = "insert into data_producer (content_id,task_id) values (?,?)";
    //公告标签修复队列--key
    public static final String NOTICE_MONGO_KEY = "ka_mongo";
    //交换机
    public static final String EXCHANGE_NAME = "ka_exchange";
}
