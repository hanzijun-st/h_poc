package com.qianlima.offline.util;

public class ExcelUtils {

    public static String delHTMLTag(String htmlStr) {
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式
        java.util.regex.Pattern p_script = java.util.regex.Pattern.compile(
                regEx_script, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll("");//过滤script标签
        java.util.regex.Pattern p_style = java.util.regex.Pattern.compile(
                regEx_style, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); //过滤style标签
        java.util.regex.Pattern p_html = java.util.regex.Pattern.compile(
                regEx_html, java.util.regex.Pattern.CASE_INSENSITIVE);

        java.util.regex.Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); //过滤html标签
        htmlStr = htmlStr.replaceAll("(\r?\n(\\s*\r?\n)+)", "\r\n");
        htmlStr = htmlStr.trim(); //返回文本字符串
        int idx;
        if ((idx = htmlStr.indexOf("<block>")) > -1) {
            return htmlStr.substring(0, idx);
        }
        return htmlStr;
    }

}
