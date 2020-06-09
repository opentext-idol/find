/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package db.migration.mysql;

import db.migration.AbstractMigrateUsersToIncludeUsernames;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

@SuppressWarnings("unused")
public class V11_4_0_3__Migrate_Users_To_Include_Usernames extends AbstractMigrateUsersToIncludeUsernames {
    @Override
    protected String getUpdateUserSql() {
        return "UPDATE users SET user_id=?, domain=?, user_store=?, uuid=?, uid=?, username=? WHERE user_id=?";
    }

    @Override
    protected void getBatchParameters(final PreparedStatement ps, final DeprecatedUser user) throws SQLException {
        ps.setLong(1, user.getUserId());
        ps.setNull(2, Types.VARCHAR);
        ps.setNull(3, Types.VARCHAR);
        ps.setNull(4, Types.VARCHAR);
        ps.setNull(5, Types.BIGINT);
        ps.setString(6, user.getUsername());
        ps.setLong(7, user.getUserId());
    }
}
