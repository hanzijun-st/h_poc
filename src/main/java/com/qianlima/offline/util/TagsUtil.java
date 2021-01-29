package com.qianlima.offline.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TagsUtil {

    private static String fieldsUrl = "http://datafetcher.intra.qianlima.com/dc/bidding/fields";

    public static List<String> getTagsByContentId(String contentId,  boolean checkStatus) {
        List<String> list = new ArrayList<>();

        Map<String, Object> map = QianlimaZTUtil.getFields(fieldsUrl, contentId, "tags", "tags");
        if (map == null) {
            log.error("该条 info_id：{}，调取中台, 获取标签字段字段失败", contentId);
            throw new RuntimeException("获取标签字段字段失败");
        }
        String returnCode = (String) map.get("returnCode");
        if ("500".equals(returnCode) || "1".equals(returnCode)) {
            log.error("该条 info_id：{}，获取标签字段字段失败", contentId);
            return null;
        } else if ("0".equals(returnCode)) {
            JSONObject data = (JSONObject) map.get("data");
            if (data == null) {
                log.error("该条 info_id：{}，获取标签字段字段失败", contentId);
                throw new RuntimeException("获取标签调取中台失败");
            }
            //判断中台提取状态
            JSONArray fileds = data.getJSONArray("fields");
            if (fileds != null && fileds.size() > 0) {
                for (int d = 0; d < fileds.size(); d++) {
                    JSONObject object = fileds.getJSONObject(d);
                    if (null != object.get("tags")) {
                        boolean tqzt = object.getBoolean("has_extract");
                        if (checkStatus && !tqzt) {
                            log.error("该条 info_id：{}，获取标签调取中台extract_budget状态是未提取", contentId);
                            return null;
                        }
                        String tags = object.getString("tags");
                        List<Map> maps = JSONArray.parseArray(tags, Map.class);
                        if (maps != null && maps.size() > 0){
                            for (Map resultMap : maps) {
                                if (resultMap.containsKey("tag_id")){
                                    list.add(resultMap.get("tag_id").toString());
                                }
                            }
                        }
                    }
                }
            }
        }
        return list;
    }
}
