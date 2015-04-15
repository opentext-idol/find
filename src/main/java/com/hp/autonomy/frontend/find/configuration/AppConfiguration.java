/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.autonomy.frontend.configuration.Authentication;
import com.hp.autonomy.frontend.configuration.BCryptUsernameAndPassword;
import com.hp.autonomy.frontend.configuration.ConfigurationFilterMixin;
import com.hp.autonomy.iod.client.api.search.FindRelatedConceptsService;
import com.hp.autonomy.iod.client.api.search.QueryTextIndexService;
import com.hp.autonomy.iod.client.api.textindexing.ListIndexesService;
import com.hp.autonomy.iod.client.converter.IodConverter;
import com.hp.autonomy.iod.client.error.IodErrorHandler;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import retrofit.RestAdapter;
import retrofit.client.ApacheClient;
import retrofit.converter.JacksonConverter;

@Configuration
public class AppConfiguration {

    @Bean(name = "dispatcherObjectMapper")
    public ObjectMapper dispatcherObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.addMixInAnnotations(Authentication.class, AuthenticationMixins.class);

        return mapper;
    }

    @Bean(name = "contextObjectMapper")
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.addMixInAnnotations(Authentication.class, AuthenticationMixins.class);
        mapper.addMixInAnnotations(BCryptUsernameAndPassword.class, ConfigurationFilterMixin.class);

        return mapper;
    }

    @Bean
    public RestAdapter iodRestAdapter() {
        final HttpClientBuilder builder = HttpClientBuilder.create();

        final String proxyHost = System.getProperty("find.https.proxyHost");

        if(proxyHost != null) {
            final Integer proxyPort = Integer.valueOf(System.getProperty("find.https.proxyPort", "8080"));
            builder.setProxy(new HttpHost(proxyHost, proxyPort));
        }

        return new RestAdapter.Builder()
                .setClient(new ApacheClient(builder.build()))
                .setEndpoint(System.getProperty("find.iod.api", "https://api.idolondemand.com/1"))
                .setConverter(new IodConverter(new JacksonConverter()))
                .setErrorHandler(new IodErrorHandler())
                .build();
    }

    @Bean
    public ListIndexesService listIndexesService() {
        return iodRestAdapter().create(ListIndexesService.class);
    }

    @Bean
    public QueryTextIndexService queryTextIndexService() {
        return iodRestAdapter().create(QueryTextIndexService.class);
    }

    @Bean
    public FindRelatedConceptsService relatedConceptsService() {
        return iodRestAdapter().create(FindRelatedConceptsService.class);
    }
}
