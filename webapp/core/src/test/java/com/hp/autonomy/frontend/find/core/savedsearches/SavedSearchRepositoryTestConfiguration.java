package com.hp.autonomy.frontend.find.core.savedsearches;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchRepositoryTestConfiguration.SAVED_SEARCH_REPOSITORY_TEST_PROPERTY;

@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories
@ConditionalOnProperty(SAVED_SEARCH_REPOSITORY_TEST_PROPERTY)
public class SavedSearchRepositoryTestConfiguration {
    static final String SAVED_SEARCH_REPOSITORY_TEST_PROPERTY = "saved-search.repository.test";
}
