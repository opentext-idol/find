package com.hp.autonomy.frontend.find.core;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

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
        databaseConfigBean.setDatatypeFactory(new H2DataTypeFactory());
        databaseConfigBean.setCaseSensitiveTableNames(false);

        final DatabaseDataSourceConnectionFactoryBean databaseDataSourceConnectionFactoryBean = new DatabaseDataSourceConnectionFactoryBean();
        databaseDataSourceConnectionFactoryBean.setDataSource(dataSource);
        databaseDataSourceConnectionFactoryBean.setDatabaseConfig(databaseConfigBean);
        databaseDataSourceConnectionFactoryBean.setSchema(schema);

        return databaseDataSourceConnectionFactoryBean.getObject();
    }
}
