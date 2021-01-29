package com.qianlima.offline.service.offline;

import com.qianlima.offline.bean.Area;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.middleground.BaiLianZhongTaiService;
import com.qianlima.offline.middleground.NewZhongTaiService;
import com.qianlima.offline.middleground.NotBaiLianZhongTaiService;
import com.qianlima.offline.util.ContentSolr;
import com.qianlima.offline.util.IctContentSolr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class DaHuaService {

    @Autowired
    private ContentSolr contentSolr;

    @Autowired
    private IctContentSolr ictContentSolr;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    private NotBaiLianZhongTaiService notBaiLianZhongTaiService;

    @Autowired
    private NewZhongTaiService newZhongTaiService;

    @Autowired
    private BaiLianZhongTaiService baiLianZhongTaiService;


    HashMap<Integer, Area> areaMap = new HashMap<>();

    //地区
    @PostConstruct
    public void init() {
        List<Map<String, Object>> maps = gwJdbcTemplate.queryForList("SELECT * FROM phpcms_area");
        for (Map<String, Object> map : maps) {
            Area area = new Area();
            area.setAreaid(Integer.valueOf(map.get("areaid").toString()));
            area.setName(map.get("name").toString());
            area.setParentid(map.get("parentid").toString());
            area.setArrparentid(map.get("arrparentid").toString());
            areaMap.put(Integer.valueOf(map.get("areaid").toString()), area);
        }
    }

    //调取中台数据
    public void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {
        boolean result = notBaiLianZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = notBaiLianZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            notBaiLianZhongTaiService.saveIntoMysql(resultMap);
        }
    }

    //浙江大华
    public void getDaHuaSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();


        String[] aaa = {"摄像头","视频监控","视频预警","监控系统","监控设备","视频信号","视频设备","录像机","摄像机","拍摄仪","监控台","高拍仪","摄像设备","摄录设备","照相设备","球机设备","监视设备","会议设备","抓拍设备","监控电子屏","监控分析设备","视频卡","视频会议室专用设备","监督设备","视频终端","球形投影仪","球形摄像机","球型投影仪","球型摄像机","轻型云台","手持云台","液压云台","云台摄像仪","全景云台","拼接云台","会议云台","视频会议","会议视频","视频会议网络系统","会议系统","视频会议室改造","上线视频会议","视频会议室建设","视频会议室技改","视频会议多点控制器","音视频会议","音视频会议室","监控工程","监控建设","监控服务","监控点建设","监控点项目","监控站项目","监控项目","全景监控","高清监控","高点监控","高空监控","视频电警","视频监察","视频监测","视频监督","视频监管","视频监视","视频督促","视频督察","视频督查","视频防控","监视控制","监测视频","监控视频","监控音视频","警务视频","动环监控","集中监控","互联网监控","实时监控","监控报警","执法视频保全","摄像系统","电子警察","电子停车","电子眼","安防监控","监控专网","监控联网应用","视频安防","视频联防","视频安保","视频巡查","视频巡逻","视频抓拍","视频警察","视频监测站","视频治安","视频查勘","视频考场","报警视频","在线视频监测","安防视频","监管视频","监督视频","视频图像","图像及视频分析","视频系统","视音频资源库","视频存储采购","视频联合辅助运营","视频采集","视频分析","视频解析","视频侦查","视频检测","视频定位","定位视频","安联网","视频督导","视频取证","安全防卫","安全护卫","安全防范","安防中控","安防实训","安防执勤","安防指挥","安防监管","安防管控","安防维保","安防防火","安防防范","治安防控","安防集成","摄像安防","移动安防","自动化安防","安防联网","云防系统","安全保障系统","安全探测系统","安全监管系统","安全系统","安全防护系统","安全预警系统","安监系统","安监预警系统","安防云系统","安防子系统","安防广播系统","安防模拟系统","安防系统","安防集成系统","人脸识别","语音识别","报警系统","巡更系统","安检安防","门禁系统","对讲系统","可视化对讲","可视对讲","安防","一键报警","智能安防","安防联动","智慧安防","天网工程","天眼工程","平安城市","雪亮工程","天眼系统","智慧城市","智能城市","智慧城镇","智能城镇","城市大脑","城市超脑","数字城市","感知城市","无线城市","数字城镇","感知城镇","无线城镇","智能交通","智能机场","智能车站","智能客运","智能客流","智能物流","智能调度","智能运输","智能货运","智慧交通","智慧机场","智慧车站","智慧客运","智慧客流","智慧物流","智慧调度","智慧运输","智慧货运"};
        String[] blacks = {"装修","修缮","保养","安防职业技术学院","安防项目审计","工程监理","耗材项目","智能交通产品事业部","智能交通产业园","检测器转换","物业服务管理","物业管理服务","广告道闸","防疫物资","办公场所家具","购买硒鼓","光纤租赁","勘察设计","楼外幕墙","变电站工程","仓库租赁","整车业务","土石方平整","视频制作","视频汇报片","维保服务","运输服务","除险加固工程","景观提升工程","安保服务","商铺租赁","废厚铁","金属软管采购","视频宣传","音响购买","信号灯","音响设备","打印机","电线电缆","混凝土材料","镀锌圆钢","镀锌扁铁","镀锌钢管","计算机配件","维保","维护","耗材","修复","修理","维修","设备租赁","更换UPS","经营性租赁","整修工程","专线租赁","审计服务"};


        for (String aa : aaa) {
            futureList1.add(executorService1.submit(() -> {
                String key = aa;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200701 TO 20200931] AND progid:[0 TO 2] AND catid:[* TO 100] AND (title:\"" + aa + "\")", key, 1998);
                log.info(key.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            for (String black : blacks) {
                                if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag) {
                                list1.add(data);
                                data.setKeyword(key);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
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


        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList(aaa));

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + list1.size());
        System.out.println("去重之后的数据量：" + list.size());


//        if (list != null && list.size() > 0) {
//            ExecutorService executorService = Executors.newFixedThreadPool(80);
//            List<Future> futureList = new ArrayList<>();
//            for (NoticeMQ content : list) {
//                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
//            }
//            for (Future future : futureList) {
//                try {
//                    future.get();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//            executorService.shutdown();
//        }
    }


    private static final String ZJ_SELECT_SQL_01 = "SELECT content_id FROM lala_data ";
    private static final String ZJ_SELECT_SQL_02 = "SELECT content_id,title FROM lala_data WHERE content_id = ? ";
    private static final String ZJ_UPDATE_SQL_01 = "UPDATE lala_data SET keyword = ? WHERE content_id = ? ";

    //中国信通院——追加多个关键词
    public void getZJbiaozhuSolrAllField() {

        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(ZJ_SELECT_SQL_01);
        for (Map<String, Object> map : maps) {

            String contentid = map.get("content_id") != null ? map.get("content_id").toString() : "";

            ArrayList<String> list = new ArrayList<>();
            list.add(contentid);

            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (String id : list) {
                    futureList.add(executorService.submit(() -> zhuijiaCodeAllData(id)));
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
        }

    }

    String[] aaa = {"摄像头","视频监控","视频预警","监控系统","监控设备","视频信号","视频设备","录像机","摄像机","拍摄仪","监控台","高拍仪","摄像设备","摄录设备","照相设备","球机设备","监视设备","会议设备","抓拍设备","监控电子屏","监控分析设备","视频卡","视频会议室专用设备","监督设备","视频终端","球形投影仪","球形摄像机","球型投影仪","球型摄像机","轻型云台","手持云台","液压云台","云台摄像仪","全景云台","拼接云台","会议云台","视频会议","会议视频","视频会议网络系统","会议系统","视频会议室改造","上线视频会议","视频会议室建设","视频会议室技改","视频会议多点控制器","音视频会议","音视频会议室","监控工程","监控建设","监控服务","监控点建设","监控点项目","监控站项目","监控项目","全景监控","高清监控","高点监控","高空监控","视频电警","视频监察","视频监测","视频监督","视频监管","视频监视","视频督促","视频督察","视频督查","视频防控","监视控制","监测视频","监控视频","监控音视频","警务视频","动环监控","集中监控","互联网监控","实时监控","监控报警","执法视频保全","摄像系统","电子警察","电子停车","电子眼","安防监控","监控专网","监控联网应用","视频安防","视频联防","视频安保","视频巡查","视频巡逻","视频抓拍","视频警察","视频监测站","视频治安","视频查勘","视频考场","报警视频","在线视频监测","安防视频","监管视频","监督视频","视频图像","图像及视频分析","视频系统","视音频资源库","视频存储采购","视频联合辅助运营","视频采集","视频分析","视频解析","视频侦查","视频检测","视频定位","定位视频","安联网","视频督导","视频取证","安全防卫","安全护卫","安全防范","安防中控","安防实训","安防执勤","安防指挥","安防监管","安防管控","安防维保","安防防火","安防防范","治安防控","安防集成","摄像安防","移动安防","自动化安防","安防联网","云防系统","安全保障系统","安全探测系统","安全监管系统","安全系统","安全防护系统","安全预警系统","安监系统","安监预警系统","安防云系统","安防子系统","安防广播系统","安防模拟系统","安防系统","安防集成系统","人脸识别","语音识别","报警系统","巡更系统","安检安防","门禁系统","对讲系统","可视化对讲","可视对讲","安防","一键报警","智能安防","安防联动","智慧安防","天网工程","天眼工程","平安城市","雪亮工程","天眼系统","智慧城市","智能城市","智慧城镇","智能城镇","城市大脑","城市超脑","数字城市","感知城市","无线城市","数字城镇","感知城镇","无线城镇","智能交通","智能机场","智能车站","智能客运","智能客流","智能物流","智能调度","智能运输","智能货运","智慧交通","智慧机场","智慧车站","智慧客运","智慧客流","智慧物流","智慧调度","智慧运输","智慧货运"};

    //追加多个关键词
    private void zhuijiaCodeAllData(String id) {

        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(ZJ_SELECT_SQL_02,id);

        for (Map<String, Object> map : maps) {
            String contentid = map.get("content_id") != null ? map.get("content_id").toString() : "";
            String title = map.get("title") != null ? map.get("title").toString() : "";

            String keyword = "";
            for (String aa : aaa) {
                if (title.contains(aa)) {
                    keyword += (aa + "、");
                }
            }

            if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.substring(0, keyword.length() - 1);
                bdJdbcTemplate.update(ZJ_UPDATE_SQL_01,keyword, contentid);
                log.info("contentId:{} 数据关键词追加处理成功！==== 关键词:{} ", contentid ,keyword);
            }
        }

//        NoticeMQ noticeMQ = new NoticeMQ();
//        noticeMQ.setContentid(Long.valueOf(contentid));
//
//        String allInfos = null;
//        allInfos = newZhongTaiService.handleZhongTaiGetResultMapWithContent(noticeMQ);
//
//        // 获取标文
//        String info = ExcelUtils.delHTMLTag(allInfos);

    }


    private static final String TXY_SELECT_SQL_01 = "SELECT content_id FROM jyf_data ";
    private static final String TXY_SELECT_SQL_02 = "SELECT content_id,keyword FROM jyf_data WHERE content_id = ? ";
    private static final String TXY_UPDATE_SQL_01 = "UPDATE jyf_data SET code = ? WHERE content_id = ? ";

    //中国通信院——标注关键词对应的所属“一级产品”和“二级产品”分类
    public void getTXYbiaozhuSolrAllField() {

        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(TXY_SELECT_SQL_01);
        for (Map<String, Object> map : maps) {

            String contentid = map.get("content_id") != null ? map.get("content_id").toString() : "";

            ArrayList<String> list = new ArrayList<>();
            list.add(contentid);

            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (String id : list) {
                    futureList.add(executorService.submit(() -> biaozhuKeyWordsAllData(id)));
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
        }

    }
    private void biaozhuKeyWordsAllData(String id) {

        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(TXY_SELECT_SQL_02,id);

        for (Map<String, Object> map : maps) {
            String contentid = map.get("content_id") != null ? map.get("content_id").toString() : "";
            String keyword = map.get("keyword") != null ? map.get("keyword").toString() : "";
            String[] splitKeyword = keyword.split("、");
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("SELECT one, pid FROM loiloi_code_data where code in ( ");

            int num = 0 ;
            for (int i = 0; i < splitKeyword.length; i++) {
                num ++;
                stringBuilder.append("'").append(splitKeyword[i]).append("'");
                if ( num != splitKeyword.length){
                    stringBuilder.append(",");
                }
            }

            stringBuilder.append(") order by num asc limit 1");

            String s = stringBuilder.toString();

            List<Map<String, Object>> codeMaps = bdJdbcTemplate.queryForList(stringBuilder.toString());
            for (Map<String, Object> codeMap : codeMaps) {
                String code = "";
                String one = codeMap.get("one") != null ? codeMap.get("one").toString() : "";
//                String two = codeMap.get("two") != null ? codeMap.get("two").toString() : "";
                code += "产品词标签" + "-" + one ;
                bdJdbcTemplate.update(TXY_UPDATE_SQL_01, one, contentid);
                log.info("contentId:{} 数据 产品词标签 标注处理成功！==== 产品词标签:{} ", contentid ,code);
            }

        }



    }


}
