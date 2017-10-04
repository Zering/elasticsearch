package com.elasticsearch.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by zhanghaojie on 2017/9/17.
 */
@Configuration
public class MyConfig {

  @Bean
  public TransportClient client() throws UnknownHostException {

    // 端口为elasticsearch的TCP端口9300，不是访问端口9200
    InetSocketTransportAddress node = new InetSocketTransportAddress(
        InetAddress.getByName("localhost"),
        9300
    );

    Settings settings = Settings.builder()
        .put("cluster.name", "wali").build();

    TransportClient client = new PreBuiltTransportClient(settings);
    client.addTransportAddress(node);

    return client;
  }
}
