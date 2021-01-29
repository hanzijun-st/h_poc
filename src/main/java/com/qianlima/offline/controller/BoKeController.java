package com.qianlima.offline.controller;

import com.qianlima.offline.replenish.ZhaoBiaoPhoneService;
import com.qianlima.offline.replenish.ZhongBiaoPhoneService;
import com.qianlima.offline.service.BiaoDiWuService;
import com.qianlima.offline.service.BoKeService;
import com.qianlima.offline.service.BoKeService02;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/poc")
@Slf4j
public class BoKeController {

    @Autowired
    private BoKeService bokeService;

    @Autowired
    private BoKeService02 boKeService02;

    @Autowired
    private BiaoDiWuService biaoDiWuService;

    @Autowired
    private ZhaoBiaoPhoneService zhaoBiaoPhoneService;

    @Autowired
    private ZhongBiaoPhoneService zhongBiaoPhoneService;

    //博科
    @RequestMapping(value = "/start/boke", method = RequestMethod.GET)
    public String getBoKeSolrAllField() throws Exception {
        bokeService.getSolrAllField();
        return "JiBoNormal的数据处理完毕啦啦啦啦啦";
    }

    //博科
    @RequestMapping(value = "/start/boke02", method = RequestMethod.GET)
    public String getBoKeSolrAllField02() throws Exception {
        boKeService02.getSolrAllField();
        return "JiBoNormal的数据处理完毕啦啦啦啦啦";
    }
    //博科 补充中标单位联系方式
    @RequestMapping(value = "/start/bokebuchong", method = RequestMethod.GET)
    public String bokebuchong(){
        zhongBiaoPhoneService.getCompanyAboutHangYe();
        return "补充完毕！！！";
    }



}
