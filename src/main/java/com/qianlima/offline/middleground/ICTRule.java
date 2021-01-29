package com.qianlima.offline.middleground;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Component
public class ICTRule {

    private CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    private void closeHttpClient(CloseableHttpClient client) throws IOException {
        if (client != null) {
            client.close();
        }
    }

/*    public static void main(String[] args) {
        String a=ICTRule.checkICT("170643995","智慧校园采购电脑","智慧校园需要购买电脑一批");
        System.out.println(a);
    }*/

    /**
     * 判断数据是否为ICT数据
     *
     * @param infoId
     * @param title   标题
     * @param content 内容
     * @return error：程序异常  8:是ICT  ""：不是ICT
     */
    public String checkICT(String infoId, String title, String content) {
        String returnResult = "error";
        //标题为空的数据，不符合要求，舍弃。
        if(StringUtils.isBlank(title)){
            return "";
        }
        CloseableHttpClient httpClient = getHttpClient();
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                .setSocketTimeout(60000).setConnectTimeout(60000).build();
        UrlEncodedFormEntity entity = null;
        try {
            HttpPost post = new HttpPost("http://cusdata.qianlima.com/api/ict");
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("title", title));
            list.add(new BasicNameValuePair("content", content));
            entity = new UrlEncodedFormEntity(list, "UTF-8");
            //创建参数列表
            post.setEntity(entity);
            //url格式编码
            post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            //设置超时时间为60秒
            post.setConfig(requestConfig);

            //执行请求
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                if (StringUtils.isNotBlank(result)) {
                    JSONObject jsonObject = JSON.parseObject(result);
                    Integer code = jsonObject.getInteger("code");
                    log.info("调用ICT， contentid：{} 接口返回code:{}", infoId, code);
                    if (jsonObject != null) {
                        if (jsonObject.getInteger("code") == 0) {
                            //成功
                            JSONObject data = (JSONObject) jsonObject.get("data");
                            boolean ict = (boolean) data.get("ict");
                            if (ict) {
                                String type = (String) data.get("type");
                                if(StringUtils.isBlank(type)){
                                    returnResult = "D";
                                }
                                returnResult = type;
                            } else {
                                returnResult = "F";
                            }
                        } else {
                            log.error("调ict服务过程报错 infoId: {}", infoId);
                        }
                    }
                }
            } else {
                log.warn("调用ICTAPI，http响应失败 infoId：{} httpcode:{}", infoId, httpResponse.getStatusLine().getStatusCode());
            }
            if (httpResponse != null) {
                httpResponse.close();
            }
        } catch (Exception e) {
            log.error("调用ICTAPI异常:{}", e);
        } finally {
            try {
                closeHttpClient(httpClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return returnResult;
    }
}
