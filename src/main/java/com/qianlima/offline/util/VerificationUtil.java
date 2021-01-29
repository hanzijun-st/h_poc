package com.qianlima.offline.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VerificationUtil {
    /**
     * 获取随机字符串
     *
     * @return
     */
    public static String create_nonce_str() {
        String s = UUID.randomUUID().toString();
        return s.replaceAll("\\-", "").toUpperCase();
    }

    /**
     * 获取时间戳
     *
     * @return
     */
    public static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }

    /**
     * 构造签名
     *
     * @param params
     * @param encode
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String createSign(Map<String, String> params, boolean encode) throws UnsupportedEncodingException {
        Set<String> keysSet = params.keySet();
        Object[] keys = keysSet.toArray();
        Arrays.sort(keys);
        StringBuffer temp = new StringBuffer();
        boolean first = true;
        for (Object key : keys) {
            if (first) {
                first = false;
            } else {
                temp.append("&");
            }
            temp.append(key).append("=");
            Object value = params.get(key);
            String valueString = "";
            if (null != value) {
                valueString = value.toString();
            }
            if (encode) {
                temp.append(URLEncoder.encode(valueString, "UTF-8"));
            } else {
                temp.append(valueString);
            }
        }
        return temp.toString();
    }

    /**
     * 构造签名
     *
     * @param params
     * @param secretKey
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String sign(Map<String, String> params, String secretKey) throws UnsupportedEncodingException {
        String string1 = createSign(params, false);
        String stringSignTemp = string1 + "&key=" + secretKey;
        String signValue = DigestUtils.md5Hex(stringSignTemp).toUpperCase();
        return signValue;
    }

    /**
     * 构造签名
     *
     * @param params
     * @param secretKey
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String packageSign(Map<String, String> params, String secretKey) throws UnsupportedEncodingException {
        String string1 = createSign(params, false);
        String stringSignTemp = string1 + "&key=" + secretKey;
        return stringSignTemp;
    }
}
