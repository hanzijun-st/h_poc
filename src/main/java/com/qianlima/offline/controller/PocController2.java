package com.qianlima.offline.controller;

import com.qianlima.offline.replenish.ZhaoBiaoPhoneService;
import com.qianlima.offline.replenish.ZhongBiaoPhoneService;
import com.qianlima.offline.replenish.ZhuCeAreaService;
import com.qianlima.offline.service.BiaoDiWuService;
import com.qianlima.offline.service.PocService2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/poc2")
@Slf4j
public class PocController2 {

    @Autowired
    private PocService2 pocService2;

    @Autowired
    private BiaoDiWuService biaoDiWuService;

    @Autowired
    private ZhaoBiaoPhoneService zhaoBiaoPhoneService;

    @Autowired
    private ZhongBiaoPhoneService zhongBiaoPhoneService;

    @Autowired
    private ZhuCeAreaService zhuCeAreaService;

    //时代华擎
    @RequestMapping(value = "/start/shidai", method = RequestMethod.GET)
    public String getShiDaiSolrAllField(String date) throws Exception {
        pocService2.getShiDaiSolrAllField(date);
        return "ShiDai Normal的数据处理完毕啦啦啦啦啦";
    }


}
