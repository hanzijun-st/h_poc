package com.qianlima.offline.bean;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * @author shenjiqiang
 * @Title: Enterprise
 * @ProjectName qianliyan
 * @Description: TODO
 * @date 2019/2/23 14:32
 */
@Data
@Document(collection = "enterprise_tyc")
public class Enterprise implements Serializable {

    @Id
    private String oId;

    /**
     *  天眼查链接
     */
    @Field("link")
    private String link;

    /**
     *  updatetime	Number	毫秒数	数据库更新的时间
     */
    @Field("updatetime")
    private Long updateTime;

    /**
     *  staffNumRange	String	varchar(200)	人员规模
     */
    @Field("staffNumRange")
    private String staffNumRange;

    /**
     *  fromTime	Number	毫秒数	经营开始时间
     */
    @Field("fromTime")
    private Long fromTime;

    /**
     *  type	Number		法人类型，1 人 2 公司
     */
    @Field("type")
    private Integer type;

    /**
     *  categoryScore	Number	万分制	行业分数
     */
    @Field("categoryScore")
    private Integer categoryScore;

    /**
     *  bondName	String	varchar(20)	股票名
     */
    @Field("bondName")
    private String bondName;

    /**
     *  isClaimed	String	弃用	网站认证
     */
    @Field("isClaimed")
    private String isClaimed;

    /**
     *  id	Number		企业id
     */
    @Field("tycId")
    private String id;

    /**
     *  isMicroEnt	Number		是否是小微企业 0不是 1是
     */
    @Field("isMicroEnt")
    private Integer isMicroEnt;

    /**
     *  usedBondName	String	varchar(20)	股票曾用名
     */
    @Field("usedBondName")
    private String usedBondName;

    /**
     *  regNumber	String	varchar(31)	注册号
     */
    @Field("regNumber")
    private String regNumber;

    /**
     *  percentileScore	Number	万分制	企业评分
     */
    @Field("percentileScore")
    private Integer percentileScore;

    /**
     *  regCapital	String	varchar(50)	注册资本
     */
    @Field("regCapital")
    private String regCapital;

    /**
     *  name	String	varchar(255)	企业名
     */
    @Field("name")
    private String name;

    /**
     *  regInstitute	String	varchar(255)	登记机关
     */
    @Field("regInstitute")
    private String regInstitute;

    /**
     *  regLocation	String	varchar(255)	注册地址
     */
    @Field("regLocation")
    private String regLocation;

    /**
     *  industry	String	varchar(255)	行业
     */
    @Field("industry")
    private String industry;

    /**
     *  approvedTime	Number	毫秒数	核准时间
     */
    @Field("approvedTime")
    private Long approvedTime;

    /**
     *  socialStaffNum	Number		参保人数
     */
    @Field("socialStaffNum")
    private Integer socialStaffNum;

    /**
     *  tags	String	varchar(255)	企业标签
     */
    @Field("tags")
    private String tags;

    /**
     *  logo	String	varchar(150)	logo（不建议使用）
     */
    @Field("logo")
    private String logo;

    /**
     *  taxNumber	String	varchar(255)	纳税人识别号
     */
    @Field("taxNumber")
    private String taxNumber;

    /**
     *  businessScope	String	varchar(4091)	经营范围
     */
    @Field("businessScope")
    private String businessScope;

    /**
     *  property3	String	varchar(255)	英文名
     */
    @Field("property3")
    private String property3;

    /**
     *  alias	String	varchar(255)	简称
     */
    @Field("alias")
    private String alias;

    /**
     *  orgNumber	String	varchar(31)	组织机构代码
     */
    @Field("orgNumber")
    private String orgNumber;

    /**
     *  regStatus	String	varchar(31)	企业状态
     */
    @Field("regStatus")
    private String regStatus;

    /**
     *  estiblishTime	Number	毫秒数	成立日期
     */
    @Field("estiblishTime")
    private Long estiblishTime;

    /**
     *  bondType	String	varchar(31)	股票类型
     */
    @Field("bondType")
    private String bondType;

    /**
     *  legalPersonName	String	varchar(120)	法人
     */
    @Field("legalPersonName")
    private String legalPersonName;

    /**
     *  toTime	Number	毫秒数	经营结束时间
     */
    @Field("toTime")
    private Long toTime;

    /**
     *  legalPersonId	Number		法人id
     */
    @Field("legalPersonId")
    private Long legalPersonId;

    /**
     *  sourceFlag	String	varchar(30)	数据来源标志
     */
    @Field("sourceFlag")
    private String sourceFlag;

    /**
     *  actualCapital	String	varchar(50)	实收注册资金
     */
    @Field("actualCapital")
    private String actualCapital;

    /**
     *  flag	Number		0-显示 1-不显示
     */
    @Field("flag")
    private Integer flag;

    /**
     *  correctCompanyId	String		新公司名id
     */
    @Field("correctCompanyId")
    private String correctCompanyId;

    /**
     *  companyOrgType	String	varchar(127)	企业类型
     */
    @Field("companyOrgType")
    private String companyOrgType;

    /**
     *  base	String	varchar(31)	省份简称
     */
    @Field("base")
    private String base;

    /**
     *  updateTimes	Number	毫秒数	抓取数据的时间
     */
    @Field("updateTimes")
    private Long updateTimes;

    /**
     *  companyType	Number		无用
     */
    @Field("companyType")
    private Integer companyType;

    /**
     *  creditCode	String	varchar(255)	统一社会信用代码
     */
    @Field("creditCode")
    private String creditCode;

    /**
     *  companyId	Number		对应表id
     */
    @Field("companyId")
    private Long companyId;

    /**
     *  historyNames	String	varchar(255)	曾用名
     */
    @Field("historyNames")
    private String historyNames;

    /**
     *  bondNum	String	varchar(20)	股票号
     */
    @Field("bondNum")
    private String bondNum;

    /**
     *  regCapitalCurrency	String	varchar(10)	注册资本币种 人民币 美元 欧元 等
     */
    @Field("regCapitalCurrency")
    private String regCapitalCurrency;

    /**
     *  actualCapitalCurrency	String	varchar(10)	实收注册资本币种 人民币 美元 欧元 等
     */
    @Field("actualCapitalCurrency")
    private String actualCapitalCurrency;

    /**
     *  orgApprovedInstitute	String		核准机关（无用）
     */
    @Field("orgApprovedInstitute")
    private String orgApprovedInstitute;

    /**
     *  nameSuffix	String		无用
     */
    @Field("nameSuffix")
    private String nameSuffix;

    /**
     *  email	String	varchar(50)	邮箱
     */
    @Field("email")
    private String email;

    /**
     *  websiteList	String	varchar(255)	网址
     */
    @Field("websiteList")
    private String websiteList;

    /**
     *  phoneNumber	String	varchar(255)	联系方式
     */
    @Field("phoneNumber")
    private String phoneNumber;

    /**
     *  property5	String		无用
     */
    @Field("property5")
    private String property5;

    /**
     *  listCode	String		无用
     */
    @Field("listCode")
    private String listCode;

    /**
     *  ownershipStake	String		无用
     */
    @Field("ownershipStake")
    private String ownershipStake;

    /**
     *  revokeDate	Number	毫秒数	吊销日期
     */
    @Field("revokeDate")
    private Long revokeDate;

    /**
     *  revokeReason	String	varchar(500)	吊销原因
     */
    @Field("revokeReason")
    private String revokeReason;

    /**
     *  cancelDate	Number	毫秒数	注销日期
     */
    @Field("cancelDate")
    private Long cancelDate;

    /**
     *  cancelReason	String	varchar(500)	注销原因
     */
    @Field("cancelReason")
    private String cancelReason;

    /**
     *  property4	String		弃用
     */
    @Field("property4")
    private String property4;

    /**
     *  total	Number		主要人员总数
     */
    @Field("total")
    private Integer total;


}