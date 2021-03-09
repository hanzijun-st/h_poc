package com.qianlima.offline.configuration;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class SolrClientConfig {

    @Autowired
    private Environment environment;

    /*@Bean(name = "newSolr")
    public SolrClient newContentSolr() {
        return new HttpSolrClient(environment.getRequiredProperty("qlm.data.newSolr"));
    }

    @Bean(name = "allSolr")
    public SolrClient contentSolr() {
        return new HttpSolrClient(environment.getRequiredProperty("qlm.data.allsolr"));
    }

    @Bean(name = "updateSolr")
    public SolrClient updateContentSolr() {
        return new HttpSolrClient(environment.getRequiredProperty("qlm.data.updateSolr"));
    }

    @Bean(name = "ictSolr")
    public SolrClient ictContentSolr() {
        return new HttpSolrClient(environment.getRequiredProperty("qlm.data.ictsolr"));
    }

    @Bean(name = "QYHYsolr")
    public SolrClient QYHYContentSolr() {
        return new HttpSolrClient(environment.getRequiredProperty("qlm.data.QYHYsolr"));
    }

    @Bean(name = "normalSolr")
    public SolrClient normalContentSolr() {
        return new HttpSolrClient(environment.getRequiredProperty("qlm.data.normalsolr"));
    }*/

    /*@Bean(name = "onlineSolr")
    public SolrClient normalContentSolr() {
        return new HttpSolrClient(environment.getRequiredProperty("qlm.data.onlineSolr"));
    }*/
    @Bean
    public SolrClient solrClient(){
        CloudSolrClient solrClient = new CloudSolrClient(environment.getRequiredProperty("qlm.data.solr.host"));
        solrClient.setDefaultCollection(environment.getRequiredProperty("qlm.data.solr.defaultCollection"));
        return solrClient;
    }

}
