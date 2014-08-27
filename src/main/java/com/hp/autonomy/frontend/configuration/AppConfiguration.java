package com.hp.autonomy.frontend.configuration;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/*
 * $Id:$
 *
 * Copyright (c) 2014, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */
@Configuration
public class AppConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        httpClientBuilder
            .setMaxConnPerRoute(20)
            .setMaxConnTotal(120);

        final String proxyHost = System.getProperty("find.https.proxyHost");

        if(proxyHost != null) {
            final Integer proxyPort = Integer.valueOf(System.getProperty("find.https.proxyPort", "80"));
            httpClientBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
        }

        final ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());

        return new RestTemplate(requestFactory);
    }

}
