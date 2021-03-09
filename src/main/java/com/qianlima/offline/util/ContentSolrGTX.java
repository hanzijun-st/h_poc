package com.qianlima.offline.util;

import com.qianlima.offline.bean.NoticeMQGTX;
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
public class ContentSolrGTX {

    @Autowired
    //@Qualifier("allSolr")
    private SolrClient solrClient;

    public List<NoticeMQGTX> companyResultsBaoXian(String tiaojian, String key, Integer taskId) {
        String cursormark = "";
        List<NoticeMQGTX> resultMap = new ArrayList<>();
        int num=0;
        while (num<1000000) {
            num += PAGE_SIZE;
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(tiaojian);
            //设置条数
            solrQuery.setRows(PAGE_SIZE);
            solrQuery.setSort("id", SolrQuery.ORDER.desc);
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
                    int count=0;
                    for (SolrDocument doc : results) {
                        if (doc.containsKey("id") && null!=doc.get("id")) {
                            NoticeMQGTX toMQEntity = new NoticeMQGTX();
                            toMQEntity.setContentid(Long.valueOf(doc.get("id").toString()));
                            toMQEntity.setTitle(doc.get("title") != null?doc.get("title").toString():null);
                            toMQEntity.setBlZhaoBiaoUnit(doc.get("blZhaoBiaoUnit") != null?doc.get("blZhaoBiaoUnit").toString():null);
                            toMQEntity.setBlZhongBiaoUnit(doc.get("blZhongBiaoUnit") != null?doc.get("blZhongBiaoUnit").toString():null);
                            toMQEntity.setZhaoBiaoUnit(doc.get("zhaoBiaoUnit") != null?doc.get("zhaoBiaoUnit").toString():null);
                            toMQEntity.setZhongBiaoUnit(doc.get("zhongBiaoUnit") != null?doc.get("zhongBiaoUnit").toString():null);
                            toMQEntity.setTags(doc.get("tags") != null?doc.get("tags").toString():null);
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
            log.info("关键词:"+key+" solr执行到了："+num);
        }
        return resultMap;
    }

    //搜索接口没有数量
    public static final Integer PAGE_SIZE = 5000;

    /**
     * 查询solr
     *
     * @param query 拼装的查询语句
     * @param pageNum 页码
     * @return
     */
    public NoticeMQGTX getSolr(String query, Integer pageNum) {
        try {
            if (pageNum == null) {
                throw new RuntimeException("页码格式不对，当前页码:" + pageNum);
            }
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(query);
            //设置页数
            solrQuery.setStart((pageNum - 1) * PAGE_SIZE);
            //设置条数
            solrQuery.setRows(PAGE_SIZE);
            solrQuery.setSort("id", SolrQuery.ORDER.desc);

            QueryResponse response = solrClient.query(solrQuery, SolrRequest.METHOD.POST);
            SolrDocumentList results = response.getResults();
            //solr的特殊性
            if (results != null && results.size() > 0) {
                for (SolrDocument doc : results) {
                    if (doc.containsKey("id") && null!=doc.get("id")) {
                        NoticeMQGTX toMQEntity = new NoticeMQGTX();
                        toMQEntity.setContentid(Long.valueOf(doc.get("id").toString()));
                        toMQEntity.setTitle(doc.get("title").toString());
                        toMQEntity.setBlZhaoBiaoUnit(doc.get("blZhaoBiaoUnit") != null?doc.get("blZhaoBiaoUnit").toString():null);
                        toMQEntity.setBlZhongBiaoUnit(doc.get("blZhongBiaoUnit") != null?doc.get("blZhongBiaoUnit").toString():null);
                        toMQEntity.setZhaoBiaoUnit(doc.get("zhaoBiaoUnit") != null?doc.get("zhaoBiaoUnit").toString():null);
                        toMQEntity.setZhongBiaoUnit(doc.get("zhongBiaoUnit") != null?doc.get("zhongBiaoUnit").toString():null);
                        toMQEntity.setTags(doc.get("tags") != null?doc.get("tags").toString():null);
                        return toMQEntity;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("solr查询异常");
        }
        return null;
    }
}
