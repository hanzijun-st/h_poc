package com.qianlima.offline.controller;

import com.qianlima.offline.bean.Params;
import com.qianlima.offline.service.han.AoLinBaSiService;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.TestService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Administrator on 2021/1/12.
 */
@RestController
@RequestMapping("/aolinbasi")
@Slf4j
public class HanTestController {

    @Autowired
    private AoLinBaSiService aoLinBaSiService;
    @Autowired
    private TestService testService;

    @Autowired
    private CurrencyService currencyService;

    @GetMapping("/start/getAolinbasiDatas")
    public String getTestAllDatas(){
        aoLinBaSiService.getAoLinBaSiAndSave();
        return "第一次测试数据---获取成功";
    }

    @GetMapping("/start/getUrl/{num}")
    public String getUrl(@PathVariable("num") String num){
        String urlOriginalLink = aoLinBaSiService.getUrlOriginalLink(num);
        return "成功获取url原链接地址---"+urlOriginalLink;
    }

    @GetMapping("/start/getBdw")
    public String getBdw(){
        testService.getBdw();
        return "请求成功---成功获取标的物";
    }

    @GetMapping("/getNewBdw")
    public String getNewBdw(){
        testService.getNewBdw();
        return "请求成功---成功获取新方法的标的物";
    }

    @GetMapping("/start/updateKeyword")
    public String updateKeyword(){
        testService.updateKeyword();
        return "-----------修改关键词成功-----------";
    }

    /**
     * 1个关键词
     * @param params
     * @return
     */
    @ApiOperation("一个关键词")
    @PostMapping("/start/getOne")
    public String getOne(@RequestBody Params params){
        currencyService.getOnePoc(params);
        return "---测试---";
    }

    @ApiOperation("中国重汽集团")
    @PostMapping("/start/getZhongQi")
    public String getZhongQi(Integer type) throws Exception{
        testService.getZhongQi(type);
        return "---getZhongQi---";
    }

    @ApiOperation("房天下")
    @PostMapping("/start/getFangTianXia")
    public String getFangTianXia(Integer type) throws Exception{
        testService.getFangTianXia(type);
        return "---getFangTianXia---";
    }

    @ApiOperation("福建海博绿创")
    @PostMapping("/getFuJianHaiBo")
    public String getFuJianHaiBo(Integer type,String date) throws Exception{
        testService.getFuJianHaiBo(type,date);
        return "---getFuJianHaiBo---";
    }
    @ApiOperation("纵横大鹏无人机-规则一")
    @PostMapping("/getZongHengDaPeng1")
    public String getZongHengDaPeng1(Integer type,String date) throws Exception{
        testService.getZongHengDaPeng(type,date);
        return "---getZongHengDaPeng---";
    }
    @ApiOperation("纵横大鹏无人机-规则二")
    @PostMapping("/getZongHengDaPeng2")
    public String getZongHengDaPeng2(Integer type,String date) throws Exception{
        testService.getZongHengDaPeng2(type,date);
        return "---getZongHengDaPeng2---";
    }
    @ApiOperation("纵横大鹏无人机-规则三")
    @PostMapping("/getZongHengDaPeng3")
    public String getZongHengDaPeng3(Integer type,String date) throws Exception{
        testService.getZongHengDaPeng3(type,date);
        return "---getZongHengDaPeng3---";
    }
}
