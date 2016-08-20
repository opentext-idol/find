package com.hp.autonomy.frontend.find.idol.search;

import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilderFactory;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import org.springframework.stereotype.Component;

@Component
public class IdolQueryRestrictionsBuilderFactory implements QueryRestrictionsBuilderFactory<IdolQueryRestrictions, String> {
    @Override
    public QueryRestrictions.Builder<IdolQueryRestrictions, String> createBuilder() {
        return new IdolQueryRestrictions.Builder()
                .setAnyLanguage(true);
    }
}
