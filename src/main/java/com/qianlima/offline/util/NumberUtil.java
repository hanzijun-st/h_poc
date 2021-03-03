package com.qianlima.offline.util;


import java.util.regex.Pattern;

/**
 * 关于数字的判断
 */
public class NumberUtil {

    /**
     * 校验手机号
     * @param in
     * @return
     */
    public static boolean validateMobilePhone(String in) {
        Pattern pattern = Pattern.compile("^[1]\\d{10}$");
        return pattern.matcher(in).matches();
    }

}
