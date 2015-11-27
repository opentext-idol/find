package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.hp.autonomy.frontend.find.core.parametricfields.ParametricRequestBuilder;
import com.hp.autonomy.idol.parametricvalues.IdolParametricRequest;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class IdolParametricRequestBuilder implements ParametricRequestBuilder<IdolParametricRequest, String> {
    @Override
    public IdolParametricRequest buildRequest(final Set<String> databases, final Set<String> fieldNames, final String queryText, final String fieldText) {
        return new IdolParametricRequest.Builder()
                .setDatabases(databases)
                .setFieldNames(fieldNames)
                .setQueryText(queryText)
                .setFieldText(fieldText)
                .build();
    }
}
