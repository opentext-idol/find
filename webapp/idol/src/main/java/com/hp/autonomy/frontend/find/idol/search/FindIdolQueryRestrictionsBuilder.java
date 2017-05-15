package com.hp.autonomy.frontend.find.idol.search;

import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Collection;

@Component
@Primary
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class FindIdolQueryRestrictionsBuilder implements IdolQueryRestrictionsBuilder {
    private final IdolQueryRestrictionsBuilder defaultBuilder;

    @Autowired
    public FindIdolQueryRestrictionsBuilder(final ObjectFactory<IdolQueryRestrictionsBuilder> defaultBuilderFactory) {
        defaultBuilder = defaultBuilderFactory.getObject()
                .anyLanguage(true);
    }

    @Override
    public FindIdolQueryRestrictionsBuilder queryText(final String queryText) {
        defaultBuilder.queryText(queryText);
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder fieldText(final String fieldText) {
        defaultBuilder.fieldText(fieldText);
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder databases(final Collection<? extends String> databases) {
        defaultBuilder.databases(databases);
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder database(final String database) {
        defaultBuilder.queryText(database);
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder clearDatabases() {
        defaultBuilder.clearDatabases();
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder minDate(final ZonedDateTime minDate) {
        defaultBuilder.minDate(minDate);
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder maxDate(final ZonedDateTime maxDate) {
        defaultBuilder.maxDate(maxDate);
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder minScore(final Integer minScore) {
        defaultBuilder.minScore(minScore);
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder languageType(final String languageType) {
        defaultBuilder.queryText(languageType);
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder anyLanguage(final boolean anyLanguage) {
        defaultBuilder.anyLanguage(anyLanguage);
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder stateMatchId(final String stateMatchId) {
        defaultBuilder.stateMatchId(stateMatchId);
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder stateMatchIds(final Collection<? extends String> stateMatchIds) {
        defaultBuilder.stateMatchIds(stateMatchIds);
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder clearStateMatchIds() {
        defaultBuilder.clearStateMatchIds();
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder stateDontMatchId(final String stateDontMatchId) {
        defaultBuilder.stateDontMatchId(stateDontMatchId);
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder stateDontMatchIds(final Collection<? extends String> stateDontMatchIds) {
        defaultBuilder.stateDontMatchIds(stateDontMatchIds);
        return this;
    }

    @Override
    public FindIdolQueryRestrictionsBuilder clearStateDontMatchIds() {
        defaultBuilder.clearStateDontMatchIds();
        return this;
    }

    @Override
    public IdolQueryRestrictions build() {
        return defaultBuilder
                .build();
    }
}
