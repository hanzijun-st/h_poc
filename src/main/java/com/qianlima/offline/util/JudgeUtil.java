package com.qianlima.offline.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class JudgeUtil {
    private static final Set<String> URL_LIST = new HashSet<>();

    static {
        try {

            ClassPathResource classPathResource = new ClassPathResource("source/yuan.txt");
            InputStream inputStream = classPathResource.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line = bufferedReader.readLine();
            while (StringUtils.isNotBlank(line)) {
                URL_LIST.add(line.toUpperCase());
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            log.error("读取源失败:{}",e);
        }
    }

    /**
     * 根据输入的源头地址，判断是否为政府类网站，并返回对应的网站名称
     * @param url
     * @return
     */
    public static String judge(String url) {
        if (StringUtils.isNotBlank(url)) {
            url = url.replace("http://", "").toUpperCase();
            for (String str : URL_LIST) {
                String[] yuans = str.split(":");
                if (yuans != null && yuans.length == 2) {
                    if (url.startsWith(yuans[0])) {
                        return yuans[1];
                    }
                }
            }
        }
        return null;
    }

    /**
     * 根据输入的源头地址，判断是否为政府类网站，并返回对应的网站名称
     * @param url
     * @return
     */
    public static Map<String,String> judgeForMap(String url) {
        Map<String, String> resultMap = new HashMap<>();
        if (StringUtils.isNotBlank(url)) {
            url = url.replace("http://", "").toUpperCase();
            for (String str : URL_LIST) {
                String[] yuans = str.split(":");
                if (yuans != null && yuans.length == 2) {
                    if (url.startsWith(yuans[0])) {
                        resultMap.put("website", yuans[0]);
                        resultMap.put("type", yuans[1]);
                    }
                }
            }
        }
        return resultMap;
    }
}
