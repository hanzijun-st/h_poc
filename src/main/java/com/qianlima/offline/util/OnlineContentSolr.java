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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 线上solr
 */
@Component
@Slf4j
public class OnlineContentSolr {

    @Autowired
    //@Qualifier("onlineSolr")
    private SolrClient solrClient;

    public List<NoticeMQ> companyResultsBaoXian(String tiaojian, String key, Integer taskId) {
        String cursormark = "";
        List<NoticeMQ> resultMap = new ArrayList<>();

        while (true) {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(tiaojian);
            solrQuery.setRows(5000);
            //solrQuery.setFields("fl","id","zhaoBiaoUnit","title");
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
                            toMQEntity.setTitle(doc.get("title") != null ? doc.get("title").toString() : null);//标题
                            toMQEntity.setZhaoBiaoUnit(doc.get("zhaoBiaoUnit") != null ? doc.get("zhaoBiaoUnit").toString() : null);//招标单位
                            toMQEntity.setBlzhaoBiaoUnit(doc.get("blZhaoBiaoUnit") != null ? doc.get("blZhaoBiaoUnit").toString() : null);//百炼招标单位
                            toMQEntity.setZhongBiaoUnit(doc.get("blZhongBiaoUnit") != null ? doc.get("blZhongBiaoUnit").toString() : null);//中标单位
                            toMQEntity.setUpdatetime(doc.get("updatetime") != null ? doc.get("updatetime").toString() : null);//发布时间
                            //toMQEntity.setTags(doc.get("tags") != null ? doc.get("tags").toString() : null);//
                            //toMQEntity.setTags(doc.get("tagids") != null ? doc.get("tagids").toString() : null);//
                            toMQEntity.setKey(key);
                            toMQEntity.setTaskId(taskId);//
                            toMQEntity.setAmount(doc.get("amountUnit") != null ? doc.get("amountUnit").toString() : null);//中标金额
                            toMQEntity.setAmountUnit(doc.get("amountUnit") != null ? doc.get("amountUnit").toString() : null);//中标金额

                            toMQEntity.setNewAmountUnit(doc.get("newAmountUnit") != null ? doc.get("newAmountUnit").toString() : null);//混合中标金额
                            toMQEntity.setBudget(doc.get("budget") != null ? doc.get("budget").toString() : null);//招标预算金额
                            toMQEntity.setNewZhongBiaoUnit(doc.get("newZhongBiaoUnit") != null ? doc.get("newZhongBiaoUnit").toString() : null);//混合中标单位
                            toMQEntity.setXmNumber(doc.get("xmNumber") != null ? doc.get("xmNumber").toString() : null);//项目编号
                            toMQEntity.setNewProvince(doc.get("newProvince") != null ? doc.get("newProvince").toString() : null);//省
                            toMQEntity.setNewCity(doc.get("newCity") != null ? doc.get("newCity").toString() : null);//市
                            toMQEntity.setNewCountry(doc.get("newCountry") != null ? doc.get("newCountry").toString() : null);//县
                            toMQEntity.setBiddingType(doc.get("biddingType") != null ? doc.get("biddingType").toString() : null);//招标方式
                            toMQEntity.setUrl(doc.get("url") != null ? doc.get("url").toString() : null);//url

                            toMQEntity.setProgid(doc.get("progid") != null ? doc.get("progid").toString() : null);//信息类型
                            toMQEntity.setZhaoRelationName(doc.get("zhaoRelationName") != null ? doc.get("zhaoRelationName").toString() : null);//自提招标单位联系人
                            toMQEntity.setZhaoRelationWay(doc.get("zhaoRelationWay") != null ? doc.get("zhaoRelationWay").toString() : null);//自提招标单位联系方式
                            toMQEntity.setAgentUnit(doc.get("agentUnit") != null ? doc.get("agentUnit").toString() : null);//代理机构
                            toMQEntity.setAgentRelationName(doc.get("agentRelationName") != null ? doc.get("agentRelationName").toString() : null);//代理机构联系人
                            toMQEntity.setAgentRelationWay(doc.get("agentRelationWay") != null ? doc.get("agentRelationWay").toString() : null);//代理机构联系方式
                            toMQEntity.setZhongRelationName(doc.get("zhongRelationName") != null ? doc.get("zhongRelationName").toString() : null);//自提中标单位联系人
                            toMQEntity.setZhongRelationWay(doc.get("zhongRelationWay") != null ? doc.get("zhongRelationWay").toString() : null);//自提中标单位联系方式

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
            log.info("=====关键词:" + key + " solr执行到了：" + resultMap.size());
        }
        return resultMap;
    }

    /**
     * 查询solr中的数据  通过输入的年份
     * @param tiaojian  查询条件
     * @param time 输入的时间
     * @return
     */
    public Map<String,Object> getSolr(String tiaojian,String time){
        Map<String,Object> map = new HashMap<>();

        tiaojian = "yyyymm:"+time+" AND "+tiaojian;
        String cursormark = "";

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(tiaojian);
        solrQuery.setRows(5000);
        solrQuery.setFields("fl","id","zhaoBiaoUnit","title");
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
                map.put(time,results.getNumFound());
            } else {
                map.put(time,0);
            }
        } catch (SolrServerException | IOException e) {
            log.error("跑数据异常,{}", e);
        }
        return map;
    }
}
