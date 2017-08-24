package com.hp.autonomy.frontend.find.idol.answer;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldValue;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequestIndexBuilder;
import com.hp.autonomy.searchcomponents.core.search.fields.DocumentFieldsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolGetContentRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolGetContentRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolGetContentRequestIndex;
import com.hp.autonomy.searchcomponents.idol.search.IdolGetContentRequestIndexBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.searchcomponents.idol.search.IdolSuggestRequest;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import lombok.Data;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnswerFilter <
    T extends GetContentRequestBuilder<IdolGetContentRequest, IdolGetContentRequestIndex, IdolGetContentRequestBuilder>,
    S extends GetContentRequestIndexBuilder<IdolGetContentRequestIndex, String, IdolGetContentRequestIndexBuilder>>{

    private final DocumentsService<IdolQueryRequest, IdolSuggestRequest, IdolGetContentRequest, IdolQueryRestrictions, IdolSearchResult, AciErrorException> documentsService;
    private final DocumentFieldsService documentFieldsService;
    private final ObjectFactory<T> getContentRequestBuilderFactory;
    private final ObjectFactory<S> getContentRequestIndexBuilderFactory;

    @Autowired
    public AnswerFilter(
            final DocumentsService<IdolQueryRequest, IdolSuggestRequest, IdolGetContentRequest, IdolQueryRestrictions, IdolSearchResult, AciErrorException> documentsService,
            final ObjectFactory<T> getContentRequestBuilderFactory,
            final ObjectFactory<S> getContentRequestIndexBuilderFactory,

            final DocumentFieldsService documentFieldsService) {
        this.documentsService = documentsService;
        this.documentFieldsService = documentFieldsService;
        this.getContentRequestBuilderFactory = getContentRequestBuilderFactory;
        this.getContentRequestIndexBuilderFactory = getContentRequestIndexBuilderFactory;
    }

    public HashMap<String, AnswerDetails> resolveUrls(final List<String> references) {
        final IdolGetContentRequestIndex getContentRequestIndex = getContentRequestIndexBuilderFactory.getObject()
                .index("*")
                .references(references)
                .build();
        final GetContentRequestBuilder<IdolGetContentRequest, IdolGetContentRequestIndex, ?> requestBuilder = getContentRequestBuilderFactory.getObject()
                .indexAndReferences(getContentRequestIndex);

        // it would be nice to limit printfields, but we can't.
        final List<String> printFields = documentFieldsService.getPrintFields(Collections.singletonList("url"));

        ((IdolGetContentRequestBuilder) requestBuilder)
                .print(PrintParam.Fields);

        final IdolGetContentRequest getContentRequest = requestBuilder.build();

        final List<IdolSearchResult> results = this.documentsService.getDocumentContent(getContentRequest);

        final HashMap<String, AnswerDetails> toReturn = new HashMap<>();

        for(IdolSearchResult result : results) {
            String resolvedUrl = "";

            final FieldInfo<?> url = result.getFieldMap().get("url");
            if (url != null) {
                final List<? extends FieldValue<?>> values = url.getValues();

                if (values != null && !values.isEmpty()) {
                    resolvedUrl = values.get(0).getValue().toString();
                }
            }

            toReturn.put(result.getReference(), new AnswerDetails(resolvedUrl, result.getTitle()));
        }

        return toReturn;
    }

    @Data
    public static class AnswerDetails {
        private final String url;
        private final String title;
    }
}
