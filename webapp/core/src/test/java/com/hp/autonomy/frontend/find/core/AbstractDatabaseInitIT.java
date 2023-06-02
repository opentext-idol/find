/*
 * Copyright 2015 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
