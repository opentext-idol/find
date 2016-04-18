/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration.database;

import com.google.common.collect.ImmutableMap;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableJpaAuditing
public class DatabaseConfiguration {
    public static final String SCHEMA_NAME = "find";

    @Autowired
    private Environment environment;

    private DatabaseTypeConfig databaseTypeConfig;

    @PostConstruct
    public void initialise() {
        databaseTypeConfig = Enum.valueOf(DatabaseTypeConfig.class, environment.getProperty("hp.find.databaseType", DatabaseTypeConfig.H2PERSISTENT.name()));
    }

    // Addition of this bean disables spring boot entity manager auto-configuration.
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            final EntityManagerFactoryBuilder builder,
            final DataSource dataSource,
            @SuppressWarnings("UnusedParameters") // Ensures we run the migrations before initialising the entity manager
            final FlywayMigrationInitializer flywayMigrationInitializer
    ) {
        final Map<String, Object> properties = ImmutableMap.<String, Object>builder()
                .put(org.hibernate.cfg.Environment.DEFAULT_SCHEMA, SCHEMA_NAME)
                .put(org.hibernate.cfg.Environment.USE_NATIONALIZED_CHARACTER_DATA, true)
                .build();

        return builder
                .dataSource(dataSource)
                .packages("com.hp.autonomy.frontend.find")
                .properties(properties)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "hp.find.database.migrate", matchIfMissing = true)
    public Flyway flyway(final DataSource dataSource) {
        final Flyway flyway = new Flyway();
        flyway.setLocations("classpath:" + databaseTypeConfig.getMigrationPath());
        flyway.setDataSource(dataSource);
        return flyway;
    }

    @Bean
    @ConditionalOnProperty(name = "hp.find.database.migrate", matchIfMissing = true)
    public FlywayMigrationInitializer flywayMigrationInitializer(final Flyway flyway) {
        return new FlywayMigrationInitializer(flyway);
    }

    @Bean
    public DataSource dataSource() {
        final String environmentUrl = environment.getProperty("hp.find.database.url");
        final String url = environmentUrl == null ? databaseTypeConfig.getDefaultUrl() : environmentUrl;

        return DataSourceBuilder
                .create()
                .username(environment.getProperty("hp.find.database.username"))
                .password(environment.getProperty("hp.find.database.password"))
                .url(url)
                .build();
    }
}
