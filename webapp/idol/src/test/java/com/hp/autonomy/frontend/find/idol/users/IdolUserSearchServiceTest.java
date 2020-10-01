/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.frontend.find.core.configuration.RelatedUsersConfig;
import com.hp.autonomy.frontend.find.core.configuration.RelatedUsersSourceConfig;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldValue;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.searchcomponents.idol.search.QueryResponseParser;
import com.hp.autonomy.types.idol.responses.DocContent;
import com.hp.autonomy.types.idol.responses.Hit;
import com.hp.autonomy.types.idol.responses.QueryResponseData;
import com.hp.autonomy.types.idol.responses.User;
import com.hp.autonomy.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public class IdolUserSearchServiceTest {
    private static final RelatedUsersConfig defaultConfig = RelatedUsersConfig.builder()
        .enabled(true)
        .interests(RelatedUsersSourceConfig.builder()
            .agentStoreProfilesDatabase("db-i").namedArea("area-i").build())
        .expertise(RelatedUsersSourceConfig.builder()
            .agentStoreProfilesDatabase("db-e").namedArea("area-e").build())
        .build();

    @Mock private UserService userService;
    @Mock private QueryResponseParser queryResponseParser;
    private IdolUserSearchService service;

    /**
     * @param name User field name
     * @param value User field value
     * @return AgentStore user profile document hit
     */
    private Hit makeUserProfile(final String name, final String value, final double weight) {
        final FieldInfo<String> fieldValue = FieldInfo.<String>builder()
            .value(new FieldValue<>(value, value))
            .build();
        final Map<String, FieldInfo<String>> fields = Collections.singletonMap(name, fieldValue);
        final IdolSearchResult searchResult = IdolSearchResult.builder()
            .fieldMap(fields)
            .weight(weight)
            .build();
        final DocContent content = Mockito.mock(DocContent.class);
        Mockito.when(content.getContent()).thenReturn(Collections.singletonList(searchResult));
        final Hit hit = new Hit();
        hit.setContent(content);
        return hit;
    }

    private Answer<QueryResponseData> getProfilesAnswer(final List<Hit> profileHits) {
        return inv -> {
            final int start = Math.min((int) inv.getArguments()[3] - 1, profileHits.size());
            final int maxResults = Math.min((int) inv.getArguments()[4], profileHits.size());
            final QueryResponseData profiles = new QueryResponseData();
            profiles.getHits().addAll(profileHits.subList(start, maxResults));
            return profiles;
        };
    }

    /**
     * Mock services so that user profiles are returned as given, and processed correctly.
     *
     * @param interestsProfileHits AgentStore user profile document hits, from the interests profile
     *                             source, ordered, across all pages
     * @param expertiseProfileHits as above, from the expertise profile source
     */
    private void mockProfiles(
        final List<Hit> interestsProfileHits, final List<Hit> expertiseProfileHits
    ) {
        Mockito.when(userService.getRelatedToSearch(
            eq("db-i"), eq("area-i"), any(), anyInt(), anyInt())
        ).thenAnswer(getProfilesAnswer(interestsProfileHits));
        Mockito.when(userService.getRelatedToSearch(
            eq("db-e"), eq("area-e"), any(), anyInt(), anyInt())
        ).thenAnswer(getProfilesAnswer(expertiseProfileHits));

        Mockito.when(queryResponseParser.parseQueryHits(any())).thenAnswer(inv -> {
            final List<Hit> hits = (List<Hit>) inv.getArguments()[0];
            return hits.stream()
                .map(hit -> (IdolSearchResult) hit.getContent().getContent().get(0))
                .collect(Collectors.toList());
        });

        Mockito.when(userService.getUsersDetails(any())).thenAnswer(inv -> {
            return ((List<String>) inv.getArguments()[0]).stream()
                .map(username -> {
                    final User user = new User();
                    user.setUsername(username);
                    return user;
                })
                .collect(Collectors.toList());
        });
    }

    @Before
    public void setUp() {
        service = new IdolUserSearchServiceImpl(userService, queryResponseParser);
    }

    @Test
    public void testGetRelatedToSearch_onlyInterested() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1", 50),
            makeUserProfile("USERNAME", "u2", 40),
            makeUserProfile("USERNAME", "u3", 30)
        ), Collections.emptyList());

        final List<RelatedUser> users = service.getRelatedToSearch(defaultConfig, "text", 30);
        Assert.assertEquals(3, users.size());
        Assert.assertEquals("u1", users.get(0).getUser().getUsername());
        Assert.assertFalse("u1", users.get(0).isExpert());
        Assert.assertEquals("u2", users.get(1).getUser().getUsername());
        Assert.assertFalse("u2", users.get(1).isExpert());
        Assert.assertEquals("u3", users.get(2).getUser().getUsername());
        Assert.assertFalse("u3", users.get(2).isExpert());

        Mockito.verify(userService)
            .getRelatedToSearch(eq("db-e"), eq("area-e"), eq("text"), anyInt(), anyInt());
        Mockito.verify(userService)
            .getRelatedToSearch(eq("db-i"), eq("area-i"), eq("text"), anyInt(), anyInt());
    }

    @Test
    public void testGetRelatedToSearch_onlyExperts() {
        mockProfiles(Collections.emptyList(), Arrays.asList(
            makeUserProfile("USERNAME", "u1", 50),
            makeUserProfile("USERNAME", "u2", 40),
            makeUserProfile("USERNAME", "u3", 30)
        ));

        final List<RelatedUser> users = service.getRelatedToSearch(defaultConfig, "text", 30);
        Assert.assertEquals(3, users.size());
        Assert.assertEquals("u1", users.get(0).getUser().getUsername());
        Assert.assertTrue("u1", users.get(0).isExpert());
        Assert.assertEquals("u2", users.get(1).getUser().getUsername());
        Assert.assertTrue("u2", users.get(1).isExpert());
        Assert.assertEquals("u3", users.get(2).getUser().getUsername());
        Assert.assertTrue("u3", users.get(2).isExpert());

        Mockito.verify(userService)
            .getRelatedToSearch(eq("db-e"), eq("area-e"), eq("text"), anyInt(), anyInt());
        Mockito.verify(userService)
            .getRelatedToSearch(eq("db-i"), eq("area-i"), eq("text"), anyInt(), anyInt());
    }

    @Test
    public void testGetRelatedToSearch_interestedAndExperts() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1", 50),
            makeUserProfile("USERNAME", "u2", 40)
        ), Arrays.asList(
            makeUserProfile("USERNAME", "u3", 55),
            makeUserProfile("USERNAME", "u4", 35)
        ));

        final List<RelatedUser> users = service.getRelatedToSearch(defaultConfig, "text", 30);
        Assert.assertEquals(4, users.size());
        // should reorder based on relevance
        Assert.assertEquals("u3", users.get(0).getUser().getUsername());
        Assert.assertTrue("u3", users.get(0).isExpert());
        Assert.assertEquals("u1", users.get(1).getUser().getUsername());
        Assert.assertFalse("u1", users.get(1).isExpert());
        Assert.assertEquals("u2", users.get(2).getUser().getUsername());
        Assert.assertFalse("u2", users.get(2).isExpert());
        Assert.assertEquals("u4", users.get(3).getUser().getUsername());
        Assert.assertTrue("u4", users.get(3).isExpert());
    }

    @Test
    public void testGetRelatedToSearch_interestedAndExperts_commonUser() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1", 50),
            makeUserProfile("USERNAME", "u2", 40)
        ), Collections.singletonList(
            makeUserProfile("USERNAME", "u2", 55)
        ));

        final List<RelatedUser> users = service.getRelatedToSearch(defaultConfig, "text", 30);
        Assert.assertEquals(2, users.size());
        // should use relevance from experts when reordering
        Assert.assertEquals("u2", users.get(0).getUser().getUsername());
        Assert.assertTrue("u2", users.get(0).isExpert());
        Assert.assertEquals("u1", users.get(1).getUser().getUsername());
        Assert.assertFalse("u1", users.get(1).isExpert());
    }

    @Test
    public void testGetRelatedToSearch_nameField() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1", 50),
            makeUserProfile("NAME", "u2", 40)
        ), Collections.emptyList());

        final List<RelatedUser> users = service.getRelatedToSearch(defaultConfig, "text", 30);
        Assert.assertEquals(2, users.size());
        Assert.assertEquals("u1", users.get(0).getUser().getUsername());
        Assert.assertEquals("should extract the username correctly",
            "u2", users.get(1).getUser().getUsername());
    }

    @Test
    public void testGetRelatedToSearch_noField() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1", 50),
            makeUserProfile("OTHER", "value", 40)
        ), Collections.emptyList());

        final List<RelatedUser> users = service.getRelatedToSearch(defaultConfig, "text", 30);
        Assert.assertEquals("should ignore the profile", 1, users.size());
        Assert.assertEquals("u1", users.get(0).getUser().getUsername());
    }

    @Test
    public void testGetRelatedToSearch_missingUser() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1", 50),
            makeUserProfile("USERNAME", "u2", 40)
        ), Collections.emptyList());

        final User user1 = new User();
        user1.setUsername("u1");
        Mockito.doReturn(Collections.singletonList(user1))
            .when(userService).getUsersDetails(any());

        final List<RelatedUser> users = service.getRelatedToSearch(defaultConfig, "text", 30);
        Assert.assertEquals("should ignore the user", 1, users.size());
        Assert.assertEquals("u1", users.get(0).getUser().getUsername());
    }

    @Test
    public void testGetRelatedToSearch_duplicateUsers() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1", 50),
            makeUserProfile("USERNAME", "u2", 40),
            makeUserProfile("USERNAME", "u1", 30),
            makeUserProfile("USERNAME", "u2", 20),
            makeUserProfile("USERNAME", "u2", 10)
        ), Collections.emptyList());

        final List<RelatedUser> users = service.getRelatedToSearch(defaultConfig, "text", 30);
        Assert.assertEquals("should merge duplicates", 2, users.size());
        Assert.assertEquals("u1", users.get(0).getUser().getUsername());
        Assert.assertEquals("u2", users.get(1).getUser().getUsername());
    }

    @Test
    public void testGetRelatedToSearch_multiplePages() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1", 100),
            makeUserProfile("USERNAME", "u2", 90),
            makeUserProfile("USERNAME", "u1", 80),
            makeUserProfile("USERNAME", "u2", 70),
            makeUserProfile("USERNAME", "u3", 60),
            makeUserProfile("USERNAME", "u2", 50),
            makeUserProfile("USERNAME", "u2", 40)
        ), Collections.emptyList());

        final List<RelatedUser> users = service.getRelatedToSearch(defaultConfig, "text", 4);
        Assert.assertEquals(3, users.size());
        Assert.assertEquals("u1", users.get(0).getUser().getUsername());
        Assert.assertEquals("u2", users.get(1).getUser().getUsername());
        Assert.assertEquals("u3", users.get(2).getUser().getUsername());

        Mockito.verify(userService)
            .getRelatedToSearch(eq("db-e"), eq("area-e"), eq("text"), anyInt(), anyInt());
        Mockito.verify(userService, Mockito.times(2))
            .getRelatedToSearch(eq("db-i"), eq("area-i"), eq("text"), anyInt(), anyInt());
    }

    @Test
    public void testGetRelatedToSearch_multiplePages_fullLastPage() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1", 100),
            makeUserProfile("USERNAME", "u2", 90),
            makeUserProfile("USERNAME", "u1", 80),
            makeUserProfile("USERNAME", "u2", 70),
            makeUserProfile("USERNAME", "u2", 60),
            makeUserProfile("USERNAME", "u1", 50)
        ), Collections.emptyList());

        final List<RelatedUser> users = service.getRelatedToSearch(defaultConfig, "text", 3);
        Assert.assertEquals(2, users.size());
        Assert.assertEquals("u1", users.get(0).getUser().getUsername());
        Assert.assertEquals("u2", users.get(1).getUser().getUsername());

        // should request a 3rd page since the 2nd was full
        Mockito.verify(userService, Mockito.times(3))
            .getRelatedToSearch(eq("db-i"), eq("area-i"), eq("text"), anyInt(), anyInt());
    }

    @Test
    public void testGetRelatedToSearch_enoughUsersBeforeLastPage() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1", 100),
            makeUserProfile("USERNAME", "u2", 90),
            makeUserProfile("USERNAME", "u1", 80),
            makeUserProfile("USERNAME", "u1", 70),
            makeUserProfile("USERNAME", "u3", 60),
            makeUserProfile("USERNAME", "u2", 50),
            makeUserProfile("USERNAME", "u3", 40)
        ), Collections.emptyList());

        final List<RelatedUser> users = service.getRelatedToSearch(defaultConfig, "text", 3);
        Assert.assertEquals(3, users.size());
        Assert.assertEquals("u1", users.get(0).getUser().getUsername());
        Assert.assertEquals("u2", users.get(1).getUser().getUsername());
        Assert.assertEquals("u3", users.get(2).getUser().getUsername());

        // shouldn't get the last page
        Mockito.verify(userService, Mockito.times(2))
            .getRelatedToSearch(eq("db-i"), eq("area-i"), eq("text"), anyInt(), anyInt());
    }

    @Test
    public void testGetRelatedToSearch_extraUsers() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1", 50),
            makeUserProfile("USERNAME", "u1", 40),
            makeUserProfile("USERNAME", "u2", 30),
            makeUserProfile("USERNAME", "u3", 20)
        ), Collections.emptyList());

        final List<RelatedUser> users = service.getRelatedToSearch(defaultConfig, "text", 2);
        Assert.assertEquals("should discard users beyond maxUsers", 2, users.size());
        Assert.assertEquals("u1", users.get(0).getUser().getUsername());
        Assert.assertEquals("u2", users.get(1).getUser().getUsername());

        // shouldn't get the last page
        Mockito.verify(userService, Mockito.times(2))
            .getRelatedToSearch(eq("db-i"), eq("area-i"), eq("text"), anyInt(), anyInt());
    }

    @Test
    public void testGetRelatedToSearch_extraUsersAcrossSources() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1", 50),
            makeUserProfile("USERNAME", "u2", 40)
        ), Arrays.asList(
            makeUserProfile("USERNAME", "u3", 45),
            makeUserProfile("USERNAME", "u4", 35)
        ));

        final List<RelatedUser> users = service.getRelatedToSearch(defaultConfig, "text", 2);
        Assert.assertEquals(
            "should discard users beyond maxUsers after combining", 2, users.size());
        Assert.assertEquals("u1", users.get(0).getUser().getUsername());
        Assert.assertFalse(users.get(0).isExpert());
        Assert.assertEquals("u3", users.get(1).getUser().getUsername());
        Assert.assertTrue(users.get(1).isExpert());
    }

}
