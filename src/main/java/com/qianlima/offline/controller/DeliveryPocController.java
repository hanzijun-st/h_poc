package com.qianlima.offline.controller;

import com.qianlima.offline.replenish.ZhaoBiaoPhoneService;
import com.qianlima.offline.replenish.ZhongBiaoPhoneService;
import com.qianlima.offline.service.BiaoDiWuService;
import com.qianlima.offline.service.DeliveryPocService;
import com.qianlima.offline.service.offline.DaHuaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/delivery")
@Slf4j
public class DeliveryPocController {

    @Autowired
    private DeliveryPocService deliveryPocService;

    @Autowired
    private BiaoDiWuService biaoDiWuService;

    @Autowired
    private DaHuaService daHuaService;

    @Autowired
    private ZhaoBiaoPhoneService zhaoBiaoPhoneService;

    @Autowired
    private ZhongBiaoPhoneService zhongBiaoPhoneService;

    //中节能 线下交付
    @RequestMapping(value = "/start/zhongjieneng", method = RequestMethod.GET)
    public String getZhongJieNengSolrAllField() throws Exception {
        deliveryPocService.getZhongJieNengSolrAllField();
        return "ZhongJieNeng Normal的数据处理完毕啦啦啦啦啦";
    }

    //大金投资 线下交付
    @RequestMapping(value = "/start/dajin", method = RequestMethod.GET)
    public String getDaJinSolrAllField() throws Exception {
        deliveryPocService.getDaJinSolrAllField();
        return "DaJin Normal的数据处理完毕啦啦啦啦啦";
    }

    //和德创新 线下交付
    @RequestMapping(value = "/start/hede", method = RequestMethod.GET)
    public String getHeDeSolrAllField() throws Exception {
        deliveryPocService.getHeDeSolrAllField();
        return "HeDe Normal的数据处理完毕啦啦啦啦啦";
    }

    //雷度米特 线下交付
    @RequestMapping(value = "/start/leidumite", method = RequestMethod.GET)
    public String getLeiDuMiTeSolrAllField() throws Exception {
        deliveryPocService.getLeiDuMiTeSolrAllField();
        return "LeiDuMiTe Normal的数据处理完毕啦啦啦啦啦";
    }

    //永升物业 线下交付
    @RequestMapping(value = "/start/yongsheng", method = RequestMethod.GET)
    public String getYongShengSolrAllField() throws Exception {
        deliveryPocService.getYongShengSolrAllField();
        return "YongSheng Normal的数据处理完毕啦啦啦啦啦";
    }

    //淡水泉 线下交付
    @RequestMapping(value = "/start/danshuiquan", method = RequestMethod.GET)
    public String getDanShuiQuanSolrAllField(String date) throws Exception {
        deliveryPocService.getDanShuiQuanSolrAllField(date);
        return "DanShuiQuan Normal的数据处理完毕啦啦啦啦啦";
    }

    //淡水泉 线下交付2
    @RequestMapping(value = "/start/danshuiquan2", method = RequestMethod.GET)
    public String getDanShuiQuan2SolrAllField(String date) throws Exception {
        deliveryPocService.getDanShuiQuan2SolrAllField(date);
        return "DanShuiQuan2 Normal的数据处理完毕啦啦啦啦啦";
    }

    //淡水泉 线下交付3
    @RequestMapping(value = "/start/danshuiquan3", method = RequestMethod.GET)
    public String getDanShuiQuan3SolrAllField(String date) throws Exception {
        deliveryPocService.getDanShuiQuan3SolrAllField(date);
        return "DanShuiQuan3 Normal的数据处理完毕啦啦啦啦啦";
    }

    //维尔利 线下交付
    @RequestMapping(value = "/start/weierli", method = RequestMethod.GET)
    public String getWeiErLiSolrAllField(String date) throws Exception {
        deliveryPocService.getWeiErLiSolrAllField(date);
        return "WeiErLi Normal的数据处理完毕啦啦啦啦啦";
    }

    //三维天地 线下交付
    @RequestMapping(value = "/start/sanwei", method = RequestMethod.GET)
    public String getSanWeiSolrAllField(String date) throws Exception {
        deliveryPocService.getSanWeiSolrAllField(date);
        return "SanWei Normal的数据处理完毕啦啦啦啦啦";
    }


}
