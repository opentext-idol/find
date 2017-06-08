package db.migration.h2;

import com.hp.autonomy.frontend.find.core.savedsearches.OldUserEntity;
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
    protected void getBatchParameters(final PreparedStatement ps, final OldUserEntity userEntity) throws SQLException {
        ps.setLong(1, userEntity.getUserId());
        ps.setNull(2, Types.VARCHAR);
        ps.setNull(3, Types.VARCHAR);
        ps.setNull(4, Types.VARCHAR);
        ps.setNull(5, Types.BIGINT);
        ps.setString(6, userEntity.getUsername());
    }
}