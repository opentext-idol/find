/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.savedsearches.ConceptClusterPhrase;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractSavedQueryServiceIT extends AbstractFindIT {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private SavedQueryService savedQueryService;

    private ObjectMapper mapper = new ObjectMapper();

    private final String TITLE = "Any old saved search";
    private final String QUERY_TEXT = "orange";
    private final String PRIMARY_PHRASE = "manhattan";
    private final String OTHER_PHRASE = "mid-town";

    private Set<ConceptClusterPhrase> getBaseConceptClusterPhrases() {
        final Set<ConceptClusterPhrase> conceptClusterPhrases = new HashSet<>();
        final ConceptClusterPhrase manhattanClusterPhraseOne = new ConceptClusterPhrase(PRIMARY_PHRASE, true, 0);
        final ConceptClusterPhrase manhattanClusterPhraseTwo = new ConceptClusterPhrase(OTHER_PHRASE, false, 0);
        conceptClusterPhrases.add(manhattanClusterPhraseOne);
        conceptClusterPhrases.add(manhattanClusterPhraseTwo);

        return conceptClusterPhrases;
    }

    private SavedQuery getBaseSavedQuery() {
        return new SavedQuery.Builder()
                .setTitle(TITLE)
                .setQueryText(QUERY_TEXT)
                .setConceptClusterPhrases(getBaseConceptClusterPhrases())
                .build();
    }

    @Test
    public void create() throws Exception {
        mockMvc.perform(post(SavedQueryController.PATH + '/')
                .content(mapper.writeValueAsString(getBaseSavedQuery()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", not(nullValue())))
                .andExpect(jsonPath("$.title", equalTo(TITLE)))
                .andExpect(jsonPath("$.queryText", equalTo(QUERY_TEXT)))
                .andExpect(jsonPath("$.conceptClusterPhrases", hasSize(2)))
                .andExpect(jsonPath("$.conceptClusterPhrases[*].phrase", containsInAnyOrder(PRIMARY_PHRASE, OTHER_PHRASE)))
                .andExpect(jsonPath("$.conceptClusterPhrases[?(@.phrase=='" + PRIMARY_PHRASE + "')].primary", contains(true)))
                .andExpect(jsonPath("$.conceptClusterPhrases[?(@.phrase== '" + PRIMARY_PHRASE + "')].clusterId", contains(0)))
                .andExpect(jsonPath("$.conceptClusterPhrases[?(@.phrase== '" + OTHER_PHRASE + "')].primary", contains(false)))
                .andExpect(jsonPath("$.conceptClusterPhrases[?(@.phrase=='" + OTHER_PHRASE + "')].clusterId", contains(0)));
    }

    @Test
    public void update() throws Exception {
        final SavedQuery createdEntity = savedQueryService.create(getBaseSavedQuery());

        final String UPDATED_QUERY_TEXT = "banana";
        final String UPDATED_PHRASE = "jersey";
        final Set<ConceptClusterPhrase> conceptClusterPhrases = new HashSet<>();
        conceptClusterPhrases.add(new ConceptClusterPhrase(UPDATED_PHRASE, true, 1));

        final SavedQuery updatedQuery = new SavedQuery.Builder()
                .setQueryText(UPDATED_QUERY_TEXT)
                .setConceptClusterPhrases(conceptClusterPhrases)
                .build();

        mockMvc.perform(put(SavedQueryController.PATH + '/' + createdEntity.getId())
                .content(mapper.writeValueAsString(updatedQuery))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(createdEntity.getId().intValue())))
                .andExpect(jsonPath("$.queryText", equalTo(UPDATED_QUERY_TEXT)))
                .andExpect(jsonPath("$.conceptClusterPhrases", hasSize(1)))
                .andExpect(jsonPath("$.conceptClusterPhrases[*].phrase", contains(UPDATED_PHRASE)))
                .andExpect(jsonPath("$.conceptClusterPhrases[?(@.phrase=='" + UPDATED_PHRASE + "')].primary", contains(true)))
                .andExpect(jsonPath("$.conceptClusterPhrases[?(@.phrase== '" + UPDATED_PHRASE + "')].clusterId", contains(1)));
    }

    @Test
    public void fetch() throws Exception {
        final SavedQuery createdEntity = savedQueryService.create(getBaseSavedQuery());

        mockMvc.perform(get(SavedQueryController.PATH + '/')
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$[0].id", is(createdEntity.getId().intValue())))
                .andExpect(jsonPath("$[0].queryText", is(createdEntity.getQueryText())))
                .andExpect(jsonPath("$[0].conceptClusterPhrases", hasSize(createdEntity.getConceptClusterPhrases().size())))
                .andExpect(jsonPath("$[0].conceptClusterPhrases[*].phrase", containsInAnyOrder(PRIMARY_PHRASE, OTHER_PHRASE)))
                .andExpect(jsonPath("$[0].conceptClusterPhrases[?(@.phrase=='" + PRIMARY_PHRASE + "')].primary", contains(true)))
                .andExpect(jsonPath("$[0].conceptClusterPhrases[?(@.phrase== '" + PRIMARY_PHRASE + "')].clusterId", contains(0)))
                .andExpect(jsonPath("$[0].conceptClusterPhrases[?(@.phrase== '" + OTHER_PHRASE + "')].primary", contains(false)))
                .andExpect(jsonPath("$[0].conceptClusterPhrases[?(@.phrase== '" + OTHER_PHRASE + "')].clusterId", contains(0)));
    }

    @Test
    public void deleteById() throws Exception {
        final SavedQuery createdEntity = savedQueryService.create(getBaseSavedQuery());

        mockMvc.perform(delete(SavedQueryController.PATH + '/' + createdEntity.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        final Set<SavedQuery> queries = savedQueryService.getAll();
        assertThat(queries, is(empty()));
    }

    @Test
    public void getAllReturnsNothing() throws Exception {
        assertThat(savedQueryService.getAll(), is(empty()));
    }

    @Test
    public void checkUserAuditDataInserted() {
        final SavedQuery savedQuery = new SavedQuery.Builder()
                .setTitle("title")
                .setQueryText("*")
                .build();

        savedQueryService.create(savedQuery);

        assertNotNull(savedQuery.getId());
        assertThat(savedQuery.getUser().getUserId(), isA(Long.class));
        assertNotNull(savedQuery.getUser().getUserId());
    }

    @Test
    public void checkTimeAuditDataInsertedUpdated() {
        final SavedQuery savedQuery = savedQueryService.create(new SavedQuery.Builder()
                .setTitle("title")
                .setQueryText("*")
                .build());

        assertNotNull(savedQuery.getId());
        assertThat(savedQuery.getDateCreated(), isA(DateTime.class));
        assertThat(savedQuery.getDateModified(), isA(DateTime.class));
        assertTrue(savedQuery.getDateCreated().isEqual(savedQuery.getDateModified().toInstant()));

        // Safe to assume completed in an hour
        // TODO: mock out the datetime service used by spring auditing to check this properly
        assertTrue(savedQuery.getDateCreated().plusHours(1).isAfterNow());

        savedQuery.setQueryText("*");

        final SavedQuery updatedQuery = savedQueryService.update(new SavedQuery.Builder()
                .setId(savedQuery.getId())
                .setTitle("new title")
                .build());

        assertThat(updatedQuery.getDateCreated(), isA(DateTime.class));
        assertThat(updatedQuery.getDateModified(), isA(DateTime.class));
        assertTrue(updatedQuery.getDateModified().isAfter(savedQuery.getDateCreated().toInstant()));
    }

    @Test
    public void checkUserNotDuplicated() {
        final SavedQuery savedQuery1 = new SavedQuery.Builder()
                .setTitle("title1")
                .setQueryText("*")
                .build();

        final SavedQuery savedQuery2 = new SavedQuery.Builder()
                .setTitle("title2")
                .setQueryText("*")
                .build();

        savedQueryService.create(savedQuery1);
        savedQueryService.create(savedQuery2);

        assertNotNull(savedQuery1.getUser().getUserId());
        assertNotNull(savedQuery2.getUser().getUserId());
        assertEquals(savedQuery1.getUser().getUserId(), savedQuery2.getUser().getUserId());
    }
}
