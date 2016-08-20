/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public abstract class AbstractDatabaseInitIT extends AbstractFindIT {
    @Autowired
    private DataSource dataSource;

    /**
     * Check connection to our configured datasource.
     * <p>
     * Depending on the datasource a dummy statement must sometimes
     * be executed to ensure a valid connection.
     */
    @Test
    public void connectToDatabase() {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource);
        jdbcTemplate.execute("SELECT true");
    }
}
