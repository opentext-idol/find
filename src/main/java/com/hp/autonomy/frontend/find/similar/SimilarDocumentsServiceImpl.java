package com.hp.autonomy.frontend.find.similar;

import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.Document;
import com.hp.autonomy.hod.client.api.textindex.query.search.Documents;
import com.hp.autonomy.hod.client.api.textindex.query.search.FindSimilarService;
import com.hp.autonomy.hod.client.api.textindex.query.search.Print;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryRequestBuilder;
import com.hp.autonomy.hod.client.api.textindex.query.search.Summary;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class SimilarDocumentsServiceImpl implements SimilarDocumentsService {
    private static final int MAX_RESULTS = 3;

    @Autowired
    private FindSimilarService<Documents> findSimilarService;

    @Override
    public List<Document> findSimilar(final Set<ResourceIdentifier> indexes, final String reference) throws HodErrorException {
        final QueryRequestBuilder requestBuilder = new QueryRequestBuilder()
                .setIndexes(indexes)
                .setPrint(Print.none)
                .setAbsoluteMaxResults(MAX_RESULTS)
                .setSummary(Summary.concept);

        final Documents result = findSimilarService.findSimilarDocumentsToIndexReference(reference, requestBuilder);
        return result.getDocuments();
    }
}
