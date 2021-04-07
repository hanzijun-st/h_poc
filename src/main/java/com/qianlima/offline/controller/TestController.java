package com.qianlima.offline.controller;

import com.qianlima.offline.service.DianXinService;
import com.qianlima.offline.service.han.AoLinBaSiService;
import com.qianlima.offline.service.han.TestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Administrator on 2021/1/12.
 */
@RestController
@RequestMapping("/test")
@Slf4j
@Api("测试类")
public class TestController {
    @Autowired
    private TestService testService;
    @Autowired
    private AoLinBaSiService aoLinBaSiService;

    @Autowired
    private DianXinService dianXinService;

    @RequestMapping(value = "/start", method = RequestMethod.GET, produces = "text/plain;charset=utf-8")
    public String getTestAllDatas(){

        return testService.getStr();
    }

    @PostMapping("/testData")
    @ApiOperation("测试数据库是否能调通")
    public String testData(){
        String s = testService.testData();
        return "---shujuku is---"+s;
    }

    @ApiOperation("佳电(上海)管理有限公司")
    @RequestMapping(value = "/start/getJdgl", method = RequestMethod.GET, produces = "text/plain;charset=utf-8")
    public String getJdgl(){
        aoLinBaSiService.getJdgl();
        return "---佳电(上海)管理有限公司---";
    }

    @ApiOperation("卓外")
    @RequestMapping(value = "/start/getZw", method = RequestMethod.GET)
    public String getZw(){
        aoLinBaSiService.getZw();
        return "---zw---";
    }

    @ApiOperation("电信")
    @RequestMapping(value = "/start/getDx", method = RequestMethod.GET)
    public String getDx(){
        try {
            dianXinService.getSolrAllField();
        } catch (Exception e) {

        }
        return "---getDx---";
    }

    @ApiOperation("筛选标题中含有...的项目名称")
    @RequestMapping(value = "/getProjectName", method = RequestMethod.GET)
    public String getProjectName(){
        try {
            testService.getProjectName();
        } catch (Exception e) {

        }
        return "---getXiangmuMingcheng---";
    }

    @ApiOperation("虎豹集团")
    @RequestMapping(value = "/getHuBao", method = RequestMethod.GET)
    public String getHuBao(String date,Integer type){
        try {
            testService.getHuBao(date,type);
        } catch (Exception e) {

        }
        return "---getHuBao---";
    }
    @ApiOperation("虎豹集团-第二回合")
    @RequestMapping(value = "/getHuBao2", method = RequestMethod.GET)
    public String getHuBao2(String date,Integer type,Integer fileType){
        try {
            testService.getHuBao2(date,type,fileType);
        } catch (Exception e) {

        }
        return "---getHuBao2 is ok---";
    }

    @ApiOperation("新鸿通科技")
    @RequestMapping(value = "/getXinhongtong", method = RequestMethod.GET)
    public String getXinhongtong(String date,Integer type){
        try {
            testService.getXinhongtong(date,type);
        } catch (Exception e) {

        }
        return "---getXinhongtong is ok---";
    }

    @ApiOperation("项目数据检查")
    @RequestMapping(value = "/getXiangmuShuju", method = RequestMethod.GET)
    public String getXiangmuShuju(String date,Integer type){
        try {
            testService.getXiangmuShuju(date,type);
        } catch (Exception e) {

        }
        return "---getXiangmuShuju is ok---";
    }



}

