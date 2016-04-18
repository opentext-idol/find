/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */


package com.hp.autonomy.frontend.find.core.beanconfiguration.database;

/**
 * Which relational database implementation to use for storage
 */
public enum DatabaseTypeConfig {

    /**
     * Store data persistently in H2
     */
    H2PERSISTENT("db/migration/h2", String.format("jdbc:h2:file:%s/data/find-db;DB_CLOSE_ON_EXIT=FALSE", System.getProperty("hp.find.home"))),

    /**
     * Store data in memory in H2
     */
    H2INMEMORY("db/migration/h2", "jdbc:h2:mem:find-db;DB_CLOSE_ON_EXIT=FALSE"),

    /**
     * Store data in a maria DB instance
     */
    MARIA("db/migration/maria", String.format("jdbc:mariadb://localhost:3306/%s", DatabaseConfiguration.SCHEMA_NAME));

    private final String migrationPath;
    private final String defaultUrl;

    DatabaseTypeConfig(final String migrationPath, final String defaultUrl) {
        this.migrationPath = migrationPath;
        this.defaultUrl = defaultUrl;
    }

    /**
     * Get the base class path for FlyWay schema migrations.
     */
    public String getMigrationPath() {
        return migrationPath;
    }

    /**
     * Get the JDBC url to use if not specified via system properties.
     */
    public String getDefaultUrl() {
        return defaultUrl;
    }

}
