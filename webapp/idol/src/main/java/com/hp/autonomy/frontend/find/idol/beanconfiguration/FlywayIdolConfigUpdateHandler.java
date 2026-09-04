package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
public class FlywayIdolConfigUpdateHandler implements IdolConfigUpdateHandler {
    private static final String LEGACY_MIGRATION_TYPE = "SPRING_JDBC";
    private static final String REPLACEMENT_MIGRATION_TYPE = "JDBC";
    private static final String TYPE_COLUMN = "type";

    private final Flyway flyway;

    @Autowired
    public FlywayIdolConfigUpdateHandler(final Flyway flyway) {
        this.flyway = flyway;
    }

    @Override
    public void update(final IdolFindConfig config) {
        repairLegacyMigrationTypes();
        flyway.repair();
        flyway.migrate();
    }

    // Previous versions of Find recorded Java migrations in the schema history table using Flyway's old
    // SpringJdbcMigration support, whose rows have a type of SPRING_JDBC. Recent versions of Flyway dropped support for
    // this type, so replace occurrences with the equivalent JDBC type before Flyway reads the history table.
    private void repairLegacyMigrationTypes() {
        final Configuration configuration = flyway.getConfiguration();

        try (final Connection connection = configuration.getDataSource().getConnection()) {
            final ResolvedTable table = findSchemaHistoryTable(connection, configuration);
            if (table == null) {
                // no schema history table
                return;
            }
            final String typeColumn = findColumn(connection, table, TYPE_COLUMN);
            if (typeColumn == null) {
                return;
            }

            try (final PreparedStatement statement = connection.prepareStatement(
                    "UPDATE " + table.qualifiedName() + " SET " + typeColumn + " = ? WHERE " + typeColumn + " = ?"
            )) {
                statement.setString(1, REPLACEMENT_MIGRATION_TYPE);
                statement.setString(2, LEGACY_MIGRATION_TYPE);
                statement.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new FlywayException("Failed to repair legacy Flyway migration types in the schema history table", e);
        }
    }

    // Resolves the schema history table's exact, database reported name and schema (which may not match the
    // configured casing, e.g. for H2) via JDBC metadata, so it can be safely referenced regardless of the
    // database's identifier case folding behaviour.
    private ResolvedTable findSchemaHistoryTable(final Connection connection, final Configuration configuration) throws SQLException {
        final DatabaseMetaData metaData = connection.getMetaData();
        final String quote = metaData.getIdentifierQuoteString();
        final String tableName = configuration.getTable();
        final String[] schemas = configuration.getSchemas();

        try (final ResultSet tables = metaData.getTables(connection.getCatalog(), null, "%", new String[]{ "TABLE" })) {
            while (tables.next()) {
                final String table = tables.getString("TABLE_NAME");
                if (!tableName.equalsIgnoreCase(table)) {
                    continue;
                }

                final String schema = tables.getString("TABLE_SCHEM");
                if (schemas.length > 0 && schema != null && !containsIgnoreCase(schemas, schema)) {
                    continue;
                }

                return new ResolvedTable(quote, schema, table);
            }
        }

        return null;
    }

    // Resolves a column's exact, database reported name for the same reason as findSchemaHistoryTable above.
    private String findColumn(final Connection connection, final ResolvedTable table, final String columnName) throws SQLException {
        final DatabaseMetaData metaData = connection.getMetaData();

        try (final ResultSet columns = metaData.getColumns(connection.getCatalog(), table.schema, table.table, "%")) {
            while (columns.next()) {
                final String column = columns.getString("COLUMN_NAME");
                if (columnName.equalsIgnoreCase(column)) {
                    return table.quote(column);
                }
            }
        }

        return null;
    }

    private static boolean containsIgnoreCase(final String[] values, final String value) {
        for (final String candidate : values) {
            if (candidate.equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }

    private record ResolvedTable(String quote, String schema, String table) {

        private String quote(final String identifier) {
                return quote + identifier + quote;
            }

            private String qualifiedName() {
                return schema == null || schema.isEmpty() ? quote(table) : quote(schema) + '.' + quote(table);
            }

        }

}
