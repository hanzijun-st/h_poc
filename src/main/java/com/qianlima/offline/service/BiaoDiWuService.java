package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.util.QianlimaZTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class BiaoDiWuService {

    @Value("${zt.api.singleFieldUrl}")
    private String singleFieldUrl;

    @Value("${zt.api.saveContentIdUrl}")
    private String saveContentIdUrl;

    private static final String SELECT_SQL = "SELECT id, content_id as contentid from lala_data ";

    private static final String UPDATA_SQL_01 = "INSERT INTO haikang_biaodiwu (contentid, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

//    private static final String UPDATA_SQL_02 = "UPDATE haikang_no3_data set code = ? where content_id = ?";

    private static final String CHECK_SQL = "select status from phpcms_content where contentid = ?";

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;


    public void getAllBiaoDIWu() throws Exception{
//        String[] aas = {"183218734","183233516","183555096","185116700","184266071","182550903","182365076","183171945","184514361","183382761","183335062","184534889","183545153","185095164","182814443","183362856","182253272","184431352","184727712","184940170","185107047","182902019","183162128","184226437","183339368","185058857","184193593","184522797","184893662","183837629","184784078","182662318","181983856","182009355","183436472","181886366","182968998","181402525","181454837","184043866","182152625","185167465","181364413","182970657","184905543","181127723","184465363","182710059","183750297","183936103","180955697","182840155","180889869","184978947","183876578","180826644","184792086","183655192","183408808","180691402","180651075","183279695","185093101","184379869","185101082","183037590","182641092","184096370","183842753","180281110","180490160","182413506","184575328","183630672","182804612","180221789","184669997","182476867","180169505","183235315","183693610","181855948","179863927","179653925","179532156","182081737","179559599","181187313","181663094","185116689","184656142","184451968","183031554","182108513","179364030","183394603","179271285","178897185","181029112","182077140","178408840","180691433","183720855","181042889","178044911","182886093","177830756","177786989","180731251","177606673","181051052","184896626","180874821","180897786","184284051","177369187","180577128","180887320","180070914","179603905","181384991","180329188","181803479","181640117","181717981","181345249","177294355","177241434","180874272","180205107","177205106","180187999","183430590","184749000","180249917","178969541","177446897","179416929","178666004","176849685","176683759","180624677","178173941","181822952","183432981","178269540","181945377","181374836","177344526","179570225","177830769","180278475","180314456","179289820","181259748","177790223","178955240","179354140","180221954","176389189","177502178","179458807","178777120","178729318","180042507","177856800","177446884","178738696","177055645","179076004","176117042","181902227","177553653","175809741","180651818","180401277","180281250","174990973","180281641","178139722","175638374","181059406","177091298","176695215","182311843","181000881","177260538","176065722","175050054","177298127","177830739","180228110","177749505","176499605","176228229","175171948","174958232","177446867","177085752","178393129","177854459","176819304","176145422","174997010","176219322","185180312","175664198","179929273","176889912","178043326","175133335","175942400","176304766","175481649","177316625","175518376","177484149","176029192","175612457","175230153","175792128","175123565","176185024","175137767","175111082"};
//        for (String aa : aas) {
//            HashMap<String, Object> hashMap = new HashMap<>();
//            hashMap.put("contentid", aa);
//            notifyZhongTai(hashMap);
//        }
        getAllnotifyZhongTai();

        Thread.sleep(10*60*1000L);

        getAllgetTarget();
//        for (String aa : aas) {
//            HashMap<String, Object> hashMap = new HashMap<>();
//            hashMap.put("contentid", aa);
//            getTarget(hashMap);
//        }
        log.info("=============================任务完成做到最新===========================");
    }

    private String getAllgetTarget(){
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            List<Future> futureList = new ArrayList<>();
            List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(SELECT_SQL);
            if (maps != null && maps.size() > 0){
                log.info("任务查出来了 total：{}", maps.size());
                for (Map<String, Object> map : maps) {
                    futureList.add(executorService.submit(() ->  getTarget(map)));
                }
                for (Future future1 : futureList) {
                    try {
                        future1.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        executorService.shutdown();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        } catch (Exception e) {
            log.error("通知任务异常 e:{}", e);
        }
        return "通知任务完成做到最新";
    }



    private String

    getAllnotifyZhongTai(){
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            List<Future> futureList = new ArrayList<>();
            List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(SELECT_SQL);
            if (maps != null && maps.size() > 0){
                log.info("任务查出来了 total：{}", maps.size());
                for (Map<String, Object> map : maps) {
                    futureList.add(executorService.submit(() ->  notifyZhongTai(map)));
                }
                for (Future future1 : futureList) {
                    try {
                        future1.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        executorService.shutdown();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        } catch (Exception e) {
            log.error("通知任务异常 e:{}", e);
        }
        return "通知任务完成做到最新";
    }


    private void notifyZhongTai(Map<String, Object> map){
        String contentid = map.get("contentid").toString();
        if (checkPHPContent(contentid) == false){
            return;
        }
        //入库成功后通知中台组装标的物
        Map<String, Object> stringObjectMap = QianlimaZTUtil.saveContentId(saveContentIdUrl, contentid);
        if ("0".equals(stringObjectMap.get("returnCode"))) {
            log.info("通知中台组装标的物成功，成功infoId:{}", contentid);
        } else {
            log.error("通知中台组装标的物失败，失败infoId:{},中台返回状态码:{}", contentid, stringObjectMap.get("returnCode"));
            throw new RuntimeException("通知中台组装标的物失败");
        }
    }




    private void getTarget(Map<String, Object> map){
        String contentid = map.get("contentid").toString();
        if (checkPHPContent(contentid) == false){
            return;
        }
        Map<String, Object> targetMap = QianlimaZTUtil.getSingleField(singleFieldUrl, contentid, "target");
        if (targetMap != null){
            String returnCode = targetMap.get("returnCode").toString();
            if ("0".equals(returnCode)){
                JSONObject data = (JSONObject) targetMap.get("data");
                if (data == null) {
                    log.error("该条 info_id：{}，获取标的物返回结果异常", contentid);
                    throw new RuntimeException("获取标的物返回结果异常");
                }
                if (null != data.get("has_extract")) {
                    if (data.getBoolean("has_extract")){
                        if (null != data.get("target")){
                            String target = data.get("target").toString();
                            if (StringUtils.isNotBlank(target)){
                                Map biaodiwuMap = JSON.parseObject(target, Map.class);
                                if (biaodiwuMap.containsKey("targetDetails")){
                                    JSONArray targetDetails = (JSONArray) biaodiwuMap.get("targetDetails");
                                    for (Object targetDetail : targetDetails) {
                                        String detail = targetDetail.toString();
                                        Map detailMap = JSON.parseObject(detail, Map.class);
                                        String serialNumber = ""; //标的物序号
                                        String name = ""; //名称
                                        String brand = ""; //品牌
                                        String model = ""; //型号
                                        String number = ""; //数量
                                        String numberUnit = ""; //数量单位
                                        String price = ""; //单价
                                        String priceUnit = "";  //单价单位
                                        String totalPrice = ""; //总价
                                        String totalPriceUnit = ""; //总价单位
                                        if (detailMap.containsKey("serialNumber")){
                                            serialNumber = (String) detailMap.get("serialNumber");
                                        }
                                        if (detailMap.containsKey("name")){
                                            name = (String) detailMap.get("name");
                                        }
                                        if (detailMap.containsKey("brand")){
                                            brand = (String) detailMap.get("brand");
                                        }
                                        if (detailMap.containsKey("model")){
                                            model = (String) detailMap.get("model");
                                        }
                                        if (detailMap.containsKey("number")){
                                            number = (String) detailMap.get("number");
                                        }
                                        if (detailMap.containsKey("numberUnit")){
                                            numberUnit = (String) detailMap.get("numberUnit");
                                        }
                                        if (detailMap.containsKey("price")){
                                            price = (String) detailMap.get("price");
                                        }
                                        if (detailMap.containsKey("priceUnit")){
                                            priceUnit = (String) detailMap.get("priceUnit");
                                        }

                                        if (detailMap.containsKey("totalPrice")){
                                            totalPrice = (String) detailMap.get("totalPrice");
                                        }
                                        if (detailMap.containsKey("totalPriceUnit")){
                                            totalPriceUnit = (String) detailMap.get("totalPriceUnit");
                                        }

                                        String[] bbb = {"长城","江铃","五十铃","上汽大通"};
                                        for (String bb : bbb) {
                                            if (brand.contains(bb)){
                                                bdJdbcTemplate.update(UPDATA_SQL_01, contentid, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit);
                                            }
                                        }
                                    }
//                                    bdJdbcTemplate.update(UPDATA_SQL_02, "标的物检索", contentid);
                                }
                            }

                        }
                    }
                }
            }
        } else {
            log.error("获取标的物返回结果异常 contentid：{}", contentid);
            throw new RuntimeException("获取标的物返回结果异常");
        }
    }


    private boolean checkPHPContent(String contentid){
        boolean flag = false;
        List<Map<String, Object>> maps = gwJdbcTemplate.queryForList(CHECK_SQL, contentid);
        if (maps != null && maps.size() > 0 ){
            for (Map<String, Object> map : maps) {
                String status = map.get("status").toString();
                if ("99".equals(status)){
                    flag = true;
                }
            }
        }
        return flag;
    }

}
