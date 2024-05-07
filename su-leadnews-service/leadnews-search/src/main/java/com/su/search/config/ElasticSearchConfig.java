package com.su.search.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类
 * 用于将 Elasticsearch 的主机和端口属性绑定到对应的字段
 * 并提供了一个方法用于创建 RestHighLevelClient实例 以便与Elasticsearch建立连接
 */

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticSearchConfig {
    private String host;
    private int port;

    @Bean
    public RestHighLevelClient client() {
        return new RestHighLevelClient(RestClient.builder(
                new HttpHost(
                        host,
                        port,
                        HttpHost.DEFAULT_SCHEME_NAME //"http"
                )
        ));
    }

}