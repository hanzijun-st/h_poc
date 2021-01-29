package com.qianlima.offline.controller;

import com.qianlima.offline.middleground.ICTRule;
import com.qianlima.offline.service.*;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/hy")
@Slf4j
public class HyController {

    @Autowired
    private HyService hyService;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    private QyhyService qyhyService;

    @Autowired
    private ShiYuanService shiYuanService;

    @Autowired
    private ICTRule ictRule;

    //迈瑞数据
    @RequestMapping(value = "/start/mairui", method = RequestMethod.GET)
    public String getMaiRuiSolrAllField3() throws Exception {
        shiYuanService.getMaiRui();
        return "DaHua Normal的数据处理完毕啦啦啦啦啦";
    }

    //KA自用行业___根据 contentid/招标单位 匹配行业标签
    @RequestMapping(value = "/start/kahangye", method = RequestMethod.GET)
    public String getKaHangYeSolrAllField() throws Exception {
        qyhyService.getKaHangYeSolrAllField();
        return "KaHangYe Normal的数据处理完毕啦啦啦啦啦";
    }

    //腾讯行业标签___根据 contentid/招标单位 匹配行业标签
    @RequestMapping(value = "/start/tengxunhy", method = RequestMethod.GET)
    public String getTengXunhySolrAllField() throws Exception {
        qyhyService.getTengXunhySolrAllField();
        return "TengXunhy Normal的数据处理完毕啦啦啦啦啦";
    }

    //移动行业标签___根据 contentid/招标单位 匹配行业标签
    @RequestMapping(value = "/start/yidonghangye", method = RequestMethod.GET)
    public String getYiDongHangYeSolrAllField() throws Exception {
        qyhyService.getYiDongHangYeSolrAllField();
        return "YiDongHangYe Normal的数据处理完毕啦啦啦啦啦";
    }

    //标题___IK分词
    @RequestMapping(value = "/start/KaFenCi", method = RequestMethod.GET)
    public String getKaFenCiSolrAllField() throws Exception {
        qyhyService.getKaFenCiSolrAllField();
        return "KaFenCi Normal的数据处理完毕啦啦啦啦啦";
    }

    //根据 contentid 获取 招标单位 中标单位
    @RequestMapping(value = "/start/zhaozhongbiao", method = RequestMethod.GET)
    public String getZhaoZhongBiaoUnitSolrAllField() throws Exception {
        qyhyService.getAllBiaoDIWu();
        return "ZhaoZhongBiaoUnit Normal的数据处理完毕啦啦啦啦啦";
    }

    @Autowired
    ZhongTaiBiaoDiWuService zhongTaiBiaoDiWuService;
    @Autowired
    ZhongTaiBiaoDiWuServiceForOne zhongTaiBiaoDiWuServiceForOne;

    //获取标的物解析表
    @RequestMapping(value = "/start/jiexibiao", method = RequestMethod.GET)
    public String getJieXiBiaoSolrAllField() throws Exception {
        zhongTaiBiaoDiWuService.getSolrAllField();
        return "LeiDuMiTe Normal的数据处理完毕啦啦啦啦啦";
    }
    //获取标的物 需求字段
    @RequestMapping(value = "/start/ziduan", method = RequestMethod.GET)
    public String getZiDuanSolrAllField() throws Exception {
        zhongTaiBiaoDiWuServiceForOne.getSolrAllField();
        return "LeiDuMiTe Normal的数据处理完毕啦啦啦啦啦";
    }

    //判断数据是不是ICT数据
    @RequestMapping(value = "/start/ifIct", method = RequestMethod.GET)
    public void getIfICTSolrAllField() throws Exception {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList1 = new ArrayList<>();
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT content_id,title,content FROM idc_data ");
        for (Map<String, Object> maps : mapList) {

            String contentId = maps.get("content_id") != null ? maps.get("content_id").toString() : "";
            String title = maps.get("title") != null ? maps.get("title").toString() : "";
            String content = maps.get("content") != null ? maps.get("content").toString() : "";

            futureList1.add(executorService1.submit(() -> {
                try {
                    String ictInfo = ictRule.checkICT(contentId, title, content);
                    bdJdbcTemplate.update("UPDATE idc_data set code = ? WHERE content_id = ?",ictInfo,contentId);
                    log.info("contentid:{}======数据处理成功!!!!!!是否为ICT数据 ::ictInfo:{} ",contentId,ictInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();

    }


    //深圳安科
    @RequestMapping(value = "/start/anke", method = RequestMethod.GET)
    public String getAnKeSolrAllField() throws Exception {
        shiYuanService.getMaiRui();
        return "AnKe Normal的数据处理完毕啦啦啦啦啦";
    }

}
