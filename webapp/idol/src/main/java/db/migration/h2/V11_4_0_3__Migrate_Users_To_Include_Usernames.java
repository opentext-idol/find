/*
 * Copyright 2017 Open Text.
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

package db.migration.h2;

import db.migration.AbstractMigrateUsersToIncludeUsernames;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

@SuppressWarnings("unused")
public class V11_4_0_3__Migrate_Users_To_Include_Usernames extends AbstractMigrateUsersToIncludeUsernames {
    @Override
    protected String getUpdateUserSql() {
        return "MERGE INTO users KEY(user_id) VALUES(?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void getBatchParameters(final PreparedStatement ps, final DeprecatedUser user) throws SQLException {
        ps.setLong(1, user.getUserId());
        ps.setNull(2, Types.VARCHAR);
        ps.setNull(3, Types.VARCHAR);
        ps.setNull(4, Types.VARCHAR);
        ps.setNull(5, Types.BIGINT);
        ps.setString(6, user.getUsername());
    }
}
