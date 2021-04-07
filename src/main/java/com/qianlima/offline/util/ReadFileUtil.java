package com.qianlima.offline.util;

import java.io.*;
import java.util.List;

/**
 * 将内容写文件中，指定存放的目录
 */
public class ReadFileUtil {

    public static void readFile(String url, String fileName, List<String> strList) {

        File file = new File(url+"/"+fileName);

        if(file.exists()) {
            System.err.println("如果已存在该名称的文件，将更新原文件内容");
            file.delete();
        }
        if(!file.exists()) {
            try {
                file.createNewFile();
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file),"GB2312");
                BufferedWriter bw = new BufferedWriter(osw);
                for (String s : strList) {
                    bw.write(s+"\r\n");
                }
                System.out.println("已完成100%，导入结束！");
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}