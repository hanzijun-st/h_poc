package com.qianlima.offline.bean;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;

/**
 * 中台获取数据--临时封装类
 */
@Data
public class CusdataInfo implements Serializable {
    /**
     * 信息id
     */
    private String infoId;
    /**
     * 信息标题
     */
    private String infoTitle;
    /**
     * 信息内容
     */
    private String infoContent;
    /**
     * 信息类型 0：公告 1：预告 2：变更 3：结果
     */
    private String infoType;
    /**
     * 发布时间 格式：yyyy-MM-dd HH:mm:ss
     */
    private String infoPublishTime;
    /**
     * 信息来源（千里马url）
     */
    private String infoQianlimaUrl;
    /**
     * 信息地区--省
     */
    private String areaProvince;
    /**
     * 信息地区--市
     */
    private String areaCity;
    /**
     * 信息地区--区县
     */
    private String areaCountry;
    /**
     * 信息地区--老地区的areaid
     */
    private String oldAreaId;
    /**
     * 项目编号
     */
    private String xmNumber;
    /**
     * 标书获取开始时间
     */
    private String bidingAcquireTime;
    /**
     * 标书获取截止时间
     */
    private String bidingEndTime;
    /**
     * 投标开始时间
     */
    private String tenderBeginTime;
    /**
     * 投标截止时间
     */
    private String tenderEndTime;
    /**
     * 开标时间
     */
    private String openBidingTime;
    /**
     * 招标方式
     * 0：公开招标 1：邀请招标 2：竞争性谈判或竞 争性磋商 3：单一来源采购 4：询价 5：国务院政府采购监督管理部门认定的其他采购方式 6：电子反拍
     */
    private String biddingType;
    /**
     * 是否电子招标
     * 1、是 0、否
     */
    private String isElectronic;
    /**
     * 招标单位
     */
    private JSONArray zhaoBiaoUnit;
    /**
     * 招标单位联系人
     */
    private JSONArray zhaoRelationName;
    /**
     * 招标单位联系方式
     */
    private JSONArray zhaoRelationWay;
    /**
     * 中标单位
     */
    private JSONArray zhongBiaoUnit;
    /**
     * 中标单位联系人
     */
    private JSONArray zhongRelationName;
    /**
     * 中标单位联系方式
     */
    private JSONArray zhongRelationWay;
    /**
     * 代理机构
     */
    private JSONArray agentUnit;
    /**
     * 代理机构联系人
     */
    private JSONArray agentRelationName;
    /**
     * 代理机构联系方式
     */
    private JSONArray agentRelationWay;
    /**
     * 预算金额和单位
     * 数组，里面金额和单位一一对应
     */
    private JSONArray budget;
    /**
     * 中标金额和单位
     * 数组，里面金额和单位一一对应
     */
    private JSONArray winnerAmount;
    /**
     * 数据的创建时间
     */
    private String createTime;
    /**
     * 标的物
     */
    private JSONObject target;
    /**
     * 附件内容信息
     */
    private JSONArray infoFile;
    /**
     * 中标结果细分
     * 06-答疑公告， 07-废标公告， 08-流标公告， 09-开标公示， 10-候选人公示， 11-中标通知， 12-合同公告， 13-验收合同， 14-违规公告， 15-其他公告
     */
    private String infoTypeSegment;
    /**
     * 附件内容信息Id
     */
    private JSONArray infoLinkIds;
}
