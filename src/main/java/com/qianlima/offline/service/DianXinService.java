package com.qianlima.offline.service;


import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.rule02.MyRuleUtils;
import com.qianlima.offline.util.ContentSolr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class DianXinService {


    @Autowired
    private ContentSolr contentSolr;


    @Autowired
    private MyRuleUtils myRuleUtils;


    @Autowired
    private CusDataFieldService cusDataFieldService;

    public void getSolrAllField() throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();

        String[] aas = { "医废信息化","医疗信息化","医院信息化","信息技术产业","医院信息化改造","医院信息化建设","医疗信息大数据","医保信息化","智医助理信息化","医疗信息化建设","虚拟化","信息应用","信息交互","信息管理","信息系统","信息化建设","信息平台","信息化系统","信息管理平台","信息服务平台","档案信息化","公司信息化","信息化升级","信息化改造","信息化工程","综合信息平台","信息化集成","信息化技术","智能信息化","医共体","医联体","监护系统","体检系统","超声系统","电子病历","区域医疗","医学影像","移动医护","分诊系统","远程医疗协作网","互联互通","桌面系统","追溯系统","支付系统","诊断系统","运维系统","预约系统","预警平台","舆情系统","硬件平台","应用系统","影像系统","叫号系统","监测预警","应急调度","舆情监测","舆论监测","应急响应","无纸化","治疗信息化","诊疗信息化","诊断信息化","远程诊疗信息化","远程医疗信息化","远程信息化","预约信息化","影像医学信息化","影像信息化","移动影像信息化","移动车载CT信息化","医院云信息化","医院影像信息化","医学影像信息化","医学摄影信息化","医学成像信息化","医疗影像信息化","医联体信息化","医共体信息化","心电信息化","摄像信息化","区域影像信息化","区域医疗信息化","内视镜信息化","内窥镜信息化","内镜信息化","叫号信息化","胶片信息化","会诊信息化","放射信息化","成像信息化","超声信息化","病理信息化","PACS信息化","系统集成","数字医疗","数字化医用","数字化医院","医疗大数据","肿瘤大数据","健康大数据","美容大数据","临床大数据","数字化医疗","医疗与大数据","数字健康","数字成像","数据资源","数据应用","数字建设","数据挖掘","数据迁移","数据采集","数据分析","数据处理","数据服务","数据安全","数据共享","数据整合","数据治理","数据交换","数据专线","数据备份","数据传输","数据加工","数据存储","数据质量","数据整理","数据接口","数据取证","数据接入","数据监测","治疗数字化","诊疗数字化","诊断数字化","远程诊疗数字化","远程医疗数字化","远程数字化","预约数字化","影像医学数字化","影像数字化","移动影像数字化","移动车载CT数字化","医院云数字化","医院影像数字化","医学影像数字化","医学摄影数字化","医学成像数字化","医疗影像数字化","医联体数字化","医共体数字化","心电数字化","摄像数字化","区域影像数字化","区域医疗数字化","内视镜数字化","内窥镜数字化","内镜数字化","叫号数字化","胶片数字化","会诊数字化","放射数字化","成像数字化","超声数字化","病理数字化","PACS数字化","分布式计算","智慧医院","智能医院","智能医疗","智能康复","医院智能化","智能化医疗","医保智能审核","医院智能通讯","智能发药系统","医疗人工智能","人工智能医疗","智能医疗设备","医疗智能辅助","口腔医院智能化","医院智能化建设","智能运维","智能硬件","智能培训","智能门禁","智能客服","智能机柜","智能电表","数据智能","人体分析","人脸支付","人脸识别","人脸融合","人脸取样","人脸检测","人工智能","智能数据湖","智能实验室","智能设备","智慧黑板","智能电视","智能运动","智能健康","智能音响","智能音箱","智能终端","智能家居","智能穿戴","智能耳机","智能电子","智能手机","智能办公","智能会议","智能产品","智慧电视","城市大脑","智慧城市","融合大数据","数字中国","智慧社会","数字城市","智能城市","数字政府","数字政务","城市治理平台","城市决策平台","未来城市","超级大脑","数据大脑平台","智慧医疗","智慧健康","无人机","深度学习","巡检机器人","智能识别","焊接机器人","文本处理","自然语言处理","文本标注","图像标注","健康医疗云","大数据云医疗","医疗影像云存储","医疗云服务","医疗上云","医疗云平台","云大数据","云网融合","云管平台","云化项目","电信云上","云网负载","支付云端","云盘建设","云盘扩容","企业云盘","云盘系统","内部云盘","数据云盘","网络云盘","网站安全云","大数云防","云防系统","云上实训","云端直播","云上直播","办公桌云建设","云镜系统","会诊会议","会诊视频","诊断会议","视频会议","多媒体项目","多媒体会议","多媒体采购","高清视频会议","多媒体学习库","多媒体虚拟库","多媒体会议室","视频监控","食堂监控","监控工程","集中监控","机房监控","融合媒体","媒体融合","实验室多媒体","媒体营销","媒体舆情","媒体广告投放","户外广告媒体","媒体产品开发","感染实时监控","医疗监控报警","医疗监控","病房监控","诊所监控","智能视频","视频存储","视频采集","视频录制","视频传输","视频集成","医院视频","安防视频","视频安防","工业视频","会议视频","天眼工程","电子警察","电子停车","无人值守","雪亮工程","平安城市","导医机器人","导诊机器人","医疗机器人","医用机器人","医疗设备大数据","可穿戴医疗设备","交换设备","监控设备","机房升级","专属主机","运维堡垒机","信息化设备","设备及软件","PC机","平板电脑","笔记本电脑","便携式电脑","台式机","一体机","台式服务器","塔式服务器","机架式服务器","机柜式服务器","刀片式服务器","摄像头","主控电脑","电脑主机","手持终端","终端机","小型机","一体化设备","行业专用机","商务本","游戏本","电脑一体机","触屏一体机","工控机","查询一体机","点餐机","多功能广告机","自助终端机","多媒体播放器","台式工作站","手提电脑","移动工作站","台式电脑","台式计算机","云服务器","办公电脑","电子设备","一体台式机","办公终端","打印机","复印机","投影仪","扫描仪","传真机","碎纸机","考勤机","装订机","计算器","文件处理机","打字机","速印机","电传机","验钞机","电子白板","投影机","幻灯机","录音笔","优盘","闪盘","放映机","移动硬盘","便携式硬盘","印刷机","订书机","基础硬件","硬件采购","系统总线","UPS电源","不间断电源","机箱电源","主机箱","网络机柜","显示器","广告屏","显示设备","机械硬盘","固态硬盘","UPS配电箱","音频处理器","模块接口卡","显示终端","语音合成器","磁带机","磁盘机","计数器","刻录机","计算机主板","硬件集成","硬件建设","电源适配器","远程医疗","网络设备","远程会诊","远程诊疗","医院网络","医疗网站","网上就诊","网上就医","综合布线","网站建设","网络租赁","网络直播","网络线路","网络维护","网络升级","交换机","路由器","中继器","集线器","网络接口卡","无线接入点","调制解调器","光纤收发器","网线接头","RJ45接头","网络连接设备","ADSL设备","桥由器","无线路由","网络模块","频道调制器","分支分配器","终端盒","信号接收器","转换器","数字调谐器","信号放大器","网络适配器","IP终端","负载均衡","信号收发器","无线控制器","信号屏蔽器","网络终端","信号发生器","接收机","接入设备","堡垒机","跳板机","机顶盒","无线网卡","第五代通信","防火墙","网络安全","主机防护","入侵检测","入侵防御","安全网关","安全网络","安全隔离与信息交换","等保测评","等级保护","边界安全产品","终端安全","应用安全","登录保护","注册保护","内容安全","号码安全","动态验证","网络产品安全","网络安全产品","信息安全测评","网站信息安全","渗透测试","医疗软件","医用软件","医院软件","医保软件","运维软件","应用软件","信息软件","杀毒软件","网络协议","编译程序","解释程序","汇编程序","语言处理程序","机器语言代码","中间代码","目标程序","程序段","程序模块","二进制程序","语法分析器","编译器","底层程序","硬件程序","事务式中间件","面向对象中间件","软件集成","系统支撑软件","系统扩容","办公软件","财务软件","企业管理软件","政务应用","流版签软件","在线办公","在线会议","在线翻译","电子表格","计算机软件","审查软件","预警软件","处理软件","监控软件","专业软件","修图软件","数字软件","加密软件","工作软件","定制软件","测算软件","备份软件","社保软件","工程软件","审计软件","协同办公","软件采购","软件升级","软件服务","软件测试","模拟软件","信息化软件","评估软件","软件扩容","评测软件","测评管理软件","软件监控","适配器","信息化管理软件","配置管理软件","虚拟存储软件","云运维管理软件","运维服务","运维项目","系统运维","平台运维","网络运维","设备运维","软件运维","机房运维","运维管理","项目运维","运维外包","网站运维","硬件运维","一体化运维","门户网站运维","微信运维","监控运维","信息运维","运维建设","运维支撑","托管运维","运维支持","IDC机房","信息产业","信息技术应用创新","信创产业发展","IT信息产业转型","信创产业链","新型基础建设","信创产品","信创体系","全民健康","疫情防控","院前急救","基层医疗","疫情监测","疫情预警","移动护理","发热监测","传染病监测","核酸监测","疫苗接种","电子健康"};
        String[] bbs = { "数字","数据","存储","数字化","数据库","大数据","智能","智慧","人脸","声纹","识别","智能化","科技化","智慧屏","机器人","边缘脑","云计算","云应用","医疗云","云医院","云医疗","医院云","云盘","桌面云","专有云","云服务","云租赁","云桌面","云主机","云终端","云支付","云市场","云平台","云存储","公有云","私有云","混合云","专属云","云设备","云闪付","云运维","云视频","云媒资","云客服","运维云","云安全","安全云","云容灾","云巡检","安防云","云监控","云实训","云录播","云办公","多媒体","融媒体","流媒体","超媒体","视频","微视频","门禁","天网","天眼","音视频","微机","掌机","5G","通信","通讯","网络","信号","基站","远程","网络","医疗网","等保","灾备","容灾","迁移","反欺诈","软件","软硬件","操作码","信创","IOT","会诊","诊断","胶片","超声","内镜","公卫","新基建","公共卫生","区块链","物联网","互联网+"};


        for (String aa : bbs) {
            String keyword = aa ;
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities02 = contentSolr.companyResultsBaoXian( "yyyymmdd:[20210101 TO 20210115] AND (progid:[0 TO 3] OR progid:5) AND zhaoBiaoUnit:* AND catid:[* TO 100] AND title:\""+aa+"\"", keyword, 101);
                log.info("keyword:{}查询出了size：{}条数据", keyword, mqEntities02.size());
                if (!mqEntities02.isEmpty()) {
                    for (NoticeMQ data : mqEntities02) {
                        list1.add(data);
                        if (!dataMap.containsKey(data.getContentid().toString())) {
                            list.add(data);
                            data.setKeyword(keyword);
                            dataMap.put(data.getContentid().toString(), "0");
                        }
                    }
                }
            }));
        }


        for (String aa : aas) {
            String keyword = aa ;
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities02 = contentSolr.companyResultsBaoXian( "yyyymmdd:[20210101 TO 20210115] AND (progid:[0 TO 3] OR progid:5) AND zhaoBiaoUnit:* AND catid:[* TO 100] AND allcontent:\""+aa+"\"", keyword, 101);
                log.info("keyword:{}查询出了size：{}条数据", keyword, mqEntities02.size());
                if (!mqEntities02.isEmpty()) {
                    for (NoticeMQ data : mqEntities02) {
                        list1.add(data);
                        if (!dataMap.containsKey(data.getContentid().toString())) {
                            list.add(data);
                            data.setKeyword(keyword);
                            dataMap.put(data.getContentid().toString(), "0");
                        }
                    }
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



        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() ->  getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
        log.info("数据全部跑完啦,总数量为：" +1);

    }

    private void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {
        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            String zhongUnit = resultMap.get("zhao_biao_unit") != null ? resultMap.get("zhao_biao_unit").toString() : "";
            String industry = myRuleUtils.getIndustry(zhongUnit);
            // 校验行业信息
            if ("政府机构-医疗".equals(industry) || "医疗单位-血站".equals(industry) || "医疗单位-急救中心".equals(industry) ||"医疗单位-医疗服务".equals(industry) ||
                    "医疗单位-疾控中心".equals(industry) || "医疗单位-卫生院".equals(industry) || "医疗单位-疗养院".equals(industry) || "医疗单位-专科医院".equals(industry) ||
                    "医疗单位-中医院".equals(industry) || "医疗单位-综合医院".equals(industry) || "商业公司-医疗服务".equals(industry)){
                resultMap.put("code", industry);
                cusDataFieldService.saveIntoMysql(resultMap);
            }

        }
    }
}
