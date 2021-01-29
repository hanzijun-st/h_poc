package com.qianlima.offline.controller;

import com.qianlima.offline.middleground.ICTLingYuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("dajine")
@Slf4j
public class DaJineController {

    @Autowired
    private ICTLingYuService ictLingYuService;

    //大金额
    @RequestMapping(value = "/start/dajine", method = RequestMethod.GET)
    public String getDaJineSolrAllField() throws Exception {
        ictLingYuService.byTitleAndZhaoLY();
        return "DaJine Normal的数据处理完毕啦啦啦啦啦";
    }


}
