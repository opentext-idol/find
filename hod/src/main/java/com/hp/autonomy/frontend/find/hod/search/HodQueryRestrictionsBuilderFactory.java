package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilderFactory;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import org.springframework.stereotype.Component;

@Component
public class HodQueryRestrictionsBuilderFactory implements QueryRestrictionsBuilderFactory<HodQueryRestrictions, ResourceIdentifier> {
    @Override
    public QueryRestrictions.Builder<HodQueryRestrictions, ResourceIdentifier> createBuilder() {
        return new HodQueryRestrictions.Builder();
    }
}
