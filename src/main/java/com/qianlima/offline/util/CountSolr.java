package com.qianlima.offline.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

;

@Component
@Slf4j
public class CountSolr {

    @Autowired
    @Qualifier("allSolr")
    private SolrClient solrClient;

    public Long companyResultsBaoXian(String tiaojian) throws Exception {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(tiaojian);
        QueryResponse response = solrClient.query(solrQuery, SolrRequest.METHOD.POST);
        SolrDocumentList results = response.getResults();
        return results.getNumFound();
    }
}
