package com.qianlima.offline.util;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * @author shenjiqiang
 * @Title: MatchWordUtils
 * @ProjectName project today
 * @Description: TODO
 * @date 2018/11/1 15:43
 */
public class MatchWordUtils {


    /**
     * n个词的分词匹配,适合多组不同的判断词
     *
     * @param content
     * @param lists
     * @return
     */
    public static boolean participleMatch(String content, String[]... lists) {
        Integer flag = 0;
        for (String[] list : lists) {
            for (String word : list) {
                if (content.contains(word)) {
                    flag++;
                    break;
                }
            }
        }
        if (flag.equals(lists.length)) {
            return true;
        }
        return false;
    }


    /**
     * 一个词的完全匹配
     *
     * @param synonymies
     * @param content
     * @return
     */
    public static boolean oneJudgeWordsMethod(String[] synonymies, String content) {
        for (String synonymy : synonymies) {
            if (content.contains(synonymy)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 两个词的完全匹配
     *
     * @param synonymies
     * @param words
     * @param content
     * @return
     */
    public static boolean twoJudgeWordsMethod(String[] synonymies, String[] words, String content) {
        for (String synonymy : synonymies) {
            for (String word : words) {
                StringBuilder matchWord = new StringBuilder(synonymy).append(word);
                if (content.contains(matchWord)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 三个词的完全匹配
     *
     * @param synonymies
     * @param words1
     * @param words2
     * @param content
     * @return
     */
    public static boolean threeJudgeWordsMethod(String[] synonymies, String[] words1, String[] words2, String content) {
        for (String synonymy : synonymies) {
            for (String word1 : words1) {
                for (String word2 : words2) {
                    StringBuilder matchWord = new StringBuilder(synonymy).append(word1).append(word2);
                    if (content.contains(matchWord)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 四个词的完全匹配
     *
     * @param synonymies
     * @param words1
     * @param words2
     * @param words3
     * @param content
     * @return
     */
    public static boolean fourJudgeWordsMethod(String[] synonymies, String[] words1, String[] words2, String[] words3, String content) {
        for (String synonymy : synonymies) {
            for (String word1 : words1) {
                for (String word2 : words2) {
                    for (String word3 : words3) {
                        StringBuilder matchWord = new StringBuilder(synonymy).append(word1).append(word2).append(word3);
                        if (content.contains(matchWord)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 查找含有多个关键词的元素，并把当前元素和下一元素的内容返回
     *
     * @param htmlDescription
     * @param keyWords
     * @return
     */
    public static String getHtmlTextWithKeyWords(String htmlDescription, String[] keyWords) {
        //<br>换成</p><p>
        htmlDescription = htmlDescription.replaceAll("(?i)<br[^>]*>", "</p><p>");
        StringBuilder text = new StringBuilder();
        LinkedHashSet<String> textSet = new LinkedHashSet<>();
        for (int i = 0; i < keyWords.length; i++) {
            textSet.addAll(getHtmlTextWithOneKeyWord(htmlDescription, keyWords[i]));
        }
        if (textSet.size() > 0) {
            textSet.forEach(textStr -> text.append(textStr).append("|"));
        }
        return text.toString();
    }

    /**
     * 查找含有关键词的元素，并把当前元素和下一元素的内容返回
     *
     * @param htmlDescription
     * @param keyWord
     * @return
     */
    private static HashSet<String> getHtmlTextWithOneKeyWord(String htmlDescription, String keyWord) {
        LinkedHashSet<String> textSet = new LinkedHashSet<>();
        Document document = Jsoup.parse(htmlDescription);
        Elements blocks = document.getElementsByTag("block");
        if (blocks != null) {
            blocks.remove();
        }
        if (StringUtils.isNotBlank(keyWord)) {
            String queryStr = ":containsOwn(" + keyWord + ")";
            Elements select = document.select(queryStr);
            if (select != null && select.size() > 0) {
                for (Element element : select) {
                    //当前元素的text内容
                    String curText = element.text();
                    textSet.add(curText);

                    if ("td".equals(element.tagName())) {
                        //下一元素的text内容
                        Element nextElement = element.nextElementSibling();
                        if (nextElement != null) {
                            String nextText = nextElement.text();
                            textSet.add(nextText);
                        }
                    }
                }
            }
        }
        return textSet;
    }

    /**
     * 查找含有多个关键词的元素，并把当前元素关键词后的内容和下一元素的内容返回
     *
     * @param htmlDescription
     * @param keyWords
     * @return
     */
    public static String getHtmlTextWithKeyWordsAlter(String htmlDescription, String[] keyWords) {
        //<br>换成</p><p>
        htmlDescription = htmlDescription.replaceAll("(?i)<br[^>]*>", "</p><p>");
        StringBuilder text = new StringBuilder();
        LinkedHashSet<String> textSet = new LinkedHashSet<>();
        for (int i = 0; i < keyWords.length; i++) {
            textSet.addAll(getHtmlTextWithOneKeyWordAlter(htmlDescription, keyWords[i]));
        }
        if (textSet.size() > 0) {
            textSet.forEach(textStr -> text.append(textStr).append("|"));
        }
        return text.toString();
    }

    /**
     * 查找含有关键词的元素，并把当前元素关键词后的内容和下一元素的内容返回
     *
     * @param htmlDescription
     * @param keyWord
     * @return
     */
    private static HashSet<String> getHtmlTextWithOneKeyWordAlter(String htmlDescription, String keyWord) {
        LinkedHashSet<String> textSet = new LinkedHashSet<>();
        Document document = Jsoup.parse(htmlDescription);
        Elements blocks = document.getElementsByTag("block");
        if (blocks != null) {
            blocks.remove();
        }
        if (StringUtils.isNotBlank(keyWord)) {
            String queryStr = ":containsOwn(" + keyWord + ")";
            Elements select = document.select(queryStr);
            if (select != null && select.size() > 0) {
                for (Element element : select) {
                    //当前元素的text内容
                    String curText = element.text();
                    int index = curText.indexOf(keyWord);
                    //只保存关键词后的内容
                    curText = curText.substring(index + keyWord.length(), curText.length());
                    textSet.add(curText);

                    if ("td".equals(element.tagName())) {
                        //下一元素的text内容
                        Element nextElement = element.nextElementSibling();
                        if (nextElement != null) {
                            String nextText = nextElement.text();
                            textSet.add(nextText);
                        }
                    }
                }
            }
        }
        return textSet;
    }
}
