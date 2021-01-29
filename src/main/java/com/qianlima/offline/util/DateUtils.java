package com.qianlima.offline.util;


import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author mahao
 * @version 0.0.1
 * @time 2019/6/19
 * @describe 时间操作工具类
 */
public class DateUtils {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    private static final String date_format = "yyyy-MM-dd HH:mm:ss";

    private static ThreadLocal<DateFormat> threadLocal = new ThreadLocal<DateFormat>();



    public static DateFormat getDateFormat() {

        DateFormat df = threadLocal.get();

        if(df==null){

            df = new SimpleDateFormat(date_format);

            threadLocal.set(df);
        }
        return df;

    }

    public static String formatDate(Date date) throws ParseException {

        return getDateFormat().format(date);

    }

    /**
     * 线程安全-将字符串时间转换成 DATE类型
     * @param strDate
     * @return
     * @throws ParseException
     */
    public static Date parse(String strDate) throws ParseException {

        return getDateFormat().parse(strDate);

    }


    public static String getFormatDateStr(Long timestmp) {
        if (timestmp == null) {
            return "";
        }
        return SDF.format(new Date(timestmp));
    }

    public static Date getFormatDate(Date date) {
        if (date == null) {
            return new Date();
        }
        Date parse;
        try {
            String format = SDF.format(new Date());
            parse = SDF.parse(format);
        } catch (Exception e) {
            e.printStackTrace();
            parse = new Date();
        }

        return parse;
    }

    /**
     * 返回 指定格式的日期字符串
     *
     * @param timestmp dateFormat
     * @return format date String
     */
    public static String getFormatDateStr(Long timestmp, String dateFormat) {
        if (timestmp == null) {
            return "";
        }
        SimpleDateFormat SDF = new SimpleDateFormat(dateFormat);
        return SDF.format(new Date(timestmp));
    }


    /**
     * java.util.Date 返回 yyyy-MM-dd String 类型
     *
     * @param date
     * @return format date String
     */
    public static String getFormatDateStr(Date date) {
        if (date == null) {
            return "";
        }
        return SDF.format(date);
    }


    /**
     * 返回 指定格式的日期字符串
     *
     * @param date dateFormat(例如 yyyy-MM-dd)
     * @return format date String
     */
    public static String getFormatDateStr(Date date, String dateFormat) {
        if (date == null) {
            return "";
        }
        //SimpleDateFormat SDF = new SimpleDateFormat(dateFormat);
        return DateFormatUtils.format(date,dateFormat);
    }


    /**
     * java.lang.Integer Epoch 返回 yyyy-MM-dd String 类型
     *
     * @param num
     * @return format date String
     */
    public static String getFormatEpochStr(int num) {
        long time = num * 1000L;
        return SDF.format(new Date(time));
    }

    /**
     * yyyy-MM-dd 返回 java.util.Date
     * 不会返回null, 产生异常时返回当前时间的Date对象的实例
     *
     * @param formatStr
     * @return
     */
    public static Date parseDateFromDateStr(String formatStr) {
        Date date;
        try {
            date = SDF.parse(formatStr);
        } catch (Exception exp) {
            date = new Date();
        }
        return date;
    }

    /**
     * 获取自昨天12点到今天12点的int类型
     *
     * @return {昨天12点epoch_int, 今天12点epoch_int}
     */
    public static int[] getDate() {
        Calendar calendar = getCalendar();
        int[] res = new int[2];
        res[1] = (int) (calendar.getTimeInMillis() / 1000);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        res[0] = (int) (calendar.getTimeInMillis() / 1000);
        return res;
    }

    /**
     * 获取yyyy-MM-dd 形式的前后日期时间
     * [0] prev_day [1] current day
     *
     * @param formatDate
     * @return
     */
    public static int[] getDate(String formatDate) {
        Calendar calendar = getCalendar();
        String[] dateStr = formatDate.split("-");
        calendar.set(Integer.valueOf(dateStr[0]), Integer.valueOf(dateStr[1]) - 1, Integer.valueOf(dateStr[2]));
        int[] res = new int[2];
        res[1] = (int) (calendar.getTimeInMillis() / 1000);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        res[0] = (int) (calendar.getTimeInMillis() / 1000);
        return res;
    }

    /**
     * 获取某一天的上一天 12点到当天12点的int类型
     *
     * @return {上一天12点epoch_int, 当天12点epoch_int}
     */
    public static int[] getBetweenDate(int n) {
        Calendar calendar = getCalendar();
        int[] res = new int[2];
        calendar.add(Calendar.DAY_OF_YEAR, 0 - n);
        res[1] = (int) (calendar.getTimeInMillis() / 1000);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        res[0] = (int) (calendar.getTimeInMillis() / 1000);
        return res;
    }

    /**
     * @param gap      数量
     * @param isFuture true 将来, false 过去
     * @return [Date ,expire time epoch Integer type]
     */
    public static Object[] getEpoch(int gap, boolean isFuture) {
        Calendar calendar = getCalendar();
        gap *= isFuture ? 1 : -1;
        calendar.add(Calendar.DAY_OF_YEAR, gap);
        return new Object[]{calendar.getTime(), (int) (calendar.getTimeInMillis() / 1000)};
    }

    private static Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

}
