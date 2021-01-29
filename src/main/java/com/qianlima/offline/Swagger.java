package com.qianlima.offline;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration //声明该类为配置类
@EnableSwagger2 //声明启动Swagger2
@EnableWebMvc //声明启动mvc
public class Swagger {
    @Bean
    public Docket customDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.qianlima.offline.controller"))//扫描的包路径
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("POC相关的API接口")//文档说明
                .version("1.0.0")//文档版本说明
                .build();
    }
}