package com.qianlima.offline.service;

import java.util.*;

/**
 * 敏感词过滤工具类
 *
 */
public class SensitivewordEngine {

    /**
     * 敏感词库
     */
    public static HashMap sensitiveWordMap = new HashMap();


    public static synchronized void initKeyWord(List<String> list) {
        if (sensitiveWordMap.isEmpty()){
            if (list != null && list.size() > 0){
                addSensitiveWordToHashMap(list);
            }
        }
    }

    /**
     * 封装敏感词库
     */
    private static void addSensitiveWordToHashMap(List<String> keyWordSet) {
        // 初始化HashMap对象并控制容器的大小
        sensitiveWordMap = new HashMap(keyWordSet.size());
        // 敏感词
        String key = null;
        // 用来按照相应的格式保存敏感词库数据
        Map nowMap = null;
        // 用来辅助构建敏感词库
        Map<String, String> newWorMap = null;
        // 使用一个迭代器来循环敏感词集合
        Iterator<String> iterator = keyWordSet.iterator();
        while (iterator.hasNext()) {
            key = iterator.next();
            // 等于敏感词库，HashMap对象在内存中占用的是同一个地址，所以此nowMap对象的变化，sensitiveWordMap对象也会跟着改变
            nowMap = sensitiveWordMap;
            for (int i = 0; i < key.length(); i++) {
                // 截取敏感词当中的字，在敏感词库中字为HashMap对象的Key键值
                char keyChar = key.charAt(i);
                // 判断这个字是否存在于敏感词库中
                Object wordMap = nowMap.get(keyChar);
                if (wordMap != null) {
                    nowMap = (Map) wordMap;
                } else {
                    newWorMap = new HashMap<String, String>();
                    newWorMap.put("isEnd", "0");
                    nowMap.put(keyChar, newWorMap);
                    nowMap = newWorMap;
                }
                // 如果该字是当前敏感词的最后一个字，则标识为结尾字
                if (i == key.length() - 1) {
                    nowMap.put("isEnd", "1");
                }
            }
        }
    }

    /**
     * 过滤所有敏感词
     */
    public static int maxMatchType = 2;

    /**
     * 获取敏感词内容
     */
    public static Set<String> getSensitiveWord(String txt, int matchType) {
        Set<String> sensitiveWordList = new HashSet<String>();
        for (int i = 0; i < txt.length(); i++) {
            int length = checkSensitiveWord(txt, i, matchType);
            if (length > 0) {
                // 将检测出的敏感词保存到集合中
                sensitiveWordList.add(txt.substring(i, i + length));
                i = i + length - 1;
            }
        }
        return sensitiveWordList;
    }

    /**
     * 检查敏感词数量
     */
    public static int checkSensitiveWord(String txt, int beginIndex, int matchType) {
        boolean flag = false;
        // 记录敏感词数量
        int matchFlag = 0;
        char word = 0;
        Map nowMap = sensitiveWordMap;
        for (int i = beginIndex; i < txt.length(); i++) {
            word = txt.charAt(i);
            // 判断该字是否存在于敏感词库中
            nowMap = (Map) nowMap.get(word);
            if (nowMap != null) {
                matchFlag++;
                // 判断是否是敏感词的结尾字，如果是结尾字则判断是否继续检测
                if ("1".equals(nowMap.get("isEnd"))) {
                    flag = true;
                    // 判断过滤类型，如果是小过滤则跳出循环，否则继续循环
                    if (SensitivewordEngine.maxMatchType == matchType) {
                        break;
                    }
                }
            } else {
                break;
            }
        }
        if (!flag) {
            matchFlag = 0;
        }
        return matchFlag;
    }

}