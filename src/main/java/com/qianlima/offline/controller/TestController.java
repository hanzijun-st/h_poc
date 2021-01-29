package com.qianlima.offline.controller;

import com.qianlima.offline.service.DianXinService;
import com.qianlima.offline.service.han.AoLinBaSiService;
import com.qianlima.offline.service.han.TestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

}

