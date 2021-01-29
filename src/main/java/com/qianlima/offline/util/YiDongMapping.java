package com.qianlima.offline.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class YiDongMapping {

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    private static final String SQL_KEY = "SELECT first, second, ka_first, ka_second, contain, exclude FROM yidong_industry_mapping where ka_first = ? and ka_second = ?";


    public Map<String, String> getYiDongIndustry(String kaFirst, String kaSecond, String zhaoBiaoUnit){
        if (StringUtils.isBlank(kaFirst) || StringUtils.isBlank(kaSecond)){
            return null;
        }
        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(SQL_KEY, kaFirst, kaSecond);

        String first = "";
        String second = "";
        HashMap<String, String> map = new HashMap<>();

        if (maps != null && maps.size() > 0){
            for (Map<String, Object> resultMap : maps) {
                first = resultMap.get("first") != null ? resultMap.get("first").toString() : "";
                second = resultMap.get("second") != null ? resultMap.get("second").toString() : "";
                String contain = resultMap.get("contain") != null ? resultMap.get("contain").toString() : "";
                String exclude = resultMap.get("exclude") != null ? resultMap.get("exclude").toString() : "";
                // 当包含词条件不为空时, 判断包含词条件
                boolean containFlag = isContainFlag(zhaoBiaoUnit, contain);
                // 当排除此条件不为空时, 判断排除词条件
                boolean excludeFlag = isExcludeFlag(zhaoBiaoUnit, exclude);
                // 当即满足包含词条件, 又满足排除词条件时, 返回对应的映射腾讯行业
                if (containFlag && excludeFlag){
                    map.put("firstIndustry", first);
                    map.put("secondIndustry", second);
                }
            }
        }
        return map;
    }


    private boolean isContainFlag(String unit, String containKeys) {
        boolean containFlag = false;
        if (StringUtils.isNotBlank(containKeys)) {
            String[] containArr = containKeys.split("、");
            for (String containStr : containArr) {
                if (unit.contains(containStr)) {
                    containFlag = true;
                    break;
                }
            }
        } else {
            containFlag = true;
        }
        return containFlag;
    }

    private boolean isExcludeFlag(String unit, String excludeKeys) {
        // 当排除此条件不为空时, 判断排除词条件
        boolean excludeFlag = true;
        if (StringUtils.isNotBlank(excludeKeys)) {
            String[] excludeArr = excludeKeys.split("、");
            for (String excludeStr : excludeArr) {
                if (unit.contains(excludeStr)) {
                    excludeFlag = false;
                    break;
                }
            }
        }
        return excludeFlag;
    }

}
