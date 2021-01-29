package com.qianlima.offline.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2021/1/14.
 */
@Data
@ApiModel
public class Params {
    @ApiModelProperty("起始时间")
    private String time1;

    @ApiModelProperty("终止时间")
    private String time2;

    @ApiModelProperty("类型区分 1全部，2.招标 3.中标")
    private String type;

    @ApiModelProperty("搜索范围 按标题搜索-title，按全文搜索-allcontent, 自提招标单位检索-zhaoBiaoUnit")
    private String title;

    @ApiModelProperty("地区")
    private String province;

    @ApiModelProperty("是否是单独地区")
    private Integer isProvince =0;

    @ApiModelProperty("是否存表  默认不存")
    private Integer isSave=0;

    @ApiModelProperty("是否有黑词 默认没有")
    private Integer isHave =0;

}
