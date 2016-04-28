package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.IdolFindApplication;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.frontend.find.core.test.MvcIntegrationTestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringApplicationConfiguration(classes = IdolFindApplication.class)
public class SavedSnapshotIT extends AbstractFindIT {
    private static final TypeReference<Set<SavedSnapshot>> LIST_TYPE_REFERENCE = new TypeReference<Set<SavedSnapshot>>() {};

    private final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private MvcIntegrationTestUtils mvcIntegrationTestUtils;

    private final String QUERY_TEXT = "orange";

    @Test
    public void create() throws Exception {
        createSnapshot(getBaseSavedSnapshot())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", not(nullValue())))
                .andExpect(jsonPath("$.resultCount", not(nullValue())))
                .andExpect(jsonPath("$.stateTokens", not(empty())));
    }

    @Test
    public void update() throws Exception {
        final SavedSnapshot createdEntity = createAndParseSnapshot(getBaseSavedSnapshot());

        final String updatedQueryText = "banana";
        final String updatedTitle = "a new title";

        final SavedSnapshot updatedSnapshot = new SavedSnapshot.Builder()
                .setQueryText(updatedQueryText)
                .setTitle(updatedTitle)
                .build();

        final MockHttpServletRequestBuilder requestBuilder = put(SavedSnapshotController.PATH + '/' + createdEntity.getId())
                .content(mapper.writeValueAsString(updatedSnapshot))
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(createdEntity.getId().intValue())))
                .andExpect(jsonPath("$.queryText", is(QUERY_TEXT))) // Only title is updateable for snapshots
                .andExpect(jsonPath("$.title", is(updatedTitle)));
    }

    @Test
    public void fetch() throws Exception {
        final SavedSnapshot createdEntity = createAndParseSnapshot(getBaseSavedSnapshot());

        listSnapshots()
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$[0].id", is(createdEntity.getId().intValue())))
                .andExpect(jsonPath("$[0].resultCount", not(nullValue())))
                .andExpect(jsonPath("$[0].stateTokens", not(empty())));
    }

    @Test
    public void deleteById() throws Exception {
        final SavedSnapshot createdEntity = createAndParseSnapshot(getBaseSavedSnapshot());

        final MockHttpServletRequestBuilder requestBuilder = delete(SavedSnapshotController.PATH + '/' + createdEntity.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder).andExpect(status().isOk());

        final Set<SavedSnapshot> queries = listAndParseSnapshots();
        assertThat(queries, is(empty()));
    }

    @Test
    public void getAllReturnsNothing() throws Exception {
        assertThat(listAndParseSnapshots(), is(empty()));
    }

    private ResultActions createSnapshot(final SavedSnapshot snapshot) throws Exception {
        return mockMvc.perform(
                post(SavedSnapshotController.PATH)
                        .content(mapper.writeValueAsString(snapshot))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(userAuth()))
        );
    }

    private SavedSnapshot createAndParseSnapshot(final SavedSnapshot snapshot) throws Exception {
        final MvcResult mvcResult = createSnapshot(snapshot).andReturn();
        final String response = mvcResult.getResponse().getContentAsString();
        return mapper.readValue(response, SavedSnapshot.class);
    }

    private ResultActions listSnapshots() throws Exception {
        return mockMvc.perform(get(SavedSnapshotController.PATH).with(authentication(userAuth())));
    }

    private Set<SavedSnapshot> listAndParseSnapshots() throws Exception {
        final MvcResult listResult = listSnapshots()
                .andExpect(status().isOk())
                .andReturn();

        return mapper.readValue(listResult.getResponse().getContentAsString(), LIST_TYPE_REFERENCE);
    }

    private SavedSnapshot getBaseSavedSnapshot() {
        final Set<EmbeddableIndex> embeddableIndexes = new HashSet<>();

        for (final String database : mvcIntegrationTestUtils.getDatabases()) {
            embeddableIndexes.add(new EmbeddableIndex(database, null));
        }

        return new SavedSnapshot.Builder()
                .setTitle("Any old saved search")
                .setQueryText(QUERY_TEXT)
                .setIndexes(embeddableIndexes)
                .build();
    }
}
