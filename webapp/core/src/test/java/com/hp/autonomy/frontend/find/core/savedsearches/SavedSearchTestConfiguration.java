package com.hp.autonomy.frontend.find.core.savedsearches;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class SavedSearchTestConfiguration {
    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Bean
    public DatabaseDataSourceConnection testConnection(final DataSource dataSource) throws Exception {
        final DatabaseConfigBean databaseConfigBean = new DatabaseConfigBean();
        databaseConfigBean.setDatatypeFactory(new H2DataTypeFactory());
        databaseConfigBean.setCaseSensitiveTableNames(false);

        final DatabaseDataSourceConnectionFactoryBean databaseDataSourceConnectionFactoryBean = new DatabaseDataSourceConnectionFactoryBean();
        databaseDataSourceConnectionFactoryBean.setDataSource(dataSource);
        databaseDataSourceConnectionFactoryBean.setDatabaseConfig(databaseConfigBean);
        databaseDataSourceConnectionFactoryBean.setSchema("FIND");

        return databaseDataSourceConnectionFactoryBean.getObject();
    }
}
