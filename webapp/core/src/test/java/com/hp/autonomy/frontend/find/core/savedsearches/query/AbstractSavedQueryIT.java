/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.savedsearches.ConceptClusterPhrase;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.frontend.find.core.test.MvcIntegrationTestUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
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
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
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

@SuppressWarnings("SpringJavaAutowiringInspection")
public abstract class AbstractSavedQueryIT extends AbstractFindIT {
    private static final TypeReference<Set<SavedQuery>> LIST_TYPE_REFERENCE = new TypeReference<Set<SavedQuery>>() {
    };

    private static final String TITLE = "Any old saved search";
    private static final String PRIMARY_PHRASE = "manhattan";
    private static final String OTHER_PHRASE = "mid-town";
    private static final Integer MIN_SCORE = 88;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MvcIntegrationTestUtils integrationTestUtils;

    @Value("classpath:save-query-request.json")
    private Resource saveQueryRequestResource;

    private JdbcTemplate jdbcTemplate;

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
                .andExpect(jsonPath("$.minScore", equalTo(MIN_SCORE)))
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

        final String updatedPhrase = "jersey";
        final Set<ConceptClusterPhrase> conceptClusterPhrases = new HashSet<>();
        conceptClusterPhrases.add(new ConceptClusterPhrase(updatedPhrase, true, 1));

        final Integer updatedMinScore = 99;
        final SavedQuery updatedQuery = new SavedQuery.Builder()
                .setMinScore(updatedMinScore)
                .setConceptClusterPhrases(conceptClusterPhrases)
                .build();

        final MockHttpServletRequestBuilder requestBuilder = put(SavedQueryController.PATH + '/' + createdEntity.getId())
                .with(authentication(biAuth()))
                .content(objectMapper.writeValueAsString(updatedQuery))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(createdEntity.getId().intValue())))
                .andExpect(jsonPath("$.minScore", equalTo(updatedMinScore)))
                .andExpect(jsonPath("$.conceptClusterPhrases", hasSize(1)))
                .andExpect(jsonPath("$.conceptClusterPhrases[*].phrase", contains(updatedPhrase)))
                .andExpect(jsonPath("$.conceptClusterPhrases[?(@.phrase=='" + updatedPhrase + "')].primary", contains(true)))
                .andExpect(jsonPath("$.conceptClusterPhrases[?(@.phrase== '" + updatedPhrase + "')].clusterId", contains(1)));
    }

    @Test
    public void createAndFetch() throws Exception {
        final byte[] requestBytes = IOUtils.toByteArray(saveQueryRequestResource.getInputStream());

        final MockHttpServletRequestBuilder requestBuilder = post(SavedQueryController.PATH + '/')
                .content(requestBytes)
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(biAuth()));

        final MvcResult createResult = mockMvc.perform(requestBuilder)
                .andReturn();

        final JsonNode responseTree = objectMapper.readTree(createResult.getResponse().getContentAsString());
        final int id = responseTree.get("id").asInt();

        listSavedQueries()
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$[0].id", is(id)))
                .andExpect(jsonPath("$[0].title", is("\u30e2\u30f3\u30ad\u30fc")))
                .andExpect(jsonPath("$[0].minDate", new ZonedDateTimeMatcher("2017-05-17T15:51:20Z")))
                .andExpect(jsonPath("$[0].maxDate", new ZonedDateTimeMatcher("2017-05-17T15:51:40Z")))
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
                .andExpect(jsonPath("$[0].numericRangeRestrictions", hasSize(1)))
                .andExpect(jsonPath("$[0].numericRangeRestrictions[0].field", is("SOME_NUMBER")))
                .andExpect(jsonPath("$[0].numericRangeRestrictions[0].min", is(123.5)))
                .andExpect(jsonPath("$[0].numericRangeRestrictions[0].max", is(124.5)))
                .andExpect(jsonPath("$[0].dateRangeRestrictions", hasSize(1)))
                .andExpect(jsonPath("$[0].dateRangeRestrictions[0].field", is("SOME_DATE")))
                .andExpect(jsonPath("$[0].dateRangeRestrictions[0].min", new ZonedDateTimeMatcher("2017-05-17T15:51:20Z")))
                .andExpect(jsonPath("$[0].dateRangeRestrictions[0].max", new ZonedDateTimeMatcher("2017-05-17T15:51:40Z")))
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
                .with(authentication(biAuth()));

        mockMvc.perform(requestBuilder).andExpect(status().isOk());

        final Set<SavedQuery> queries = listAndParseSavedQueries();
        assertThat(queries, is(empty()));
    }

    @Test
    public void getAllReturnsNothing() throws Exception {
        assertThat(listAndParseSavedQueries(), is(empty()));
    }

    @Test
    public void basicUserNotAuthorised() throws Exception {
        mockMvc.perform(get(SavedQueryController.PATH).with(authentication(userAuth())))
                .andExpect(status().is(403));
    }

    @Test
    public void checkTimeAuditDataInsertedUpdated() throws Exception {
        final SavedQuery inputSavedQuery = new SavedQuery.Builder()
                .setTitle("title")
                .setMinScore(0)
                .build();

        final SavedQuery savedQuery = createAndParseSavedQuery(inputSavedQuery);

        assertNotNull(savedQuery.getId());
        assertThat(savedQuery.getDateCreated(), isA(ZonedDateTime.class));
        assertThat(savedQuery.getDateModified(), isA(ZonedDateTime.class));
        assertTrue(savedQuery.getDateCreated().isEqual(savedQuery.getDateModified()));

        // Safe to assume completed in an hour
        // TODO: mock out the datetime service used by spring auditing to check this properly
        assertTrue(savedQuery.getDateCreated().plusHours(1).isAfter(ZonedDateTime.now()));

        savedQuery.setConceptClusterPhrases(Collections.singleton(new ConceptClusterPhrase("*", true, -1)));

        final SavedQuery savedQueryUpdate = new SavedQuery.Builder()
                .setId(savedQuery.getId())
                .setTitle("new title")
                .setMinScore(0)
                .build();

        final SavedQuery updatedSavedQuery = updateAndParseSavedQuery(savedQueryUpdate);

        assertThat(updatedSavedQuery.getDateCreated(), isA(ZonedDateTime.class));
        assertThat(updatedSavedQuery.getDateModified(), isA(ZonedDateTime.class));
        assertTrue(updatedSavedQuery.getDateModified().isAfter(savedQuery.getDateCreated()));
    }

    @Test
    public void checkUserNotDuplicated() throws Exception {
        final SavedQuery savedQuery1 = new SavedQuery.Builder()
                .setTitle("title1")
                .setMinScore(0)
                .build();

        final SavedQuery savedQuery2 = new SavedQuery.Builder()
                .setTitle("title2")
                .setMinScore(0)
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
                .setMinScore(0)
                .setIndexes(indexes)
                .setConceptClusterPhrases(Collections.singleton(new ConceptClusterPhrase("*", true, -1)))
                .build();

        final SavedQuery saveRequest2 = new SavedQuery.Builder()
                .setDateDocsLastFetched(ZonedDateTime.now())
                .setTitle("title2")
                .setMinScore(0)
                .setIndexes(indexes)
                .setConceptClusterPhrases(Collections.singleton(new ConceptClusterPhrase("*", true, -1)))
                .build();

        final SavedQuery savedQuery1 = createAndParseSavedQuery(saveRequest1);
        final long id1 = savedQuery1.getId();

        final SavedQuery savedQuery2 = createAndParseSavedQuery(saveRequest2);
        final long id2 = savedQuery2.getId();

        mockMvc.perform(get(SavedQueryController.PATH + NEW_RESULTS_PATH + '/' + id1).with(authentication(biAuth())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", greaterThan(0)));

        mockMvc.perform(get(SavedQueryController.PATH + NEW_RESULTS_PATH + '/' + id2).with(authentication(biAuth())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", is(0))); // likely to work though not foolproof
    }

    private ResultActions createSavedQuery(final SavedQuery savedQuery) throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post(SavedQueryController.PATH + '/')
                .content(objectMapper.writeValueAsString(savedQuery))
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(biAuth()));

        return mockMvc.perform(requestBuilder);
    }

    private SavedQuery createAndParseSavedQuery(final SavedQuery savedQuery) throws Exception {
        final MvcResult mvcResult = createSavedQuery(savedQuery).andReturn();
        final String response = mvcResult.getResponse().getContentAsString();
        return objectMapper.readValue(response, SavedQuery.class);
    }

    private SavedQuery updateAndParseSavedQuery(final SavedQuery update) throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = put(SavedQueryController.PATH + '/' + update.getId())
                .content(objectMapper.writeValueAsString(update))
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(biAuth()));

        final MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        final String response = mvcResult.getResponse().getContentAsString();
        return objectMapper.readValue(response, SavedQuery.class);
    }

    private ResultActions listSavedQueries() throws Exception {
        return mockMvc.perform(get(SavedQueryController.PATH).with(authentication(biAuth())));
    }

    private Set<SavedQuery> listAndParseSavedQueries() throws Exception {
        final MvcResult listResult = listSavedQueries()
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(listResult.getResponse().getContentAsString(), LIST_TYPE_REFERENCE);
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
                .setMinScore(MIN_SCORE)
                .setConceptClusterPhrases(getBaseConceptClusterPhrases())
                .build();
    }

    private static class ZonedDateTimeMatcher extends BaseMatcher<ChronoZonedDateTime<?>> {
        private final ChronoZonedDateTime<?> expectation;

        private ZonedDateTimeMatcher(final CharSequence expectation) {
            this.expectation = ZonedDateTime.parse(expectation);
        }

        @Override
        public boolean matches(final Object o) {
            return o instanceof CharSequence && ZonedDateTime.parse((CharSequence) o).isEqual(expectation);
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText(expectation.toString());
        }
    }
}
