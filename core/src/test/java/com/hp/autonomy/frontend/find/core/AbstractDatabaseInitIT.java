/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractDatabaseInitIT extends AbstractFindIT {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired private DataSource dataSource;

    /**
     * Check connection to our configured datasource.
     *
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
