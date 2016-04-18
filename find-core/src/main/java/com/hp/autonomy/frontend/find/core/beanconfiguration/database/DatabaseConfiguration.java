/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration.database;

import com.google.common.collect.ImmutableMap;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
    public static final String MIGRATE_PROPERTY_NAME = "hp.find.database.migrate";
    public static final String FLYWAY_MIGRATION_INITIALIZER_BEAN = "flywayMigrationInitializer";

    @Autowired
    private Environment environment;

    private DatabaseTypeConfig databaseTypeConfig;

    @PostConstruct
    public void initialise() {
        databaseTypeConfig = Enum.valueOf(DatabaseTypeConfig.class, environment.getProperty("hp.find.databaseType", DatabaseTypeConfig.H2PERSISTENT.name()));
    }

    @Bean
    // Ensure we run the migration before starting hibernate
    @DependsOn(FLYWAY_MIGRATION_INITIALIZER_BEAN)
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final EntityManagerFactoryBuilder builder, final DataSource dataSource) {
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

    @Bean(name = FLYWAY_MIGRATION_INITIALIZER_BEAN)
    public InitializingBean flywayMigrationInitializer(final DataSource dataSource) {
        final Boolean shouldMigrate = Boolean.valueOf(environment.getProperty(MIGRATE_PROPERTY_NAME, "true"));

        if (shouldMigrate) {
            final Flyway flyway = new Flyway();
            flyway.setLocations("classpath:" + databaseTypeConfig.getMigrationPath());
            flyway.setDataSource(dataSource);
            return new FlywayMigrationInitializer(flyway);
        } else {
            return new NoOpInitializingBean();
        }
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

    private static class NoOpInitializingBean implements InitializingBean {
        @Override
        public void afterPropertiesSet() throws Exception {}
    }
}
