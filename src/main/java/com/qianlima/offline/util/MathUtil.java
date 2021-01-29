package com.qianlima.offline.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MathUtil {

    private static final String DATE_SDF = "yyyy-MM-dd hh:mm:ss";
    /**
     *  校验金额是否是纯数字
     * @param amount
     * @return
     */
    public static boolean match(String amount){
        String reg = "^\\d+$|^\\d+.\\d+$";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(amount);
        return  m.matches();
    }

    /**
     * 去除正文中的html内容
     */
    public static String delHTMLTag(String htmlStr) {
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式
        Pattern p_script = Pattern.compile(
                regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll("");//过滤script标签
        Pattern p_style = Pattern.compile(
                regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); //过滤style标签
        Pattern p_html = Pattern.compile(
                regEx_html, Pattern.CASE_INSENSITIVE);

        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); //过滤html标签
        htmlStr = htmlStr.replaceAll("(\r?\n(\\s*\r?\n)+)", "\r\n");
        htmlStr = htmlStr.trim(); //返回文本字符串
        return htmlStr;
    }

    /**
     * 校验日期
     *
     * @param updateTimeStr
     * @return
     */
    public static boolean checkDate(String updateTimeStr) {
        boolean result = false;
        try {
            Date updateTimeData = DateUtils.parseDate(updateTimeStr, DATE_SDF);
            Date nowData = new Date();
            long updateTime = updateTimeData.getTime() + 24*60*60*1000*90L;
            if (updateTime > nowData.getTime()) {
                result = true;
            }
        } catch (Exception e) {
            log.info("日期格式不正确, 日期转换异常");
        }
        return result;
    }

    public static String processAboutContent(String content) {
        Document document = Jsoup.parse(content);
        Elements elements = document.select("a[href]");
        Integer elementSize = elements.size();
        for (Integer i = 0; i < elementSize; i++) {
            Element element = elements.get(i);
            if (element == null || document.select("a[href]") == null || document.select("a[href]").size() == 0) {
                break;
            }
            String elementStr = element.attr("href");
            if (StringUtils.isNotBlank(elementStr) && elementStr.contains("www.qianlima.com")) {
                if (element.is("a")) {
                    element.select("a").remove();
                }
            }
        }
        return document.body().html();
    }
}
