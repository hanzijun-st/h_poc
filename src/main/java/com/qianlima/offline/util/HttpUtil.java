package com.qianlima.offline.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 异常推送
 */
@Slf4j
@Component
public class HttpUtil {

    String appKey = "dc_20190923_bigcustomer";
    String deviceInfo = "qlm_bigcustomer";
    String nonceStr = VerificationUtil.create_nonce_str();

    public String push(String contentId) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
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
        parameter.put("sign", VerificationUtil.sign(sign, "56B14F090D8B4A0E89CF95AC6F9CBEDD"));
        parameter.put("data", map);

        String s = JSONObject.toJSONString(parameter);
        String result = null;
        CloseableHttpClient httpClient = getHttpClient();
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000).setSocketTimeout(60000).setConnectTimeout(60000).build();
        try {
            HttpPost post = new HttpPost("http://datafetcher.intra.qianlima.com/dc/bidding/content");
            //创建参数列表
            post.setEntity(new StringEntity(s, Charset.forName("UTF-8")));
            //url格式编码
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            //设置超时时间为60秒
            post.setConfig(requestConfig);
            //执行请求
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            try {
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    if (null != entity) {
                        result = EntityUtils.toString(entity);
                    }
                }
            } finally {
                httpResponse.close();
            }

        } catch (UnsupportedEncodingException e) {
            log.error("推送异常:{}", e);
        } catch (IOException e) {
            log.error("推送异常:{}", e);
        } finally {
            try {
                closeHttpClient(httpClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public String getICT(String title, String content) {
        String result = null;

        //创建默认的httpClient实例
        CloseableHttpClient httpClient = getHttpClient();
        try {
            HttpPost post = new HttpPost("http://118.190.158.164:8088/api/ict");
            //创建参数列表
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("title", title));
            list.add(new BasicNameValuePair("content", content));
            //url格式编码
            UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(list, "UTF-8");
            post.setEntity(uefEntity);
            //执行请求
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            try {
                HttpEntity entity = httpResponse.getEntity();
                if (null != entity) {
                    result = EntityUtils.toString(entity);
                }
            } finally {
                httpResponse.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                closeHttpClient(httpClient);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;

    }

    private CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    private void closeHttpClient(CloseableHttpClient client) throws IOException {
        if (client != null) {
            client.close();
        }
    }
}
