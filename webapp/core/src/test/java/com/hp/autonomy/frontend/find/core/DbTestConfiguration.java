package com.hp.autonomy.frontend.find.core;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Types;

@Configuration
public class DbTestConfiguration {
    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Bean
    public DatabaseDataSourceConnection integrationTestConn(final DataSource dataSource) throws Exception {
        return connWithSchema(dataSource, "FIND");
    }

    @Bean
    public DatabaseDataSourceConnection unitTestConn(final DataSource dataSource) throws Exception {
        return connWithSchema(dataSource, "PUBLIC");
    }

    private DatabaseDataSourceConnection connWithSchema(final DataSource dataSource, final String schema)
            throws Exception
    {
        final DatabaseConfigBean databaseConfigBean = new DatabaseConfigBean();
        databaseConfigBean.setDatatypeFactory(new H2TimestampTzDataTypeFactory());
        databaseConfigBean.setCaseSensitiveTableNames(false);

        final DatabaseDataSourceConnectionFactoryBean databaseDataSourceConnectionFactoryBean = new DatabaseDataSourceConnectionFactoryBean();
        databaseDataSourceConnectionFactoryBean.setDataSource(dataSource);
        databaseDataSourceConnectionFactoryBean.setDatabaseConfig(databaseConfigBean);
        databaseDataSourceConnectionFactoryBean.setSchema(schema);

        return databaseDataSourceConnectionFactoryBean.getObject();
    }

    // dbunit doesn't have a version supporting newer JDBC, so we need to extend the DataTypeFactory
    private static class H2TimestampTzDataTypeFactory extends H2DataTypeFactory {
        @Override
        public DataType createDataType(final int sqlType, final String sqlTypeName) throws DataTypeException {
            if (sqlType == Types.TIMESTAMP_WITH_TIMEZONE) {
                return DataType.TIMESTAMP;
            }
            return super.createDataType(sqlType, sqlTypeName);
        }

    }

}
