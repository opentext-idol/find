package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public abstract class AbstractSavedSearchServiceTest<T extends SavedSearch<T, B>, B extends SavedSearch.Builder<T, B>> {
    private final Supplier<B> builderConstructor;
    @MockitoBean
    private SharedToUserRepository sharedToUserRepository;
    @MockitoBean
    private SharedToEveryoneRepository sharedToEveryoneRepository;
    @SuppressWarnings("unused")
    @MockitoBean
    private AuditorAware<UserEntity> userEntityAuditorAware;
    @SuppressWarnings("unused")
    @MockitoBean
    private TagNameFactory tagNameFactory;
    @Autowired
    protected SavedSearchService<T, B> service;
    @Autowired
    private SavedSearchRepository<T, B> crudRepository;

    protected AbstractSavedSearchServiceTest(final Supplier<B> builderConstructor) {
        this.builderConstructor = builderConstructor;
    }

    @Before
    public void setUp() {
        final UserEntity userEntity = new UserEntity();
        userEntity.setUserId(1L);
        when(userEntityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of(userEntity));

        final Set<T> ownedQueries = new HashSet<>();
        ownedQueries.add(mockSavedSearchResult(1L, true));
        ownedQueries.add(mockSavedSearchResult(2L, true));
        when(crudRepository.findByActiveTrueAndUser_UserId(anyLong())).thenReturn(ownedQueries);

        final Set<SharedToUser> permissions = new HashSet<>();
        permissions.add(mockSharedToUser(false, 1L, 3L));
        permissions.add(mockSharedToUser(false, 1L, 4L));
        permissions.add(mockSharedToUser(true, 1L, 5L));
        when(sharedToUserRepository.findByUserId(anyLong(), any())).thenReturn(permissions);
        when(sharedToEveryoneRepository.findActiveByType(any())).thenReturn(Collections.emptySet());
    }

    private T mockSavedSearchResult(final Long searchId, final Boolean active) {
        return builderConstructor.get()
                .setTitle("Title " + searchId.toString())
                .setId(searchId)
                .setDateCreated(ZonedDateTime.now())
                .setActive(active)
                .setMinScore(0)
                .setCanEdit(true)
                .build();
    }

    private SharedToUser mockSharedToUser(final Boolean canEdit, final Long userId, final Long searchId) {
        return SharedToUser.builder()
                .userId(userId)
                .savedSearch(mockSavedSearchResult(searchId, true))
                .sharedDate(ZonedDateTime.now())
                .modifiedDate(ZonedDateTime.now())
                .canEdit(canEdit)
                .id(new SharedToUserPK(searchId, userId))
                .build();
    }

    @Test
    public void getAll() {
        final Set<T> results = service.getOwned();
        assertThat(results, hasSize(2));

        final Set<T> canEditTrueResults = results.stream()
                .filter(SavedSearch::isCanEdit)
                .collect(Collectors.toSet());

        assertThat(canEditTrueResults, hasSize(2));
    }

    @Test
    public void getShared() {
        final Set<T> results = service.getShared();
        assertThat(results, hasSize(3));

        final Set<T> canEditTrueResults = results.stream()
                .filter(SavedSearch::isCanEdit)
                .collect(Collectors.toSet());

        assertThat(canEditTrueResults, hasSize(1));
    }
}
