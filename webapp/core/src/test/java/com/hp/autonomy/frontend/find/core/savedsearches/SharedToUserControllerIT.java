package com.hp.autonomy.frontend.find.core.savedsearches;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("ProhibitedExceptionDeclared")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup(value = "classpath:shared-to-user.xml", connection = "testConnection")
@DbUnitConfiguration(databaseConnection = "testConnection")
public abstract class SharedToUserControllerIT extends AbstractFindIT {
    private static String json;
    private static String deleteJson;
    private static String failedDeleteJson;

    @BeforeClass
    public static void initJson() throws IOException {
        json = IOUtils.toString(SharedToUserControllerIT.class.getResourceAsStream("/shared-to-users.json"));
        deleteJson = IOUtils.toString(SharedToUserControllerIT.class.getResourceAsStream("/delete-shared-to-users.json"));
        failedDeleteJson = IOUtils.toString(SharedToUserControllerIT.class.getResourceAsStream("/delete-non-existing-shared-to-users.json"));
    }

    @Test
    public void getPermissionsForSearch() throws Exception {
        final MockHttpServletRequestBuilder getRequestBuilder = get(SharedToUserController.SHARED_SEARCHES_PATH
                + SharedToUserController.PERMISSIONS_PATH)
                .with(authentication(biAuth()));
        getRequestBuilder.param(SharedToUserController.SEARCH_ID_PARAM, "1");

        mockMvc.perform(getRequestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id.searchId", is(1)))
                .andExpect(jsonPath("$[1].id.searchId", is(1)))
                .andExpect(jsonPath("$[2].id.searchId", is(1)))
                .andReturn();
    }

    @Test
    public void save() throws Exception {
        final MockHttpServletRequestBuilder saveRequestBuilder = get(SharedToUserController.SHARED_SEARCHES_PATH
                + SharedToUserController.SAVE_PERMISSION_PATH)
                .with(authentication(biAuth()));
        saveRequestBuilder.param(SharedToUserController.DATA_PARAM, json);

        mockMvc.perform(saveRequestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().string(notNullValue()));

    }

    @Test
    public void tryToSaveExistingPermission() throws Exception {
        final MockHttpServletRequestBuilder saveRequestBuilder = get(SharedToUserController.SHARED_SEARCHES_PATH
                + SharedToUserController.SAVE_PERMISSION_PATH)
                .with(authentication(biAuth()));
        saveRequestBuilder.param(SharedToUserController.DATA_PARAM, json);

        mockMvc.perform(saveRequestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().string(notNullValue()))
                .andReturn();

        mockMvc.perform(saveRequestBuilder)
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void deleteExistingPermission() throws Exception {
        final MockHttpServletRequestBuilder deleteRequestBuilder = get(SharedToUserController.SHARED_SEARCHES_PATH
                + SharedToUserController.DELETE_PERMISSION_PATH)
                .with(authentication(biAuth()));
        deleteRequestBuilder.param(SharedToUserController.DATA_PARAM, deleteJson);

        mockMvc.perform(deleteRequestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void deleteNonExistingPermission() throws Exception {
        final MockHttpServletRequestBuilder deleteRequestBuilder = get(SharedToUserController.SHARED_SEARCHES_PATH
                + SharedToUserController.DELETE_PERMISSION_PATH)
                .with(authentication(biAuth()));
        deleteRequestBuilder.param(SharedToUserController.DATA_PARAM, failedDeleteJson);

        mockMvc.perform(deleteRequestBuilder)
                .andExpect(status().isOk())
                .andReturn();
    }
}
