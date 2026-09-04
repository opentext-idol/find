package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.*;

@RunWith(SpringRunner.class)
public class FlywayIdolConfigUpdateHandlerTest {
    @Mock private Flyway flyway;
    @Mock private Configuration flywayConfiguration;
    @Mock private DataSource dataSource;
    @Mock private Connection connection;
    @Mock private DatabaseMetaData databaseMetaData;
    @Mock private ResultSet tablesResultSet;
    @Mock private ResultSet columnsResultSet;
    @Mock private PreparedStatement preparedStatement;
    @Mock private IdolFindConfig config;

    private FlywayIdolConfigUpdateHandler flywayIdolConfigUpdateHandler;

    @Before
    public void setUp() throws SQLException {
        MockitoAnnotations.initMocks(this); // to be removed in favour of MockitoExtension with junit 5

        Mockito.doReturn(flywayConfiguration).when(flyway).getConfiguration();
        Mockito.doReturn(dataSource).when(flywayConfiguration).getDataSource();
        Mockito.doReturn(new String[]{"public"}).when(flywayConfiguration).getSchemas();
        Mockito.doReturn("schema_version").when(flywayConfiguration).getTable();
        Mockito.doReturn(connection).when(dataSource).getConnection();
        Mockito.doReturn(databaseMetaData).when(connection).getMetaData();
        Mockito.doReturn("\"").when(databaseMetaData).getIdentifierQuoteString();
        Mockito.doReturn(tablesResultSet).when(databaseMetaData).getTables(
                Mockito.any(), Mockito.isNull(), Mockito.eq("%"), Mockito.any()
        );
        Mockito.doReturn(true, false).when(tablesResultSet).next();
        Mockito.doReturn("schema_version").when(tablesResultSet).getString("TABLE_NAME");
        Mockito.doReturn("public").when(tablesResultSet).getString("TABLE_SCHEM");
        Mockito.doReturn(columnsResultSet).when(databaseMetaData).getColumns(
                Mockito.any(), Mockito.eq("public"), Mockito.eq("schema_version"), Mockito.eq("%")
        );
        Mockito.doReturn(true, false).when(columnsResultSet).next();
        Mockito.doReturn("type").when(columnsResultSet).getString("COLUMN_NAME");
        Mockito.doReturn(preparedStatement).when(connection).prepareStatement(Mockito.anyString());

        flywayIdolConfigUpdateHandler = new FlywayIdolConfigUpdateHandler(flyway);
    }

    @Test
    public void testUpdate_validConfig() throws SQLException {
        flywayIdolConfigUpdateHandler.update(config);

        Mockito.verify(connection).prepareStatement("UPDATE \"public\".\"schema_version\" SET \"type\" = ? WHERE \"type\" = ?");
        Mockito.verify(preparedStatement).setString(1, "JDBC");
        Mockito.verify(preparedStatement).setString(2, "SPRING_JDBC");
        Mockito.verify(preparedStatement).executeUpdate();
        Mockito.verify(flyway).repair();
        Mockito.verify(flyway).migrate();
    }

    @Test
    public void testUpdate_noHistoryTable() throws SQLException {
        Mockito.doReturn(false).when(tablesResultSet).next();

        flywayIdolConfigUpdateHandler.update(config);

        Mockito.verify(connection, Mockito.never()).prepareStatement(Mockito.anyString());
        Mockito.verify(flyway).repair();
        Mockito.verify(flyway).migrate();
    }

    @Test
    public void testUpdate_tableInDifferentSchema() throws SQLException {
        Mockito.doReturn("private").when(tablesResultSet).getString("TABLE_SCHEM");

        flywayIdolConfigUpdateHandler.update(config);

        Mockito.verify(connection, Mockito.never()).prepareStatement(Mockito.anyString());
        Mockito.verify(flyway).repair();
        Mockito.verify(flyway).migrate();
    }

    @Test
    public void testUpdate_noTypeColumn() throws SQLException {
        Mockito.doReturn(false).when(columnsResultSet).next();

        flywayIdolConfigUpdateHandler.update(config);

        Mockito.verify(connection, Mockito.never()).prepareStatement(Mockito.anyString());
        Mockito.verify(flyway).repair();
        Mockito.verify(flyway).migrate();
    }

}
