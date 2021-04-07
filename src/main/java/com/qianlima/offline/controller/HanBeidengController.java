package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.TestBeiDengService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * hanzijun 接口---3月9号以后
 */
@RestController
@RequestMapping("/han")
@Slf4j
@Api("han3.9")
public class HanBeidengController {

    @Autowired
    private TestBeiDengService testBeiDengService;


    @ApiOperation("贝登-第四回合(全文检索a)")
    @PostMapping(value = "/getBeideng",produces = "text/plain;charset=utf-8")
    //参数：progidStr取值---> 判断 0:0、1:全部、2:招标[0 TO 2]、3:3、4:[0 TO 3]、5:中标[3 OR progid:5]、6:[0 OR progid:3]
    public String getBeideng4(Integer type,String date,String progidStr) throws Exception{
        testBeiDengService.getBeiDeng4(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---贝登-第四回 接口运行结束---";
    }
    @ApiOperation("贝登-第四回合-2(全文+辅助检索a)")
    @PostMapping(value = "/getBeideng2",produces = "text/plain;charset=utf-8")
    //参数：progidStr取值---> 判断 0:0、1:全部、2:招标[0 TO 2]、3:3、4:[0 TO 3]、5:中标[3 OR progid:5]、6:[0 OR progid:3]
    public String getBeideng4_2(Integer type,String date,String progidStr) throws Exception{
        testBeiDengService.getBeideng4_2(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---贝登-第四回-2 接口运行结束---";
    }

    @ApiOperation("贝登-第四回合-3(规则一和规则二混合)")
    @PostMapping(value = "/getBeideng3",produces = "text/plain;charset=utf-8")
    //参数：progidStr取值---> 判断 0:0、1:全部、2:招标[0 TO 2]、3:3、4:[0 TO 3]、5:中标[3 OR progid:5]、6:[0 OR progid:3]
    public String getBeideng3(Integer type,String date,String progidStr) throws Exception{
        testBeiDengService.getBeideng3(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---贝登-第四回-规则3 接口运行结束---";
    }

    @ApiOperation("贝登-第四回合-4(标识黑词)")
    @PostMapping(value = "/getBeideng4_1",produces = "text/plain;charset=utf-8")
    //参数：progidStr取值---> 判断 0:0、1:全部、2:招标[0 TO 2]、3:3、4:[0 TO 3]、5:中标[3 OR progid:5]、6:[0 OR progid:3]
    public String getBeideng4_1(Integer type,String date,String progidStr) throws Exception{
        testBeiDengService.getBeideng4_1(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---贝登-第四回-规则4去标识黑词 接口运行结束---";
    }


}
