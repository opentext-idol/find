/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.hp.autonomy.frontend.find.core.savedsearches.AbstractSavedSearchServiceTest;
import com.hp.autonomy.frontend.find.core.savedsearches.ConceptClusterPhrase;
import com.hp.autonomy.frontend.find.core.savedsearches.FieldTextParser;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.searchcomponents.core.search.StateTokenAndResultCount;
import com.hp.autonomy.searchcomponents.core.search.TypedStateToken;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SpringBootTest(classes = SavedSnapshotService.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SavedSnapshotServiceTest extends AbstractSavedSearchServiceTest<SavedSnapshot, SavedSnapshot.Builder> {
    private static final TypedStateToken normalToken =
        new TypedStateToken("normal", TypedStateToken.StateTokenType.QUERY);
    private static final TypedStateToken promotionsToken =
        new TypedStateToken("promotions", TypedStateToken.StateTokenType.PROMOTIONS);

    @SuppressWarnings("unused")
    @MockBean
    private SavedSnapshotRepository crudRepository;
    @MockBean
    private IdolDocumentsService documentsService;
    @MockBean
    private FieldTextParser fieldTextParser;
    @MockBean
    private ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;
    @MockBean
    private IdolQueryRestrictionsBuilder queryRestrictionsBuilder;

    @Before
    public void setUpSnapshot() {
        Mockito.when(queryRestrictionsBuilderFactory.getObject()).thenReturn(queryRestrictionsBuilder);
        Mockito.when(queryRestrictionsBuilder.queryText(Mockito.anyString())).thenReturn(queryRestrictionsBuilder);
        Mockito.when(queryRestrictionsBuilder.fieldText(Mockito.anyString())).thenReturn(queryRestrictionsBuilder);
        Mockito.when(queryRestrictionsBuilder.databases(Mockito.any())).thenReturn(queryRestrictionsBuilder);
        Mockito.when(queryRestrictionsBuilder.minDate(Mockito.any())).thenReturn(queryRestrictionsBuilder);
        Mockito.when(queryRestrictionsBuilder.maxDate(Mockito.any())).thenReturn(queryRestrictionsBuilder);
        Mockito.when(queryRestrictionsBuilder.minScore(Mockito.anyInt())).thenReturn(queryRestrictionsBuilder);
        Mockito.when(queryRestrictionsBuilder.stateMatchIds(Mockito.any())).thenReturn(queryRestrictionsBuilder);
        Mockito.when(queryRestrictionsBuilder.stateDontMatchIds(Mockito.any())).thenReturn(queryRestrictionsBuilder);

        Mockito.when(documentsService.getStateTokenAndResultCount(
            Mockito.any(), Mockito.anyInt(), Mockito.eq(false)
        )).thenReturn(new StateTokenAndResultCount(normalToken, 123));
        Mockito.when(documentsService.getStateTokenAndResultCount(
            Mockito.any(), Mockito.anyInt(), Mockito.eq(true)
        )).thenReturn(new StateTokenAndResultCount(promotionsToken, 456));
    }

    public SavedSnapshotServiceTest() {
        super(SavedSnapshot.Builder::new);
    }

    @Test
    public void testBuild() {
        final Set<ConceptClusterPhrase> conceptClusterPhrases =
            Collections.singleton(new ConceptClusterPhrase("query", true, 1));
        final SavedQuery query = new SavedQuery.Builder()
            .setConceptClusterPhrases(conceptClusterPhrases)
            .build();
        final SavedSnapshot snapshot = service.build(query);

        Mockito.verify(fieldTextParser).toFieldText(query, true);
        Mockito.verify(fieldTextParser).toFieldText(query, false);

        Assert.assertEquals("should copy input properties",
            conceptClusterPhrases,
            snapshot.getConceptClusterPhrases());
        Assert.assertEquals("should retrieve tokens",
            new HashSet<>(Arrays.asList(normalToken, promotionsToken)),
            snapshot.getStateTokens());
    }

}
