package com.qianlima.offline.util;

/**
 * 工具类
 * 2016年6月2日18:25:12
 *
 * @author Administrator
 */

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LogUtils {

    public static List<String> readRule(String fileName) throws IOException {
        List<String> list = new ArrayList<>();
        ClassPathResource classPathResource = new ClassPathResource("source/"+fileName+".txt");
        InputStream inputStream = classPathResource.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line = bufferedReader.readLine();
        while (StringUtils.isNotBlank(line)) {//BufferedReader有readLine()，可以实现按行读取
            list.add(line);
            line = bufferedReader.readLine();
        }
        return list;
    }

    public static List<String> readKeyWord(String path) throws Exception {
        List<String> list = new ArrayList<>();
        File file = new File(path);
        if (!file.exists() || file.isDirectory()) {
            file.createNewFile();
        }
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = bufferedReader.readLine();
        while (StringUtils.isNotBlank(line)) {//BufferedReader有readLine()，可以实现按行读取
            list.add(line);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return list;
    }

    //招标单位联系人、联系电话。中标单位联系人、联系电话 多个的用的英文逗号分隔。
    public static String format(String field) {
        if (StringUtils.isEmpty(field)) {
            return "";
        }
        return field.replaceAll("，", ",");
    }

    /**
     * 针对中台返回的null，做特殊处理
     *
     * @param cs
     * @return
     */
    public static boolean isNotBlank(CharSequence cs) {
        return StringUtils.isNotBlank(cs) && !"null".equals(cs);
    }

}
