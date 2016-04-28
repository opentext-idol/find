package com.hp.autonomy.frontend.find.core.savedsearches;

import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.WebApplicationInitializer;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;

@ConditionalOnProperty("saved-search-test")
@SpringBootApplication(exclude = SessionAutoConfiguration.class)
@EnableJpaRepositories
public class SavedSearchTestApplicationConfiguration extends SpringBootServletInitializer implements WebApplicationInitializer {
    @Bean
    public AuditorAware<UserEntity> userEntityAuditorAware() {
        //noinspection unchecked
        return mock(AuditorAware.class);
    }

    @Bean
    public DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection(final DataSource dataSource) {
        final DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection = new DatabaseDataSourceConnectionFactoryBean(dataSource);
        dbUnitDatabaseConnection.setSchema("FIND");
        return dbUnitDatabaseConnection;
    }
}
