package com.hp.autonomy.frontend.find.core.savedsearches.snapshot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.searchcomponents.core.search.StateTokenAndResultCount;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.Serializable;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractSavedSnapshotServiceIT<S extends Serializable, R extends SearchResult, E extends Exception> extends AbstractFindIT {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected SavedSnapshotService savedSnapshotService;

    @Mock
    protected DocumentsService<S, R, E> documentsService;

    protected SavedSnapshotController controller;

    private ObjectMapper mapper = new ObjectMapper();

    private final String QUERY_TEXT = "orange";
    private final Long RESULT_COUNT = 555L;
    private final String STATE_TOKEN = "a state token";

    protected abstract void initController();

    private SavedSnapshot getBaseSavedSnapshot() {
        return new SavedSnapshot.Builder()
                .setTitle("Any old saved search")
                .setQueryText(QUERY_TEXT)
                .build();
    }

    @Before
    public void setup() throws E {
        MockitoAnnotations.initMocks(this);

        when(documentsService.getStateTokenAndResultCount(Matchers.<QueryRestrictions<S>>any(), anyInt()))
                .thenReturn(new StateTokenAndResultCount(STATE_TOKEN, RESULT_COUNT));

        initController();
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void create() throws Exception {
        mockMvc.perform(post(SavedSnapshotController.PATH + '/')
                .content(mapper.writeValueAsString(getBaseSavedSnapshot()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", not(nullValue())))
                .andExpect(jsonPath("$.resultCount", is(RESULT_COUNT.intValue())))
                .andExpect(jsonPath("$.stateTokens", contains(STATE_TOKEN)));
    }

    @Test
    public void update() throws Exception {
        final SavedSnapshot createdEntity = savedSnapshotService.create(getBaseSavedSnapshot());

        final String UPDATED_QUERY_TEXT = "banana";
        final String UPDATED_TITLE = "a new title";

        final SavedSnapshot updatedSnapshot = new SavedSnapshot.Builder()
                .setQueryText(UPDATED_QUERY_TEXT)
                .setTitle(UPDATED_TITLE)
                .build();

        mockMvc.perform(put(SavedSnapshotController.PATH + '/' + createdEntity.getId())
                .content(mapper.writeValueAsString(updatedSnapshot))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(createdEntity.getId().intValue())))
                .andExpect(jsonPath("$.queryText", is(QUERY_TEXT))) // Only title is updateable for snapshots
                .andExpect(jsonPath("$.title", is(UPDATED_TITLE)));
    }

    @Test
    public void fetch() throws Exception {
        final SavedSnapshot createdEntity = controller.create(getBaseSavedSnapshot());

        mockMvc.perform(get(SavedSnapshotController.PATH + '/')
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$[0].id", is(createdEntity.getId().intValue())))
                .andExpect(jsonPath("$[0].resultCount", is(RESULT_COUNT.intValue())))
                .andExpect(jsonPath("$[0].stateTokens", contains(STATE_TOKEN)));
    }

    @Test
    public void deleteById() throws Exception {
        final SavedSnapshot createdEntity = controller.create(getBaseSavedSnapshot());

        mockMvc.perform(delete(SavedSnapshotController.PATH + '/' + createdEntity.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        final Set<SavedSnapshot> queries = savedSnapshotService.getAll();
        assertThat(queries, is(empty()));
    }

    @Test
    public void getAllReturnsNothing() throws Exception {
        assertThat(savedSnapshotService.getAll(), is(empty()));
    }
}
