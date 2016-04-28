/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.hp.autonomy.frontend.find.core.savedsearches.ConceptClusterPhrase;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.frontend.find.core.test.MvcIntegrationTestUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryController.NEW_RESULTS_PATH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public abstract class AbstractSavedQueryIT extends AbstractFindIT {
    private static final TypeReference<Set<SavedQuery>> LIST_TYPE_REFERENCE = new TypeReference<Set<SavedQuery>>() {
    };

    private static final String TITLE = "Any old saved search";
    private static final String QUERY_TEXT = "orange";
    private static final String PRIMARY_PHRASE = "manhattan";
    private static final String OTHER_PHRASE = "mid-town";

    private final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    protected DataSource dataSource;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    protected MvcIntegrationTestUtils integrationTestUtils;

    @Value("classpath:save-query-request.json")
    private Resource saveQueryRequestResource;

    private JdbcTemplate jdbcTemplate;

    public AbstractSavedQueryIT() {
        mapper.registerModule(new JodaModule());
    }

    @PostConstruct
    public void initialise() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void create() throws Exception {
        createSavedQuery(getBaseSavedQuery())
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
        final SavedQuery createdEntity = createAndParseSavedQuery(getBaseSavedQuery());

        final String updatedQueryText = "banana";
        final String updatedPhrase = "jersey";
        final Set<ConceptClusterPhrase> conceptClusterPhrases = new HashSet<>();
        conceptClusterPhrases.add(new ConceptClusterPhrase(updatedPhrase, true, 1));

        final SavedQuery updatedQuery = new SavedQuery.Builder()
                .setQueryText(updatedQueryText)
                .setConceptClusterPhrases(conceptClusterPhrases)
                .build();

        final MockHttpServletRequestBuilder requestBuilder = put(SavedQueryController.PATH + '/' + createdEntity.getId())
                .with(authentication(userAuth()))
                .content(mapper.writeValueAsString(updatedQuery))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(createdEntity.getId().intValue())))
                .andExpect(jsonPath("$.queryText", equalTo(updatedQueryText)))
                .andExpect(jsonPath("$.conceptClusterPhrases", hasSize(1)))
                .andExpect(jsonPath("$.conceptClusterPhrases[*].phrase", contains(updatedPhrase)))
                .andExpect(jsonPath("$.conceptClusterPhrases[?(@.phrase=='" + updatedPhrase + "')].primary", contains(true)))
                .andExpect(jsonPath("$.conceptClusterPhrases[?(@.phrase== '" + updatedPhrase + "')].clusterId", contains(1)));
    }

    @Test
    public void createAndFetch() throws Exception {
        // Query text containing U+1F435
        final String queryText = "monkey face character \uD83D\uDC35";

        final byte[] requestBytes = IOUtils.toByteArray(saveQueryRequestResource.getInputStream());

        final MockHttpServletRequestBuilder requestBuilder = post(SavedQueryController.PATH + '/')
                .content(requestBytes)
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(userAuth()));

        final MvcResult createResult = mockMvc.perform(requestBuilder)
                .andReturn();

        final JsonNode responseTree = mapper.readTree(createResult.getResponse().getContentAsString());
        final int id = responseTree.get("id").asInt();

        listSavedQueries()
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$[0].id", is(id)))
                .andExpect(jsonPath("$[0].title", is("\u30e2\u30f3\u30ad\u30fc")))
                .andExpect(jsonPath("$[0].queryText", is(queryText)))
                .andExpect(jsonPath("$[0].minDate", is(1400000000)))
                .andExpect(jsonPath("$[0].maxDate", is(1500000000)))
                .andExpect(jsonPath("$[0].conceptClusterPhrases", hasSize(3)))
                .andExpect(jsonPath("$[0].conceptClusterPhrases[*].phrase", containsInAnyOrder("characters", "faces", "animals")))
                .andExpect(jsonPath("$[0].conceptClusterPhrases[?(@.phrase=='characters')].primary", contains(true)))
                .andExpect(jsonPath("$[0].conceptClusterPhrases[?(@.phrase=='characters')].clusterId", contains(1)))
                .andExpect(jsonPath("$[0].conceptClusterPhrases[?(@.phrase=='faces')].primary", contains(false)))
                .andExpect(jsonPath("$[0].conceptClusterPhrases[?(@.phrase=='faces')].clusterId", contains(1)))
                .andExpect(jsonPath("$[0].conceptClusterPhrases[?(@.phrase=='animals')].primary", contains(true)))
                .andExpect(jsonPath("$[0].conceptClusterPhrases[?(@.phrase=='animals')].clusterId", contains(2)))
                .andExpect(jsonPath("$[0].parametricValues", hasSize(1)))
                .andExpect(jsonPath("$[0].parametricValues[0].field", is("CATEGORY")))
                .andExpect(jsonPath("$[0].parametricValues[0].value", is("COMPUTING")))
                .andExpect(jsonPath("$[0].indexes", hasSize(2)))
                .andExpect(jsonPath("$[0].indexes[*].name", containsInAnyOrder("English Wikipedia", "\u65e5\u672c\u8a9e Wikipedia")))
                .andExpect(jsonPath("$[0].indexes[?(@.name=='English Wikipedia')].domain", contains("MY_DOMAIN")))
                .andExpect(jsonPath("$[0].indexes[?(@.name=='\u65e5\u672c\u8a9e Wikipedia')].domain", contains("MY_DOMAIN")));
    }

    @Test
    public void deleteById() throws Exception {
        final SavedQuery createdEntity = createAndParseSavedQuery(getBaseSavedQuery());

        final MockHttpServletRequestBuilder requestBuilder = delete(SavedQueryController.PATH + '/' + createdEntity.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder).andExpect(status().isOk());

        final Set<SavedQuery> queries = listAndParseSavedQueries();
        assertThat(queries, is(empty()));
    }

    @Test
    public void getAllReturnsNothing() throws Exception {
        assertThat(listAndParseSavedQueries(), is(empty()));
    }

    @Test
    public void checkTimeAuditDataInsertedUpdated() throws Exception {
        final SavedQuery inputSavedQuery = new SavedQuery.Builder()
                .setTitle("title")
                .setQueryText("*")
                .build();

        final SavedQuery savedQuery = createAndParseSavedQuery(inputSavedQuery);

        assertNotNull(savedQuery.getId());
        assertThat(savedQuery.getDateCreated(), isA(DateTime.class));
        assertThat(savedQuery.getDateModified(), isA(DateTime.class));
        assertTrue(savedQuery.getDateCreated().isEqual(savedQuery.getDateModified().toInstant()));

        // Safe to assume completed in an hour
        // TODO: mock out the datetime service used by spring auditing to check this properly
        assertTrue(savedQuery.getDateCreated().plusHours(1).isAfterNow());

        savedQuery.setQueryText("*");

        final SavedQuery savedQueryUpdate = new SavedQuery.Builder()
                .setId(savedQuery.getId())
                .setTitle("new title")
                .build();

        final SavedQuery updatedSavedQuery = updateAndParseSavedQuery(savedQueryUpdate);

        assertThat(updatedSavedQuery.getDateCreated(), isA(DateTime.class));
        assertThat(updatedSavedQuery.getDateModified(), isA(DateTime.class));
        assertTrue(updatedSavedQuery.getDateModified().isAfter(savedQuery.getDateCreated().toInstant()));
    }

    @Test
    public void checkUserNotDuplicated() throws Exception {
        final SavedQuery savedQuery1 = new SavedQuery.Builder()
                .setTitle("title1")
                .setQueryText("*")
                .build();

        final SavedQuery savedQuery2 = new SavedQuery.Builder()
                .setTitle("title2")
                .setQueryText("*")
                .build();

        createSavedQuery(savedQuery1);
        createSavedQuery(savedQuery2);

        final int userRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, "find." + UserEntity.Table.NAME);
        assertThat(userRows, is(1));
    }

    @Test
    public void checkForNewQueryResults() throws Exception {
        final Set<EmbeddableIndex> indexes = Collections.singleton(integrationTestUtils.getEmbeddableIndex());

        final SavedQuery saveRequest1 = new SavedQuery.Builder()
                .setTitle("title1")
                .setQueryText("*")
                .setIndexes(indexes)
                .build();

        final SavedQuery saveRequest2 = new SavedQuery.Builder()
                .setDateNewDocsLastFetched(DateTime.now())
                .setTitle("title2")
                .setQueryText("*")
                .setIndexes(indexes)
                .build();

        final SavedQuery savedQuery1 = createAndParseSavedQuery(saveRequest1);
        final long id1 = savedQuery1.getId();

        final SavedQuery savedQuery2 = createAndParseSavedQuery(saveRequest2);
        final long id2 = savedQuery2.getId();

        mockMvc.perform(get(SavedQueryController.PATH + NEW_RESULTS_PATH + id1).with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", is(true)));

        mockMvc.perform(get(SavedQueryController.PATH + NEW_RESULTS_PATH + id2).with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", is(false))); // likely to work though not full-proof
    }

    private ResultActions createSavedQuery(final SavedQuery savedQuery) throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post(SavedQueryController.PATH + '/')
                .content(mapper.writeValueAsString(savedQuery))
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(userAuth()));

        return mockMvc.perform(requestBuilder);
    }

    private SavedQuery createAndParseSavedQuery(final SavedQuery savedQuery) throws Exception {
        final MvcResult mvcResult = createSavedQuery(savedQuery).andReturn();
        final String response = mvcResult.getResponse().getContentAsString();
        return mapper.readValue(response, SavedQuery.class);
    }

    private SavedQuery updateAndParseSavedQuery(final SavedQuery update) throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = put(SavedQueryController.PATH + '/' + update.getId())
                .content(mapper.writeValueAsString(update))
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(userAuth()));

        final MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        final String response = mvcResult.getResponse().getContentAsString();
        return mapper.readValue(response, SavedQuery.class);
    }

    private ResultActions listSavedQueries() throws Exception {
        return mockMvc.perform(get(SavedQueryController.PATH).with(authentication(userAuth())));
    }

    private Set<SavedQuery> listAndParseSavedQueries() throws Exception {
        final MvcResult listResult = listSavedQueries()
                .andExpect(status().isOk())
                .andReturn();

        return mapper.readValue(listResult.getResponse().getContentAsString(), LIST_TYPE_REFERENCE);
    }

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
}
