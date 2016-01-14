/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration.database;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@Conditional(H2PersistentCondition.class)
public class H2PersistentConfiguration {

    /**
     * Configure persistent file-based H2 datasource located in the
     * specified home directory, and named 'find-db'.
     */
    @Bean
    public DataSource datasource() {
        final String url = String.format("jdbc:h2:file:%s/data/find-db;", System.getProperty("hp.find.home"));
        final DataSourceBuilder builder = DataSourceBuilder.create();
        return builder
                .url(url)
                .build();
    }
}
