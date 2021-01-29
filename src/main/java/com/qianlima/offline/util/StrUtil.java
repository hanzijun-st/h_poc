package com.qianlima.offline.util;



/**
 * Created by Administrator on 2021/1/19.
 */
public class StrUtil {

    /**
     * 字符串是否为空
     * null 或 者长度为0
     *
     * @author 静心事成
     * @param str 字符串
     * @return boolean
     * */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0 || str.equals("") || trim(str).length()==0;
    }

    /**
     * 字符串是否不为空
     * 不为null 并且 长度不为0
     *
     * @author 静心事成
     * @param str 字符串
     * @return boolean
     * */
    public static boolean isNotEmpty(CharSequence str) {
        return false == isEmpty(str);
    }

    /**
     * 删除某个字符
     * @param str
     * @param delChar
     * @return
     */
    public static String deleteString(String str, char delChar) {
        StringBuffer stringBuffer = new StringBuffer("");
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != delChar) {
                stringBuffer.append(str.charAt(i));
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 接口调用完提示返回
     * @return
     */
    public static String getPutStr(){
        StringBuffer s = new StringBuffer();
        s.append("===============================本次任务结束=============================");
        s.append("===================================================================");
        return s.toString();
    }

    /**
     * 保存完数据库后返回提示
     * @return
     */
    public static String getSaveDataStr(){
        StringBuffer s = new StringBuffer();
        s.append("===============================数据保存完成=============================");
        s.append("===================================================================");
        return s.toString();
    }

    public static CharSequence trim(CharSequence cs) {
        int len = cs.length();
        int st = 0;

        while ((st < len) && (cs.charAt(st) <= ' ')) {
            st++;
        }
        while ((st < len) && (cs.charAt(len - 1) <= ' ')) {
            len--;
        }
        return ((st > 0) || (len < cs.length())) ? cs.subSequence(st, len) : cs;
    }

    /**
     *  \\s* 可以匹配空格、制表符、换页符等空白字符的其中任意一个。
     * @param str
     * @return
     */
    public static String delAllPlace(String str){
        //可以替换大部分空白字符， 不限于空格 ；
        return str.replaceAll("\\s*", "");
    }
}
