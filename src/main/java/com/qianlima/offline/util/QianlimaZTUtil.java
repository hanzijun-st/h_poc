package com.qianlima.offline.util;

import com.alibaba.fastjson.JSON;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 千里马调取中台的工具类
 */
@Slf4j
public class QianlimaZTUtil {

    static String appKey = "dc_20190923_bigcustomer";
    static String deviceInfo = "qlm_bigcustomer";
    static String secretKey = "56B14F090D8B4A0E89CF95AC6F9CBEDD";
    static String nonceStr = VerificationUtil.create_nonce_str();
    static String apiUrl = "http://datafetcher.intra.qianlima.com/dc/bidding/fields";


    /**
     * 通知中台进行标的物提取
     *
     * @param apiUrl
     * @param contentId
     * @return returnCode 0:推送成功  -1:参数为空  1:data为空  -2:认证失败  -3:向MQ推送失败 -4:未知错误
     */
    public static Map<String, Object> saveContentId(String apiUrl, String contentId) {
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
                    returnCode = jsonObject.getInteger("code").toString();
                    message = jsonObject.getString("message");
                    log.info("调用中台API， contentid：{} 接口返回code:{}", contentId, returnCode);
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
        resultMap.put("message", message);
        return resultMap;
    }


    /**
     *  获取中台单独字段的接口
     * @param apiUrl
     * @param contentId
     * @param fieldName
     * @return returnCode 0:推送成功  -1:参数为空  1:data为空  -2:认证失败  -3:向MQ推送失败 -4:未知错误
     */
    public static Map<String, Object> getSingleField(String apiUrl, String contentId,String fieldName) {
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
                    //log.info("调用中台API获取标的物数据 contentid：{} 接口返回code:{}", contentId, code);
                    if (code == 0) {
                        JSONObject jSONObject = jsonObject.getJSONObject("data");
                        if (jSONObject != null && jSONObject.size() > 0) {
                            data = jSONObject;
                            returnCode = "0";
                        } else {
                            returnCode = "1";
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
     * 获取数据源头url
     *
     * @param url
     * @param contentId
     * @return
     */
    public static String getFromUrl(String url, String contentId) {
        CloseableHttpClient httpClient = getHttpClient();
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                .setSocketTimeout(60000).setConnectTimeout(60000).build();
        try {
            JSONObject map = new JSONObject(new LinkedHashMap<>());
            map.put("contentId", contentId);
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
            HttpPost post = new HttpPost(url);
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
                    if (code == 0) {
                        JSONObject jSONObject = jsonObject.getJSONObject("data");
                        return jSONObject.getString("fromUrl");
                    } else {
                        log.error("获取数据源头url contentid：{} 接口返回result:{}", contentId, result);
                    }
                }
            } else {
                log.error("获取数据源头url，http响应失败 contentid：{} httpcode:{}", contentId, httpResponse.getStatusLine().getStatusCode());
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

        return null;
    }

    /**
     * 获取中台多个字段的接口
     * @param contentId
     * @param fieldName                 获取的字段名
     * @param validateFields            字段校验位
     * @return returnCode 0:推送成功  -1:参数为空  1:data为空  -2:认证失败  -3:向MQ推送失败 -4:未知错误
     */
    public static Map<String, Object> getFields(String contentId, String fieldName, String validateFields) {
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
            map.put("fieldNames", fieldName);
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
                    //log.info("调用中台API获取多个字段数据 contentid：{} 接口返回code:{}", contentId, code);
                    if (code == 0) {
                        JSONObject jSONObject = jsonObject.getJSONObject("data");
                        if (jSONObject != null && jSONObject.size() > 0) {
                            data = jSONObject;
                            returnCode = "0";
                        } else {
                            returnCode = "1";
                        }
                    } else if (code == 1) {
                        returnCode = "1";
                    }
                }
            } else {
                log.warn("调用中台API获取多个字段数据，http响应失败 contentid：{} httpcode:{}", contentId, httpResponse.getStatusLine().getStatusCode());
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

    public static Map<String, Object> getFields(String apiUrl, String contentId, String fieldName, String validateFields) {
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
            map.put("fieldNames", fieldName);
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
                    log.info("调用中台API获取多个字段数据 contentid：{} 接口返回code:{}", contentId, code);
                    if (code == 0) {
                        JSONObject jSONObject = jsonObject.getJSONObject("data");
                        if (jSONObject != null && jSONObject.size() > 0) {
                            data = jSONObject;
                            returnCode = "0";
                        } else {
                            returnCode = "1";
                        }
                    } else if (code == 1) {
                        returnCode = "1";
                    }
                }
            } else {
                log.warn("调用中台API获取多个字段数据，http响应失败 contentid：{} httpcode:{}", contentId, httpResponse.getStatusLine().getStatusCode());
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
     *  获取中台单独字段的接口
     * @param contentId
     * @param fieldName
     * @return returnCode 0:推送成功  -1:参数为空  1:data为空  -2:认证失败  -3:向MQ推送失败 -4:未知错误
     */
    public static Map<String, Object> getSingleField(String contentId,String fieldName) {
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
                    log.info("调用中台API获取标的物数据 contentid：{} 接口返回code:{}", contentId, code);
                    if (code == 0) {
                        JSONObject jSONObject = jsonObject.getJSONObject("data");
                        if (jSONObject != null && jSONObject.size() > 0) {
                            data = jSONObject;
                            returnCode = "0";
                        } else {
                            returnCode = "1";
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

    private static CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    private static void closeHttpClient(CloseableHttpClient client) throws IOException {
        if (client != null) {
            client.close();
        }
    }
}
