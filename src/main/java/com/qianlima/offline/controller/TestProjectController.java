package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.TestProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2021/1/12.
 */
@RestController
@RequestMapping("/pro")
@Slf4j
@Api("项目类")
public class TestProjectController {

    @Autowired
    private TestProjectService testProjectService;



    @ApiOperation("筛选标题中含有...的项目名称")
    @RequestMapping(value = "/getProjectName", method = RequestMethod.GET)
    public String getProjectName(){
        try {
            testProjectService.getProjectName();
        } catch (Exception e) {
            e.getMessage();
        }
        return "---getXiangmuMingcheng---";
    }

}

