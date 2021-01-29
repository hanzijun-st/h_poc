package com.qianlima.offline.util;

import org.apache.commons.lang3.StringUtils;

public class ZTStringUtil {
    /**
     * 针对中台返回的null，做特殊处理
     *
     * @param cs
     * @return
     */
    public static boolean isNotBlank(CharSequence cs) {
        return StringUtils.isNotBlank(cs) && !"null".equals(cs);
    }

    /**
     * 针对中台返回的null，做特殊处理
     *
     * @param cs
     * @return
     */
    public static boolean isBlank(CharSequence cs) {
        return StringUtils.isBlank(cs) || "null".equals(cs);
    }
}
