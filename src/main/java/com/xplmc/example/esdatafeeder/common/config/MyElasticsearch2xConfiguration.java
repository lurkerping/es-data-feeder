package com.xplmc.example.esdatafeeder.common.config;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Elasticsearch2.x configuration
 *
 * @author luke
 */
@Configuration
@EnableConfigurationProperties(MyElasticsearchProperties.class)
public class MyElasticsearch2xConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MyElasticsearch2xConfiguration.class);
    private static final String COMMA = ",";
    private static final String COLON = ":";

    private final MyElasticsearchProperties properties;

    public MyElasticsearch2xConfiguration(MyElasticsearchProperties properties) {
        this.properties = properties;
    }

    @Bean
    public TransportClient transportClient() throws UnknownHostException {
        //elasticsearch cluster info
        Settings settings = Settings.settingsBuilder()
                .put("client.transport.sniff", true)
                .put("cluster.name", properties.getClusterName()).build();

        //build a TransportClient
        TransportClient client = TransportClient.builder().settings(settings).build();

        //add cluster nodes
        Assert.hasText(properties.getClusterNodes(), "[Assertion failed]elasticsearch.cluster-nodes not configured");
        for (String clusterNode : StringUtils.split(properties.getClusterNodes(), COMMA)) {
            String hostname = StringUtils.substringBefore(clusterNode, COLON);
            String port = StringUtils.substringAfter(clusterNode, COLON);
            Assert.hasText(hostname, "[Assertion failed]wrong elasticsearch.cluster-nodes format，hostname is required");
            Assert.hasText(port, "[Assertion failed]wrong elasticsearch.cluster-nodes format，port is required");
            logger.info("adding transport address: " + clusterNode);
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostname), Integer.parseInt(port)));
        }
        return client;
    }

}