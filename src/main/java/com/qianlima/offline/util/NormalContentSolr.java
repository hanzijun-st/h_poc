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
public class NormalContentSolr {

    @Autowired
    //@Qualifier("normalSolr")
    private SolrClient normalClient;

    public List<NoticeMQ> companyResultsBaoXian(String tiaojian, String key, Integer taskId) {
        String cursormark = "";
        List<NoticeMQ> resultMap = new ArrayList<>();

        while (true) {
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
                QueryResponse response = normalClient.query(solrQuery, SolrRequest.METHOD.POST);
                SolrDocumentList results = response.getResults();
                if (results != null && results.size() > 0) {
                    int count=0;
                    for (SolrDocument doc : results) {
                        if (doc.containsKey("id") && null!=doc.get("id")) {
                            NoticeMQ toMQEntity = new NoticeMQ();
                            toMQEntity.setContentid(Long.valueOf(doc.get("id").toString()));
                            toMQEntity.setTitle(doc.get("title") != null?doc.get("title").toString():null);
                            toMQEntity.setZhaoBiaoUnit(doc.get("zhaoBiaoUnit") != null?doc.get("zhaoBiaoUnit").toString():null);
                            toMQEntity.setBlzhaoBiaoUnit(doc.get("blZhaoBiaoUnit") != null?doc.get("blZhaoBiaoUnit").toString():null);
                            toMQEntity.setZhongBiaoUnit(doc.get("blZhongBiaoUnit") != null?doc.get("blZhongBiaoUnit").toString():null);
                            toMQEntity.setAreaid(doc.get("areaid") != null?doc.get("areaid").toString():null);
                            toMQEntity.setUpdatetime(doc.get("updatetime") != null?doc.get("updatetime").toString():null);
                            toMQEntity.setTags(doc.get("tagids") != null?doc.get("tagids").toString():null);
                            toMQEntity.setAmount(doc.get("blAmountUnit") != null?doc.get("blAmountUnit").toString():null);
                            toMQEntity.setBudget(doc.get("blBudget") != null?doc.get("blBudget").toString():null);
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
            log.info("=====关键词:"+key+" solr执行到了："+resultMap.size());
        }
        return resultMap;
    }
}
