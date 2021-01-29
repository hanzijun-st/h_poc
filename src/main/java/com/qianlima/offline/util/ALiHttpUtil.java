package com.qianlima.offline.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 异常推送
 */
@Slf4j
public class ALiHttpUtil {

    static String appKey = "dc_20190923_bigcustomer";
    static String deviceInfo = "qlm_bigcustomer";
    static String secretKey = "56B14F090D8B4A0E89CF95AC6F9CBEDD";
    static String nonceStr = VerificationUtil.create_nonce_str();

    /**
     * 调取中台接口
     *
     * @param apiUrl
     * @param contentId
     * @param nowFlag   true：实时数据，需要校验字段完整性   false:历史数据，不需要校验字段完整性
     * @return returnCode       500：调取中台接口异常，数据需要重新跑;
     * 0:数据获取成功
     * 01:接口调用成功，但是字段提取错误。所以忽略此类数据。
     * 1：数据在中台不存在，保留数据以供排查问题，但是不需要重新跑
     */
    public static Map<String, Object> findByContentId(String apiUrl, String contentId, boolean nowFlag) {
        Map<String, Object> resultMap = new HashMap<>();
        String returnCode = "500";
        CloseableHttpClient httpClient = getHttpClient();
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                .setSocketTimeout(60000).setConnectTimeout(60000).build();
        JSONObject data = null;
        try {
            JSONObject map = new JSONObject(new LinkedHashMap<>());
            map.put("contentIds", contentId);
            if (nowFlag) {
                //等于1为正在处理现在的数据，需要加字段验证
                map.put("validateFields", "ORG,MIX,ZHAOBIAO_LINKINFO,ZHAOBIAO_PROXY_LINKINFO,ZHAOBIAO_MODE,ZHONGBIAO_LINKINFO,TIMES");
            }
            Map<String, String> sign = new HashMap<>();
            sign.put("data", JSONObject.toJSONString(map));
            sign.put("appKey", appKey);
            sign.put("deviceInfo", deviceInfo);
            sign.put("nonceStr", nonceStr);

            Map<String, Object> parameter = new HashMap<>();
            parameter.put("appKey", appKey);
            parameter.put("deviceInfo", deviceInfo);
            parameter.put("nonceStr", nonceStr);
            parameter.put("sign", VerificationUtil.sign(sign, secretKey));
            parameter.put("data", map);
            String s = JSONObject.toJSONString(parameter);
            HttpPost post = new HttpPost(apiUrl);
            //创建参数列表
            post.setEntity(new StringEntity(s, Charset.forName("UTF-8")));
            //url格式编码
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            //设置超时时间为60秒
            post.setConfig(requestConfig);

            //执行请求
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                if (StringUtils.isNotBlank(result)) {
                    JSONObject jsonObject = JSON.parseObject(result);
                    Integer code = jsonObject.getInteger("code");
                    log.info("调用中台API， contentid：{} 接口返回code:{}", contentId, code);
                    if (code == 0) {
                        JSONArray dataList = jsonObject.getJSONArray("data");
                        if (dataList != null && dataList.size() > 0) {
                            data = dataList.getJSONObject(0);
                            returnCode = "0";
                        } else {
                            returnCode = "01";
                        }
                    } else if (code == 1) {
                        returnCode = "1";
                    }
                }
            } else {
                log.warn("调用中台API，http响应失败 contentid：{} httpcode:{}", contentId, httpResponse.getStatusLine().getStatusCode());
            }
            if (httpResponse != null) {
                httpResponse.close();
            }
        } catch (UnsupportedEncodingException e) {
            log.error("调用中台API异常:{}", e);

        } catch (IOException e) {
            log.error("调用中台API异常:{}", e);
        } finally {
            try {
                closeHttpClient(httpClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        resultMap.put("returnCode", returnCode);
        resultMap.put("data", data);
        return resultMap;
    }

    public static void main(String[] args) {
        List<String> fields = new ArrayList<>();
        fields.add("expandField");
        fields.add("extract_zhaoBiaoUnit");
        fields.add("extract_zhongBiaoUnit");
        fields.add("extract_agentUnit");
        fields.add("extract_amountUnit");
        fields.add("extract_budget");
        String a = "175898574,175898573,175898568,175898566,175898565,175898564,175898563,175898561,175898557,175898555,175898554,175898547,175898544,175898521,175898515,175898511,175898507,175898506,175898503,175898500";
        String[] b = a.split(",");
        for (String str : b) {
            for (String field : fields) {
                Map<String, Object> map = ALiHttpUtil.getTarget("http://172.18.30.33:28888/dc/bidding/field", str, field);
                log.info("数据验收 infoid:{} field:{} data：{}", str, field, JSON.toJSONString(map));
            }

        }

        System.out.println("111");
    }

    /**
     * 获取中台标的物接口
     *
     * @param apiUrl
     * @param contentId
     * @param fieldName
     * @return returnCode  500 异常  0: 正常提取到   2：没有提取到  1：数据不存在
     */
    public static Map<String, Object> getTarget(String apiUrl, String contentId, String fieldName) {
        Map<String, Object> resultMap = new HashMap<>();
        String returnCode = "500";
        CloseableHttpClient httpClient = getHttpClient();
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                .setSocketTimeout(60000).setConnectTimeout(60000).build();
        Object data = null;
        String message = null;
        try {
            JSONObject map = new JSONObject(new LinkedHashMap<>());
            map.put("contentId", contentId);
            map.put("fieldName", fieldName);
            map.put("validateFields", fieldName);
            Map<String, String> sign = new HashMap<>();
            sign.put("data", JSONObject.toJSONString(map));
            sign.put("appKey", appKey);
            sign.put("deviceInfo", deviceInfo);
            sign.put("nonceStr", nonceStr);

            Map<String, Object> parameter = new HashMap<>();
            parameter.put("appKey", appKey);
            parameter.put("deviceInfo", deviceInfo);
            parameter.put("nonceStr", nonceStr);
            parameter.put("sign", VerificationUtil.sign(sign, secretKey));
            parameter.put("data", map);
            String s = JSONObject.toJSONString(parameter);
            HttpPost post = new HttpPost(apiUrl);
            //创建参数列表
            post.setEntity(new StringEntity(s, Charset.forName("UTF-8")));
            //url格式编码
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            //设置超时时间为60秒
            post.setConfig(requestConfig);
            //执行请求
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                if (StringUtils.isNotBlank(result)) {
                    JSONObject jsonObject = JSON.parseObject(result);
                    Integer code = jsonObject.getInteger("code");
                    message = jsonObject.getString("message");
                    if (code == 0) {
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        Boolean has = dataObject.getBoolean("has_extract");
                        if (has) {
                            returnCode = "0";
                            data = dataObject.get(fieldName);
                        } else {
                            returnCode = "2";
                        }
                    } else if (code == 1) {
                        returnCode = "1";
                    }
                }
            } else {
                log.warn("调用中台API获取标的物数据，http响应失败 contentid：{} httpcode:{}", contentId, httpResponse.getStatusLine().getStatusCode());
            }
            if (httpResponse != null) {
                httpResponse.close();
            }
        } catch (UnsupportedEncodingException e) {
            log.error("调用中台API异常:{}", e);

        } catch (IOException e) {
            log.error("调用中台API异常:{}", e);
        } finally {
            try {
                closeHttpClient(httpClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        resultMap.put("returnCode", returnCode);
        resultMap.put("data", data);
        resultMap.put("message", message);
        return resultMap;
    }

    /**
     * 获取中台标的物接口
     *
     * @param apiUrl
     * @param contentId
     * @return returnCode  500 异常  0: 正常提取到   1：数据不存在
     */
    public static Map<String, Object> getTargets(String apiUrl, String contentId, String fieldNames, String validateFields) {
        Map<String, Object> resultMap = new HashMap<>();
        String returnCode = "500";
        CloseableHttpClient httpClient = getHttpClient();
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                .setSocketTimeout(60000).setConnectTimeout(60000).build();
        JSONObject data = null;
        String message = null;
        try {
            JSONObject map = new JSONObject(new LinkedHashMap<>());
            map.put("contentId", contentId);
            map.put("fieldNames", fieldNames);
            map.put("validateFields", validateFields);
            Map<String, String> sign = new HashMap<>();
            sign.put("data", JSONObject.toJSONString(map));
            sign.put("appKey", appKey);
            sign.put("deviceInfo", deviceInfo);
            sign.put("nonceStr", nonceStr);

            Map<String, Object> parameter = new HashMap<>();
            parameter.put("appKey", appKey);
            parameter.put("deviceInfo", deviceInfo);
            parameter.put("nonceStr", nonceStr);
            parameter.put("sign", VerificationUtil.sign(sign, secretKey));
            parameter.put("data", map);
            String s = JSONObject.toJSONString(parameter);
            HttpPost post = new HttpPost(apiUrl);
            //创建参数列表
            post.setEntity(new StringEntity(s, Charset.forName("UTF-8")));
            //url格式编码
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            //设置超时时间为60秒
            post.setConfig(requestConfig);
            //执行请求
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                if (StringUtils.isNotBlank(result)) {
                    JSONObject jsonObject = JSON.parseObject(result);
                    Integer code = jsonObject.getInteger("code");
                    message = jsonObject.getString("message");
                    if (code == 0) {
                        data = jsonObject.getJSONObject("data");
                        returnCode = "0";

                    } else if (code == 1) {
                        returnCode = "1";
                    }
                }
            } else {
                log.warn("调用中台API获取标的物数据，http响应失败 contentid：{} httpcode:{}", contentId, httpResponse.getStatusLine().getStatusCode());
            }
            if (httpResponse != null) {
                httpResponse.close();
            }
        } catch (UnsupportedEncodingException e) {
            log.error("调用中台API异常:{}", e);

        } catch (IOException e) {
            log.error("调用中台API异常:{}", e);
        } finally {
            try {
                closeHttpClient(httpClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        resultMap.put("returnCode", returnCode);
        resultMap.put("data", data);
        resultMap.put("message", message);
        return resultMap;
    }

    private static CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    private static void closeHttpClient(CloseableHttpClient client) throws IOException {
        if (client != null) {
            client.close();
        }
    }
}
