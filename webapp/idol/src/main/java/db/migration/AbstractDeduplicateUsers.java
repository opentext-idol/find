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

package db.migration;

import lombok.Data;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractDeduplicateUsers  implements SpringJdbcMigration {

    @Override
    public void migrate(final JdbcTemplate jdbcTemplate) throws Exception {
        final List<User> allUsers = getAllUserEntities(jdbcTemplate);
        final Map<String, List<User>> groupedUsers = allUsers.stream().collect(Collectors.groupingBy(User::getUsername));

        groupedUsers.forEach((username, userEntities) -> {
            reassignSavedSearchesToSingleUser(jdbcTemplate, userEntities);
            deleteDuplicateUsers(jdbcTemplate, userEntities);
        });
    }

    private List<User> getAllUserEntities(final JdbcOperations jdbcTemplate) {
        return jdbcTemplate.query("SELECT user_id, username FROM users", (rs, rowNum) -> {
            final String username = rs.getString("username");
            final long userId = rs.getLong("user_id");
            return new User(username, userId);
        });
    }

    private void reassignSavedSearchesToSingleUser(final JdbcOperations jdbcTemplate, final List<User> users) {
        final Long firstUserId = users.get(0).getUserId();

        users.forEach(user -> {
            final Long userId = user.getUserId();

            if(!Objects.equals(userId, firstUserId)) {
                final List<Long> savedSearchIds = jdbcTemplate.queryForList("SELECT search_id FROM searches WHERE user_id=?", Long.class, userId);
                updateSavedSearch(jdbcTemplate, firstUserId, savedSearchIds);
            }
        });
    }

    private void updateSavedSearch(final JdbcOperations jdbcTemplate, final Long firstUserId, final List<Long> savedSearchIds) {
        final String updateSql = "UPDATE searches SET user_id=? WHERE search_id=?";
        jdbcTemplate.batchUpdate(updateSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                ps.setLong(1, firstUserId);
                ps.setLong(2, savedSearchIds.get(i));
            }

            @Override
            public int getBatchSize() {
                return savedSearchIds.size();
            }
        });
    }

    private void deleteDuplicateUsers(final JdbcOperations jdbcTemplate, final List<User> userEntities) {
        final String deleteSql = "DELETE FROM users WHERE user_id=?";
        jdbcTemplate.batchUpdate(deleteSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                ps.setLong(1, userEntities.get(i + 1).getUserId());
            }

            @Override
            public int getBatchSize() {
                // Don't delete the first user
                return userEntities.size() - 1;
            }
        });
    }

    @Data
    private static class User {
        private final String username;
        private final Long userId;
    }
}
