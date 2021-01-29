package com.qianlima.offline.controller;

import com.qianlima.offline.replenish.ZhaoBiaoPhoneService;
import com.qianlima.offline.replenish.ZhongBiaoPhoneService;
import com.qianlima.offline.rule02.KeDaXunFeiRule;
import com.qianlima.offline.service.BiaoDiWuService;
import com.qianlima.offline.service.PocTestService;
import com.qianlima.offline.service.offline.DaHuaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/test")
@Slf4j
public class PocTestController {

    @Autowired
    private PocTestService pocTestService;

    @Autowired
    private BiaoDiWuService biaoDiWuService;

    @Autowired
    private DaHuaService daHuaService;

    @Autowired
    private ZhaoBiaoPhoneService zhaoBiaoPhoneService;

    @Autowired
    private ZhongBiaoPhoneService zhongBiaoPhoneService;

    //根据contentid导出标准字段(从solr里面查询)
    @RequestMapping(value = "/start/dajine", method = RequestMethod.GET)
    public String getDaJineSolrAllField() throws Exception {
        pocTestService.getSolrAllField();
        return "DaJineNormal的数据处理完毕啦啦啦啦啦";
    }
    //根据contentid导出标准字段(从中台查询)
    @RequestMapping(value = "/start/dajine2", method = RequestMethod.GET)
    public String getDaJineSolrAllField2() throws Exception {
        pocTestService.getSolrAllField2();
        return "DaJineNormal的数据处理完毕啦啦啦啦啦";
    }
    //根据contentid导出部分需求字段
    @RequestMapping(value = "/start/dajine3", method = RequestMethod.GET)
    public String getDaJineSolrAllField3() throws Exception {
        pocTestService.getSolrAllField3();
        return "DaJineNormal的数据处理完毕啦啦啦啦啦";
    }

    //通世达
    @RequestMapping(value = "/start/tongshida", method = RequestMethod.GET)
    public String getTongShiDaSolrAllField() throws Exception {
        pocTestService.getTongShiDaSolrAllField();
        return "TongShiDaNormal的数据处理完毕啦啦啦啦啦";
    }

    //江苏圣威
    @RequestMapping(value = "/start/shengwei", method = RequestMethod.GET)
    public String getShengWeiDaSolrAllField() throws Exception {
        pocTestService.getShengWeiDaSolrAllField();
        return "ShengWeiNormal的数据处理完毕啦啦啦啦啦";
    }

    //江苏火禾
    @RequestMapping(value = "/start/huohe", method = RequestMethod.GET)
    public String getHuoHeSolrAllField() throws Exception {
        pocTestService.getHuoHeSolrAllField();
        return "HuoHe Normal的数据处理完毕啦啦啦啦啦";
    }

    //威盛信息
    @RequestMapping(value = "/start/weisheng", method = RequestMethod.GET)
    public String getWeiShengSolrAllField() throws Exception {
        pocTestService.getWeiShengSolrAllField();
        return "WeiSheng Normal的数据处理完毕啦啦啦啦啦";
    }

    //乔治白
    @RequestMapping(value = "/start/qiaozhibai", method = RequestMethod.GET)
    public String getQiaoZhiSolrAllField(String date) throws Exception {
        pocTestService.getQiaoZhiSolrAllField(date);
        return "QiaoZhi Normal的数据处理完毕啦啦啦啦啦";
    }

    //凌立健康
    @RequestMapping(value = "/start/lingli", method = RequestMethod.GET)
    public String getLingLiSolrAllField(String date) throws Exception {
        pocTestService.getLingLiSolrAllField(date);
        return "LingLi Normal的数据处理完毕啦啦啦啦啦";
    }

    //昂楷科技
    @RequestMapping(value = "/start/angkai", method = RequestMethod.GET)
    public String getAngKaiSolrAllField(String date) throws Exception {
        pocTestService.getAngKaiSolrAllField(date);
        return "AngKai Normal的数据处理完毕啦啦啦啦啦";
    }

    //程力重工
    @RequestMapping(value = "/start/chengli", method = RequestMethod.GET)
    public String getChengLiSolrAllField(String date) throws Exception {
        pocTestService.getChengLiSolrAllField(date);
        return "ChengLi Normal的数据处理完毕啦啦啦啦啦";
    }

    //金海屿
    @RequestMapping(value = "/start/jinhaiyu", method = RequestMethod.GET)
    public String getJinHaiYuSolrAllField(String date) throws Exception {
        pocTestService.getJinHaiYuSolrAllField(date);
        return "JinHaiYu Normal的数据处理完毕啦啦啦啦啦";
    }

    //南京莱斯
    @RequestMapping(value = "/start/laisi", method = RequestMethod.GET)
    public String getLaiSiSolrAllField(String date) throws Exception {
        pocTestService.getLaiSiSolrAllField(date);
        return "LaiSi Normal的数据处理完毕啦啦啦啦啦";
    }

    //速臻科技
    @RequestMapping(value = "/start/suzhen", method = RequestMethod.GET)
        public String getSuZhenSolrAllField(String date) throws Exception {
        pocTestService.getSuZhenSolrAllField(date);
        return "SuZhen Normal的数据处理完毕啦啦啦啦啦";
    }

    //象辑知源
    @RequestMapping(value = "/start/xiangji", method = RequestMethod.GET)
    public String getXiangJiSolrAllField(String date) throws Exception {
        pocTestService.getXiangJiSolrAllField(date);
        return "XiangJi Normal的数据处理完毕啦啦啦啦啦";
    }

    //北京万通(腾讯ICT线上数据并匹配行业标签)
    @RequestMapping(value = "/start/wantong", method = RequestMethod.GET)
    public String getWanTongSolrAllField(String date) throws Exception {
        pocTestService.getWanTongSolrAllField(date);
        return "WanTong Normal的数据处理完毕啦啦啦啦啦";
    }

    //荣昌耀化网络
    @RequestMapping(value = "/start/rongyao", method = RequestMethod.GET)
    public String getRongYaoSolrAllField(String date) throws Exception {
        pocTestService.getRongYaoSolrAllField(date);
        return "RongYao Normal的数据处理完毕啦啦啦啦啦";
    }

    //羽医医疗
    @RequestMapping(value = "/start/yuyi", method = RequestMethod.GET)
    public String getYuYiSolrAllField(String date) throws Exception {
        pocTestService.getYuYiSolrAllField(date);
        return "YuYi Normal的数据处理完毕啦啦啦啦啦";
    }

    //美敦力
    @RequestMapping(value = "/start/meidunli", method = RequestMethod.GET)
    public String getMeiDunLiSolrAllField(String date) throws Exception {
        pocTestService.getMeiDunLiSolrAllField(date);
        return "MeiDunLi Normal的数据处理完毕啦啦啦啦啦";
    }

    //蓝谷生物
    @RequestMapping(value = "/start/langu", method = RequestMethod.GET)
    public String getLanGuSolrAllField(String date) throws Exception {
        pocTestService.getLanGuSolrAllField(date);
        return "LanGu Normal的数据处理完毕啦啦啦啦啦";
    }

    //ASP
    @RequestMapping(value = "/start/ASP", method = RequestMethod.GET)
    public String getASPSolrAllField(String date) throws Exception {
        pocTestService.getASPSolrAllField(date);
        return "ASP Normal的数据处理完毕啦啦啦啦啦";
    }

    //武汉高德智感
    @RequestMapping(value = "/start/gaode", method = RequestMethod.GET)
    public String getGaoDeSolrAllField(String date) throws Exception {
        pocTestService.getGaoDeSolrAllField(date);
        return "GaoDe Normal的数据处理完毕啦啦啦啦啦";
    }

    //中通管业
    @RequestMapping(value = "/start/zhongtong", method = RequestMethod.GET)
    public String getZhongTongSolrAllField(String date) throws Exception {
        pocTestService.getZhongTongSolrAllField(date);
        return "ZhongTong Normal的数据处理完毕啦啦啦啦啦";
    }

    //腾讯云
    @RequestMapping(value = "/start/tengxunyun", method = RequestMethod.GET)
    public String getTengXunYunSolrAllField(String date) throws Exception {
        pocTestService.getTengXunYunSolrAllField(date);
        return "TengXunYun Normal的数据处理完毕啦啦啦啦啦";
    }

    @Autowired
    private KeDaXunFeiRule keDaXunFeiRule;
    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    //数据匹配
    @RequestMapping(value = "/start/shuju", method = RequestMethod.GET)
    public void getShuJuSolrAllField() throws Exception {

        keDaXunFeiRule.initWordComb();
        List<Map<String, Object>> resultMaps = bdJdbcTemplate.queryForList(" SELECT content_id,title FROM `jyf_data`");

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        List<Future> futureList = new ArrayList<>();
        for (Map<String, Object> resultMap : resultMaps) {
            futureList.add(executorService.submit(() -> {

                String infoId = resultMap.get("content_id") != null ? resultMap.get("content_id").toString() : "";
                String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";

                String keyword = keDaXunFeiRule.ruleVerification(title, infoId);
                bdJdbcTemplate.update("update jyf_data set keyword = ? where content_id = ?",keyword,infoId);
            }));
        }

    }

}
