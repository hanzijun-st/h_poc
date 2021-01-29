package com.qianlima.offline.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class KeyUtils {

    private final static HashMap<String, String> simpleAreaMap = new HashMap<>();



    public static synchronized HashMap<String, String> getSimpleAreaMap() {
        if (simpleAreaMap.isEmpty()) {
            try {
                ClassPathResource classPathResource = new ClassPathResource("map/yiyuan.txt");
                InputStream inputStream = classPathResource.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line = bufferedReader.readLine();
                while (StringUtils.isNotBlank(line)) {//BufferedReader有readLine()，可以实现按行读取
                    line = line.trim();
                    String[] arr = line.split(":");
                    simpleAreaMap.put(arr[0], arr[1]);
                    line = bufferedReader.readLine();
                }
            } catch (Exception e) {
                log.error("读取ka_simple_area 失败, 请查证原因");
            }
        }
        return simpleAreaMap;
    }


}


