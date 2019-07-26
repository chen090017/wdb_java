package com.wdb.pdd.common.config;

import com.pdd.pop.sdk.http.PopAccessTokenClient;
import com.pdd.pop.sdk.http.PopHttpClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/4 0004
 * @描述
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "pdd")
public class PddSdkConfig {

    private String clientId;

    private String clientSecret;

    @Bean
    public PopHttpClient httpClient(){
        return new PopHttpClient(clientId, clientSecret);
    }

    @Bean
    public PopAccessTokenClient accessTokenClient() {
        PopAccessTokenClient accessTokenClient = new PopAccessTokenClient(clientId,clientSecret);
        return accessTokenClient;
    }
}
