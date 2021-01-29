package com.qianlima.offline.controller;

import com.qianlima.offline.replenish.ZhaoBiaoPhoneService;
import com.qianlima.offline.replenish.ZhongBiaoPhoneService;
import com.qianlima.offline.replenish.ZhuCeAreaService;
import com.qianlima.offline.service.BiaoDiWuService;
import com.qianlima.offline.service.PocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/poc")
@Slf4j
public class PocController {

    @Autowired
    private PocService pocService;

    @Autowired
    private BiaoDiWuService biaoDiWuService;

    @Autowired
    private ZhaoBiaoPhoneService zhaoBiaoPhoneService;

    @Autowired
    private ZhongBiaoPhoneService zhongBiaoPhoneService;

    @Autowired
    private ZhuCeAreaService zhuCeAreaService;

    //阿里云-业务部
    @RequestMapping(value = "/start/aliyun", method = RequestMethod.GET)
    public String geALiYunSolrAllField(String date) throws Exception {
        pocService.geALiYunSolrAllField(date);
        return "ALiYun Normal的数据处理完毕啦啦啦啦啦";
    }

    //匹配行业标签
    @RequestMapping(value = "/start/ruiwode", method = RequestMethod.GET)
    public String getRuiWoDeSolrAllField(String date) throws Exception {
        pocService.getRuiWoDeSolrAllField(date);
        return "RuiWoDe Normal的数据处理完毕啦啦啦啦啦";
    }

//    //平安联想
//    @RequestMapping(value = "/start/pingan", method = RequestMethod.GET)
//    public String getPingAnSolrAllField(String date) throws Exception {
//        pocService.getPingAnSolrAllField(date);
//        return "PingAn Normal的数据处理完毕啦啦啦啦啦";
//    }

    //维尔利线下交付
    @RequestMapping(value = "/start/weierli", method = RequestMethod.GET)
    public String getWeiErLiSolrAllField(String date) throws Exception {
        pocService.getWeiErLiSolrAllField(date);
        return "WeiErLi Normal的数据处理完毕啦啦啦啦啦";
    }

    //睿博天米
    @RequestMapping(value = "/start/ruibo", method = RequestMethod.GET)
    public String getRuiBoSolrAllField(String date) throws Exception {
        pocService.getRuiBoSolrAllField(date);
        return "RuiBo Normal的数据处理完毕啦啦啦啦啦";
    }
    //睿博天米 天眼查补充中标单位联系方式
    @RequestMapping(value = "/start/ruibobuchong", method = RequestMethod.GET)
    public String getbuchongRuiBoSolrAllField() {
        zhongBiaoPhoneService.getCompanyAboutHangYe();
        return "RuiBobuchong Normal的数据处理完毕啦啦啦啦啦";
    }
    //睿博天米 天眼查补充注册地址
    @RequestMapping(value = "/start/ruibobuchong2", method = RequestMethod.GET)
    public String getbuchong2RuiBoSolrAllField() {
        zhuCeAreaService.getRegLocationHangYe();
        return "睿博天米 注册地址补充完毕啦啦啦啦啦";
    }

    //中讯邮电
    @RequestMapping(value = "/start/zhongxun", method = RequestMethod.GET)
    public String getZhongXunSolrAllField(String date) throws Exception {
        pocService.getZhongXunSolrAllField(date);
        return "ZhongXun Normal的数据处理完毕啦啦啦啦啦";
    }

    //电子网络设备
    @RequestMapping(value = "/start/dainzi", method = RequestMethod.GET)
    public String getDianZiLiSolrAllField(String date) throws Exception {
        pocService.getDianZiLiSolrAllField(date);
        return "DianZi Normal的数据处理完毕啦啦啦啦啦";
    }
    //电子网络设备补充数据
    @RequestMapping(value = "/start/dainzibuchong", method = RequestMethod.GET)
    public String juchibuchong(){
        zhongBiaoPhoneService.getCompanyAboutHangYe();
        return "补充完毕！！！";
    }

    //安永
    @RequestMapping(value = "/start/anyong", method = RequestMethod.GET)
    public String getAnYongSolrAllField(String date) throws Exception {
        pocService.getAnYongSolrAllField(date);
        return "AnYong Normal的数据处理完毕啦啦啦啦啦";
    }

    //福州雪品
    @RequestMapping(value = "/start/xuepin", method = RequestMethod.GET)
    public String getXunPinSolrAllField(String date) throws Exception {
        pocService.getXunPinSolrAllField(date);
        return "XunPin Normal的数据处理完毕啦啦啦啦啦";
    }

    //上海商众
    @RequestMapping(value = "/start/shangzhong", method = RequestMethod.GET)
    public String getShangZhongSolrAllField(String date) throws Exception {
        pocService.getShangZhongSolrAllField(date);
        return "ShangZhong Normal的数据处理完毕啦啦啦啦啦";
    }

    //华大基因
    @RequestMapping(value = "/start/huadajiyin", method = RequestMethod.GET)
    public String getHuaDaJiYinSolrAllField(String date) throws Exception {
        pocService.getHuaDaJiYinSolrAllField(date);
        return "HuaDaJiYin Normal的数据处理完毕啦啦啦啦啦";
    }

    //浙江华数广电
    @RequestMapping(value = "/start/huashu", method = RequestMethod.GET)
    public String getHuaShuSolrAllField(String date) throws Exception {
        pocService.getHuaShuSolrAllField(date);
        return "HuaShu Normal的数据处理完毕啦啦啦啦啦";
    }

    //银河物业
    @RequestMapping(value = "/start/yinhe", method = RequestMethod.GET)
    public String getYinHeSolrAllField(String date) throws Exception {
        pocService.getYinHeSolrAllField(date);
        return "YinHe Normal的数据处理完毕啦啦啦啦啦";
    }

    //深信服
    @RequestMapping(value = "/start/shenxinfu", method = RequestMethod.GET)
    public String getShenXinFuSolrAllField(String date) throws Exception {
        pocService.getShenXinFuSolrAllField(date);
        return "ShenXinFu Normal的数据处理完毕啦啦啦啦啦";
    }

    //大金投资
    @RequestMapping(value = "/start/dajin", method = RequestMethod.GET)
    public String getDaJinSolrAllField(String date) throws Exception {
        pocService.getDaJinSolrAllField(date);
        return "DaJin Normal的数据处理完毕啦啦啦啦啦";
    }

    //上海院校
    @RequestMapping(value = "/start/yuanxiao", method = RequestMethod.GET)
    public String getYuanXiaoSolrAllField(String date) throws Exception {
        pocService.getYuanXiaoSolrAllField(date);
        return "YuanXiao Normal的数据处理完毕啦啦啦啦啦";
    }

    //勘探者科技
    @RequestMapping(value = "/start/kantanzhe", method = RequestMethod.GET)
    public String getKanTanZheSolrAllField(String date) throws Exception {
        pocService.getKanTanZheSolrAllField(date);
        return "KanTanZhe Normal的数据处理完毕啦啦啦啦啦";
    }

    //中铁建设
    @RequestMapping(value = "/start/zhongtie", method = RequestMethod.GET)
    public String getZhongTieSolrAllField(String date) throws Exception {
        pocService.getZhongTieSolrAllField(date);
        return "ZhongTie Normal的数据处理完毕啦啦啦啦啦";
    }

    //蔚来之光
    @RequestMapping(value = "/start/weilai", method = RequestMethod.GET)
    public String getWeiLaiSolrAllField(String date) throws Exception {
        pocService.getWeiLaiSolrAllField(date);
        return "WeiLai Normal的数据处理完毕啦啦啦啦啦";
    }

    //广汽汇理线下交付
    @RequestMapping(value = "/start/guangqi", method = RequestMethod.GET)
    public String getGuangQiSolrAllField(String date) throws Exception {
        pocService.getGuangQiSolrAllField(date);
        return "GuangQi Normal的数据处理完毕啦啦啦啦啦";
    }

    //晶元光电
    @RequestMapping(value = "/start/jingyuan", method = RequestMethod.GET)
    public String getJingYuanSolrAllField(String date) throws Exception {
        pocService.getJingYuanSolrAllField(date);
        return "JingYuan Normal的数据处理完毕啦啦啦啦啦";
    }

    //万方数据
    @RequestMapping(value = "/start/wanfang", method = RequestMethod.GET)
    public String getWanFangSolrAllField(String date) throws Exception {
        pocService.getWanFangSolrAllField(date);
        return "WanFang Normal的数据处理完毕啦啦啦啦啦";
    }

    //华为终端
    @RequestMapping(value = "/start/huawei", method = RequestMethod.GET)
    public String getHuaWeiSolrAllField(String date) throws Exception {
        pocService.getHuaWeiSolrAllField(date);
        return "HuaWei Normal的数据处理完毕啦啦啦啦啦";
    }

    //平安信息
    @RequestMapping(value = "/start/pingan", method = RequestMethod.GET)
    public String getPingAnSolrAllField(String date){
        pocService.getPingAnXinxiSolrAllField(date);
        return "PingAn Normal的数据处理完毕啦啦啦啦啦";
    }

    //华讯网络
    @RequestMapping(value = "/start/huaxun", method = RequestMethod.GET)
    public String getHuaXunSolrAllField(String date){
        pocService.getHuaXunSolrAllField(date);
        return "HuaXun Normal的数据处理完毕啦啦啦啦啦";
    }

    //岛津企业
    @RequestMapping(value = "/start/daojin", method = RequestMethod.GET)
    public String getDaoJinSolrAllField(String date){
        pocService.getDaoJinSolrAllField(date);
        return "DaoJin Normal的数据处理完毕啦啦啦啦啦";
    }

}
