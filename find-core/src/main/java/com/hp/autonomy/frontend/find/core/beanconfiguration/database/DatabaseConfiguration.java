/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;


@Configuration
@EnableJpaAuditing
public class DatabaseConfiguration {

    @Autowired DataSource datasource;

    /**
     *  Addition of this bean disables spring boot entity manager auto-configuration.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(datasource)
                .packages("com.hp.autonomy.frontend.find")
                .build();
    }
}
