package db.migration.h2;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.impl.AciServiceImpl;
import com.autonomy.aci.client.transport.AciHttpClient;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.transport.impl.AciHttpClientImpl;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactoryImpl;
import com.hp.autonomy.types.idol.marshalling.marshallers.jaxb2.Jaxb2MarshallerFactory;
import com.hp.autonomy.types.idol.responses.User;
import com.hp.autonomy.types.requests.idol.actions.user.UserActions;
import com.hp.autonomy.types.requests.idol.actions.user.params.UserReadParams;
import org.apache.http.client.HttpClient;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class V11_4_0_2__Migrate_Users_To_Include_Usernames implements SpringJdbcMigration {
    private static final String[] TABLES = {
            "search_concept_cluster_phrases",
            "search_indexes",
            "search_parametric_ranges",
            "search_parametric_values",
            "search_stored_state"
    };

    private static final int HTTP_SOCKET_TIMEOUT = 90000;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 20;
    private static final int MAX_CONNECTIONS_TOTAL = 120;

    private final AciService aciService;
    private final AciServerDetails serverDetails;
    private final ProcessorFactory processorFactory;

    public V11_4_0_2__Migrate_Users_To_Include_Usernames() {
//        // ToDo get these details from the config file
        serverDetails = new AciServerDetails("cbg-data-admin-dev.hpeswlab.net", 9030);
        processorFactory = new ProcessorFactoryImpl(new Jaxb2MarshallerFactory());

        final SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(HTTP_SOCKET_TIMEOUT).build();

        final HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
                .setMaxConnTotal(MAX_CONNECTIONS_TOTAL)
                .setDefaultSocketConfig(socketConfig)
                .build();

        final AciHttpClient AciHttpClient = new AciHttpClientImpl(httpClient);
        aciService = new AciServiceImpl(AciHttpClient, serverDetails);
    }

    private String getUsername(final Long uid) {
        try {
            final AciParameters parameters = new AciParameters(UserActions.UserRead.name());
            parameters.add(UserReadParams.UID.name(), uid);
            parameters.add(UserReadParams.SecurityInfo.name(), Boolean.FALSE);
            parameters.add(UserReadParams.DeferLogin.name(), Boolean.FALSE);
            final User user = aciService.executeAction(serverDetails, parameters, processorFactory.getResponseDataProcessor(User.class));
            return user.getUsername();
        } catch (final AciErrorException e) {
            // TODO: Abort migration if this happens? Check error code and if is a real fail then fail
//            if (e.getErrorId() == "UASERVERUSERREAD-2147438053") {
//
//            }
            return null;
        }
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public void migrate(final JdbcTemplate jdbcTemplate) throws Exception {

        final List<UserEntity> userEntities = jdbcTemplate.query("SELECT * FROM users", new BeanPropertyRowMapper<>(UserEntity.class));
        final Collection<UserEntity> userEntitiesToDelete = new ArrayList<>();
        final List<UserEntity> userEntitiesToChange = userEntities.stream()
                .filter(userEntity -> userEntity.getUid() != null)
                .map(userEntity -> {
                    String username = getUsername(userEntity.getUid());
                    if (username == null) {
                        userEntitiesToDelete.add(userEntity);
                    }
                    return userEntity.toBuilder()
                            .username(username)
                            .uid(null)
                            .build();
                })
                .collect(Collectors.toList());


        updateUserEntities(jdbcTemplate, userEntitiesToChange);
        final List<Long> searchIds = getIdsForUsersSavedSearches(jdbcTemplate, userEntitiesToDelete);
        Arrays.asList(TABLES).forEach(table -> deleteReferencesToSavedSearches(jdbcTemplate, searchIds, table));
        deleteUserEntitiesSavedSearches(jdbcTemplate, userEntitiesToDelete);
        deleteUserEntities(jdbcTemplate, userEntitiesToDelete);
    }

    private void updateUserEntities(final JdbcOperations jdbcTemplate, final Collection<UserEntity> userEntities) {
        final String sql = "MERGE INTO users KEY(user_id) VALUES(?, ?, ?, ?, ?, ?)";
        if (!userEntities.isEmpty()) {
            userEntities.forEach(userEntity -> jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                    ps.setLong(1, userEntity.getUserId());
                    ps.setNull(2, Types.VARCHAR);
                    ps.setNull(3, Types.VARCHAR);
                    ps.setNull(4, Types.VARCHAR);
                    ps.setNull(5, Types.BIGINT);
                    ps.setString(6, userEntity.getUsername());
                }

                @Override
                public int getBatchSize() {
                    return userEntities.size();
                }
            }));
        }
    }

    private void deleteUserEntities(final JdbcOperations jdbcTemplate, final Collection<UserEntity> userEntities) {
        final String sql = "DELETE FROM users WHERE user_id=?";
        if (!userEntities.isEmpty()) {
            userEntities.forEach(userEntity -> jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                    ps.setLong(1, userEntity.getUserId());
                }

                @Override
                public int getBatchSize() {
                    return userEntities.size();
                }
            }));
        }
    }

    private void deleteUserEntitiesSavedSearches(final JdbcTemplate jdbcTemplate, final Collection<UserEntity> userEntities) {
        final String sql = "DELETE FROM searches WHERE user_id=?";
        if (!userEntities.isEmpty()) {
            userEntities.forEach(userEntity -> jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                    ps.setLong(1, userEntity.getUserId());
                }

                @Override
                public int getBatchSize() {
                    return userEntities.size();
                }
            }));
        }
    }

    private void deleteReferencesToSavedSearches(final JdbcTemplate jdbcTemplate, final Collection<Long> searchIds,
                                                 final String table) {
        final String sql = "DELETE FROM " + table + " WHERE search_id=?";
        if (!searchIds.isEmpty()) {
            searchIds.forEach(searchId -> jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                    ps.setLong(1, searchId);
                }

                @Override
                public int getBatchSize() {
                    return searchIds.size();
                }
            }));
        }
    }

    private List<Long> getIdsForUsersSavedSearches(final JdbcOperations jdbcTemplate, final Iterable<UserEntity> userEntities) {
        final List<Long> searchIds = new ArrayList<>();
        userEntities.forEach(userEntity -> {
            searchIds.addAll(jdbcTemplate.queryForList("SELECT search_id FROM searches WHERE user_id="
                    + userEntity.getUserId().toString() + " GROUP BY search_id", Long.class));
        });
        return searchIds;
    }
}