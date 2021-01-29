package com.qianlima.offline.configuration;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig implements EnvironmentAware {

    // 配置文件
    Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        System.out.println("获取配置文件");
        this.environment = environment;
    }

    /**
     * 官网jdbc数据源n
     */
    @Bean(name = "gwDataSource")
    @Qualifier("gwDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.gw")
    public DataSource gwDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 官网jdbc数据源JdbcTemplet1
     */
    @Bean(name = "gwJdbcTemplate")
    public JdbcTemplate gwJdbcTemplate(@Qualifier("gwDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 13数据源
     */
    @Bean(name = "bdDataSource")
    @Qualifier("bdDataSource")
    @ConfigurationProperties(prefix="spring.datasource.bd")
    public DataSource bdPrimaryDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * jdbcs数据源JdbcTemplet1
     */
    @Bean(name = "bdJdbcTemplate")
    public JdbcTemplate bdPrimaryJdbcTemplate(@Qualifier("bdDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }



}
