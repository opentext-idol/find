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

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.impl.AciServiceImpl;
import com.autonomy.aci.client.transport.AciHttpClient;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.transport.impl.AciHttpClientImpl;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactoryImpl;
import com.hp.autonomy.types.idol.marshalling.marshallers.jaxb2.Jaxb2MarshallerFactory;
import com.hp.autonomy.types.idol.responses.User;
import com.hp.autonomy.types.requests.idol.actions.user.UserActions;
import com.hp.autonomy.types.requests.idol.actions.user.params.UserReadParams;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.http.client.HttpClient;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


// Warning: this migration depends on access to system properties that are added in IdolFindConfigService
public abstract class AbstractMigrateUsersToIncludeUsernames implements SpringJdbcMigration {

    public static final String COMMUNITY_PROTOCOL = "find.community.protocol";
    public static final String COMMUNITY_HOST = "find.community.host";
    public static final String COMMUNITY_PORT = "find.community.port";

    private static final int HTTP_SOCKET_TIMEOUT = 9000;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 5;
    private static final int MAX_CONNECTIONS_TOTAL = 5;
    private static final String NO_USER_IN_COMMUNITY_ERROR_CODE = "UASERVERUSERREAD-2147438053";

    private final AciService aciService;
    private final AciServerDetails serverDetails;
    private final ProcessorFactory processorFactory;

    protected AbstractMigrateUsersToIncludeUsernames() {
        final String host = System.getProperty(COMMUNITY_HOST);
        final int port = Integer.parseInt(System.getProperty(COMMUNITY_PORT));
        final AciServerDetails.TransportProtocol transportProtocol = AciServerDetails.TransportProtocol.valueOf(System.getProperty(COMMUNITY_PROTOCOL));

        serverDetails = new AciServerDetails(transportProtocol, host, port);
        processorFactory = new ProcessorFactoryImpl(new Jaxb2MarshallerFactory());

        final SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(HTTP_SOCKET_TIMEOUT).build();

        final HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
                .setMaxConnTotal(MAX_CONNECTIONS_TOTAL)
                .setDefaultSocketConfig(socketConfig)
                .build();

        final AciHttpClient aciHttpClient = new AciHttpClientImpl(httpClient);
        aciService = new AciServiceImpl(aciHttpClient, serverDetails);
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public void migrate(final JdbcTemplate jdbcTemplate) throws Exception {
        final List<DeprecatedUser> users = getAllUsers(jdbcTemplate);

        final Collection<DeprecatedUser> hasUids = users.stream()
                .filter(user -> user.getUid() != null)
                .collect(Collectors.toList());

        final List<DeprecatedUser> usersToChange = hasUids.stream()
                .map(user -> new DeprecatedUser(user.getUserId(), getUsernameFromCommunity(user), null))
                .collect(Collectors.toList());

        final List<DeprecatedUser> usersToDelete = usersToChange.stream()
                .filter(user -> user.getUsername() == null)
                .collect(Collectors.toList());

        updateUsers(jdbcTemplate, usersToChange);

        deleteUsers(jdbcTemplate, usersToDelete);
    }

    protected abstract String getUpdateUserSql();

    protected abstract void getBatchParameters(final PreparedStatement ps, final DeprecatedUser user) throws SQLException;

    private List<DeprecatedUser> getAllUsers(final JdbcOperations jdbcTemplate) {
        return jdbcTemplate.query("SELECT user_id, uid FROM users", (rs, rowNum) -> {
            final Long userId = rs.getLong("user_id");
            final Long uid = rs.getLong("uid");
            return new AbstractMigrateUsersToIncludeUsernames.DeprecatedUser(userId, null, uid);
        });
    }

    private String getUsernameFromCommunity(final DeprecatedUser user) throws AciErrorException {
        try {
            final AciParameters parameters = new AciParameters(UserActions.UserRead.name());
            parameters.add(UserReadParams.UID.name(), user.getUid());

            final User communityUser = aciService.executeAction(serverDetails, parameters, processorFactory.getResponseDataProcessor(User.class));
            return communityUser.getUsername();
        } catch (final AciErrorException e) {
            if (NO_USER_IN_COMMUNITY_ERROR_CODE.equals(e.getErrorId())) {
                return null;
            } else {
                throw e;
            }
        }
    }

    private void updateUsers(final JdbcOperations jdbcTemplate, final List<DeprecatedUser> users) {
        final String sql = getUpdateUserSql();

        jdbcTemplate.batchUpdate(sql, new UsersBatchPreparedStatementSetter(
                users,
                (ps, i) -> getBatchParameters(ps, users.get(i))
        ));
    }

    private void deleteUsers(final JdbcOperations jdbcTemplate, final List<DeprecatedUser> users) {
        final String sql = "DELETE FROM users WHERE user_id=?";

        jdbcTemplate.batchUpdate(sql, new UsersBatchPreparedStatementSetter(
                users,
                (ps, i) -> ps.setLong(1, users.get(i).getUserId()))
        );
    }

    @AllArgsConstructor
    private static class UsersBatchPreparedStatementSetter implements BatchPreparedStatementSetter {

        private final List<DeprecatedUser> users;
        private final SQLConsumer consumer;

        @Override
        public void setValues(final PreparedStatement ps, final int i) throws SQLException {
            consumer.accept(ps, i);
        }

        @Override
        public int getBatchSize() {
            return users.size();
        }
    }

    @FunctionalInterface
    private interface SQLConsumer {
        void accept(PreparedStatement ps, int i) throws SQLException;
    }

    @Data
    @AllArgsConstructor
    protected static class DeprecatedUser {
        private final Long userId;
        private String username;
        private Long uid;
    }
}
