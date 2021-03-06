package com.qianlima.offline.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.extract.TargetService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NewBiaoDiWuService {

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    // 标的物匹配到的关键词
    private String[] keywords = {"牙科综合治疗台","口腔CT","口腔综合治疗台","牙科综合治疗椅","口腔CBCT","牙科手机","口腔颌面锥形束计算机体层摄影设备","涡轮手机","锥形束CT","口腔X射线数字化体层摄影设备","CBCT","电动牙科椅","牙科高速手机","牙科CT","口腔X射线机","牙科影像板扫描仪","口腔数字化影像系统","口腔颌面锥形束计算机体层摄影系统","口腔影像设备","口腔X射线数字化体层摄影系统","牙科慢速手机","大视野CBCT","锥形束计算机体层摄影设备","液压牙科椅","牙科CBCT","电动手机","机械牙科椅","锥形束影像CT","口腔摄影系统","种植机","牙科椅","牙椅","手术椅","种植椅","口腔颌面曲面体层X射线机","曲面体层X射线机","口腔三合一CBCT","口腔数字化体层摄影设备","口腔影像板扫描成像系统","口腔影像板扫描仪",
            "牙科影像板扫描系统","牙科X射线机","口内X光机","口内x射线机","牙片机","耳鼻喉cbct","耳鼻喉ct","多功能cbct","多功能ct","专科cbct"};

    private String SQL = "insert into han_new_bdw(infoId, sum, sum_unit, keyword, serial_number, name, brand, model, " +
            "number, number_unit, price, price_unit, total_price, total_price_unit, configuration) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public void handleForData(Long contentId){
        String result = TargetService.extract(contentId,"http://172.18.30.243:2023/inspect");
        if (StringUtils.isNotBlank(result)){
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject != null && jsonObject.containsKey("content_target")){
                JSONObject resultObject = jsonObject.getJSONObject("content_target");
                if (resultObject != null && resultObject.containsKey("target_details")){
                    String sum = resultObject.getString("sum");
                    String sum_unit = resultObject.getString("sum_unit");
                    JSONArray targetDetails = resultObject.getJSONArray("target_details");
                    if (targetDetails != null && targetDetails.size() > 0){
                        for (int i = 0; i < targetDetails.size(); i++) {
                            String serial_number = "";
                            String name = "";
                            String brand = "";
                            String model = "";
                            String number = "";
                            String number_unit = "";
                            String price = "";
                            String price_unit = "";
                            String total_price = "";
                            String total_price_unit = "";
                            String configuration = "";
                            String keyword = "";
                            JSONObject finalObject = targetDetails.getJSONObject(i);
                            if (finalObject != null){
                                serial_number = finalObject.getString("serial_number");
                                name = finalObject.getString("name");
                                brand = finalObject.getString("brand");
                                model = finalObject.getString("model");
                                number = finalObject.getString("number");
                                number_unit = finalObject.getString("number_unit");
                                price = finalObject.getString("price");
                                price_unit = finalObject.getString("price_unit");
                                total_price = finalObject.getString("total_price");
                                total_price_unit = finalObject.getString("total_price_unit");
                                JSONArray configurations = finalObject.getJSONArray("configurations");
                                if (configurations != null && configurations.size() > 0){
                                    for (int j = 0; j < configurations.size(); j++) {
                                        JSONObject jsonObject1 = configurations.getJSONObject(j);
                                        String key = jsonObject1.getString("key");
                                        String value = jsonObject1.getString("value");
                                        configuration += key + "：" + value + "：";
                                    }
                                }
                                if (StringUtils.isNotBlank(configuration)){
                                    configuration = configuration.substring(0, configuration.length() - 1);
                                }
                                // 进行匹配关键词操作
                                if (keywords != null && keywords.length > 0){
                                    String allField = name + "&" + brand + "&" + model + "&" + configuration;
                                    for (String key : keywords) {
                                        if (allField.toUpperCase().contains(key.toUpperCase())){
                                            keyword += key + "，";
                                        }
                                    }
                                    if (StringUtils.isNotBlank(keyword)){
                                        keyword = keyword.substring(0, keyword.length() - 1);
                                    }
                                }
                                // 进行数据保存操作
                            }
                            // 进行数据库保存操作
                            bdJdbcTemplate.update(SQL, contentId, sum, sum_unit, keyword, serial_number, name, brand, model, number, number_unit, price, price_unit, total_price, total_price_unit, configuration);
                        }
                    }
                }
            }
        }
    }

}
