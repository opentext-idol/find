package com.hp.autonomy.frontend.find.core.savedsearches;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.hp.autonomy.frontend.find.core.DbTestConfiguration;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryRepository;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.time.ZonedDateTime;
import java.util.*;

import static com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchRepositoryTestConfiguration.SAVED_SEARCH_REPOSITORY_TEST_PROPERTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SavedSearchRepositoryTestConfiguration.class,
        properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop", SAVED_SEARCH_REPOSITORY_TEST_PROPERTY},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(DbTestConfiguration.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DbUnitConfiguration(databaseConnection = "unitTestConn")
@DatabaseSetup(value = "classpath:shared-to-user.xml", connection = "unitTestConn")
@DirtiesContext
public class SharedToUserRepositoryTest {
    @Autowired
    private SharedToUserRepository repository;
    @Autowired
    private SavedQueryRepository savedQueryRepository;
    @Autowired
    private UserEntityRepository userEntityRepository;

    @Test
    public void save() {
        final SavedQuery savedQuery = savedQueryRepository.findAll().iterator().next();
        final UserEntity user = userEntityRepository.findAll().iterator().next();

        final SharedToUser sharedToUser = SharedToUser.builder()
                .id(new SharedToUserPK())
                .savedSearch(savedQuery)
                .userId(user.getUserId())
                .canEdit(true)
                .modifiedDate(ZonedDateTime.now())
                .sharedDate(ZonedDateTime.now())
                .build();

        final Long initialCount = repository.count();
        repository.save(sharedToUser);

        assertThat(repository.count(), is(initialCount + 1));
    }

    @Test
    public void saveMultiple() {
        final Iterator<SavedQuery> queryIterator = savedQueryRepository.findAll().iterator();
        final SavedQuery savedQuery = queryIterator.next();
        final SavedQuery savedQuery2 = queryIterator.next();
        final UserEntity user = userEntityRepository.findAll().iterator().next();
        final Long initialCount = repository.count();

        final Collection<SharedToUser> sharedToUserList = new ArrayList<>();

        sharedToUserList.add(SharedToUser.builder()
                .id(new SharedToUserPK())
                .savedSearch(savedQuery)
                .userId(user.getUserId())
                .canEdit(true)
                .modifiedDate(ZonedDateTime.now())
                .sharedDate(ZonedDateTime.now())
                .build());

        sharedToUserList.add(SharedToUser.builder()
                .id(new SharedToUserPK())
                .savedSearch(savedQuery2)
                .userId(user.getUserId())
                .canEdit(true)
                .modifiedDate(ZonedDateTime.now())
                .sharedDate(ZonedDateTime.now())
                .build());

        repository.saveAll(sharedToUserList);

        assertThat(repository.count(), is(initialCount + 2));
    }

    @Test
    public void findOne() {
        final SharedToUserPK pk = new SharedToUserPK(1L, 3L);
        final SharedToUser sharedToUser = repository.findById(pk).get();
        assertThat(sharedToUser.getId(), equalTo(pk));
        assertThat(sharedToUser.getSavedSearch(), notNullValue());
        assertThat(sharedToUser.getUser(), notNullValue());
        assertThat(sharedToUser.getCanEdit(), is(true));
        //TODO: fix this
//        assertThat(sharedToUser.getModifiedDate(), notNullValue());
//        assertThat(sharedToUser.getSharedDate(), notNullValue());
    }

    @Test
    public void exists() {
        assertThat(repository.existsById(new SharedToUserPK(1L, 3L)), is(true));
        assertThat(repository.existsById(new SharedToUserPK(2L, 3L)), is(false));
    }

    @Test
    public void findAll() {
        assertThat(repository.findAll().iterator().hasNext(), is(true));
    }

    @Test
    public void findAllWithIds() {
        final SharedToUserPK pk = new SharedToUserPK(1L, 3L);
        assertThat(repository.findAllById(Collections.singletonList(pk)).iterator().hasNext(), is(true));
        final SharedToUserPK pkNoResults = new SharedToUserPK(1L, 1L);
        assertThat(repository.findAllById(Collections.singletonList(pkNoResults)).iterator().hasNext(), is(false));
    }

    @Test
    public void deleteWithId() {
        final Long initialCount = repository.count();
        repository.deleteById(new SharedToUserPK(1L, 3L));
        assertThat(repository.count(), is(initialCount - 1));
    }

    public void deleteWithWrongId() {
        // silently ignored
        repository.deleteById(new SharedToUserPK(1L, 1L));
    }

    @Test
    public void deleteWithObject() {
        final Long initialCount = repository.count();
        repository.delete(repository.findById(new SharedToUserPK(1L, 3L)).get());
        assertThat(repository.count(), is(initialCount - 1));
    }

    @Test
    public void deleteAll() {
        final SavedQuery savedQuery = savedQueryRepository.findAll().iterator().next();
        final UserEntity user = userEntityRepository.findAll().iterator().next();

        final SharedToUser sharedToUser = SharedToUser.builder()
                .id(new SharedToUserPK())
                .savedSearch(savedQuery)
                .userId(user.getUserId())
                .canEdit(true)
                .modifiedDate(ZonedDateTime.now())
                .sharedDate(ZonedDateTime.now())
                .build();

        repository.save(sharedToUser);
        assertThat(repository.count(), is(greaterThan(0L)));

        repository.deleteAll();
        assertThat(repository.count(), is(0L));
    }

    @Test
    public void findPermittedSavedQueriesByUserId() {
        final Set<SharedToUser> searchesSharedWithNamedUser = repository.findByUserId(4L, SavedQuery.class);
        assertThat(searchesSharedWithNamedUser.size(), is(1));
    }

    @Test
    public void findPermittedSavedSnapshotByUserId() {
        final Set<SharedToUser> searchesSharedWithNamedUser = repository.findByUserId(4L, SavedSnapshot.class);
        assertThat(searchesSharedWithNamedUser.size(), is(1));
    }

    @Test
    public void findPermittedUsersBySearchId() {
        final Set<SharedToUser> usersAllowedToSeeNamedSearch = repository.findBySavedSearch_Id(1L);
        assertThat(usersAllowedToSeeNamedSearch.size(), is(3));
    }
}
