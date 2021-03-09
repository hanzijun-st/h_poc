package com.qianlima.offline.util;

import com.qianlima.offline.bean.NoticeMQ;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TencentContentSolr {
    @Autowired
    //@Qualifier("ictSolr")
    private SolrClient solrClient;

    public List<NoticeMQ> companyResultsBaoXian(String tiaojian, String key, Integer taskId) {
        String cursormark = "";
        List<NoticeMQ> resultMap = new ArrayList<>();
        int num = 0;
        while (num < 3000000) {
            num += 5000;
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(tiaojian);
            solrQuery.setRows(5000);
            if (StringUtils.isEmpty(cursormark)) {
                solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, CursorMarkParams.CURSOR_MARK_START);
            } else {
                solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursormark);
            }
            solrQuery.setSort("id", SolrQuery.ORDER.desc);
            try {
                QueryResponse response = solrClient.query(solrQuery, SolrRequest.METHOD.POST);
                SolrDocumentList results = response.getResults();
                if (results != null && results.size() > 0) {
                    int count = 0;
                    for (SolrDocument doc : results) {
                        if (doc.containsKey("id") && null != doc.get("id")) {
                            NoticeMQ toMQEntity = new NoticeMQ();
                            toMQEntity.setContentid(Long.valueOf(doc.get("id").toString()));
                            toMQEntity.setTitle(doc.get("title").toString());
//                            toMQEntity.setUrl(doc.get("url") != null?doc.get("url").toString():null);
//                            toMQEntity.setAmount(doc.get("amountUnit") != null?doc.get("amountUnit").toString():null);
                            toMQEntity.setZhaoBiaoUnit(doc.get("zhaoBiaoUnit") != null ? doc.get("zhaoBiaoUnit").toString() : null);
//                            toMQEntity.setZhongBiaoUnit(doc.get("zhongBiaoUnit") != null?doc.get("zhongBiaoUnit").toString():null);
//                            toMQEntity.setZhaoIndustry(doc.get("zhaoIndustry") != null?doc.get("zhaoIndustry").toString():null);
                            toMQEntity.setUpdatetime(doc.get("updatetime") != null ? doc.get("updatetime").toString() : null);
                            toMQEntity.setKey(key);
                            toMQEntity.setTaskId(taskId);
                            resultMap.add(toMQEntity);
                        }
                    }
                } else {
                    break;
                }
                if (results.size() == results.getNumFound() || cursormark.equals(response.getNextCursorMark())) {
                    break;
                } else {
                    cursormark = response.getNextCursorMark();
                }
            } catch (SolrServerException | IOException e) {
                log.error("跑数据异常,{}", e);
            }
            log.info("关键词:" + key + " solr执行到了：" + num);
        }
        return resultMap;
    }
}
