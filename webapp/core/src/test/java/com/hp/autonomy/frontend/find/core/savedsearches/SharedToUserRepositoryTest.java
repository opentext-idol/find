package com.hp.autonomy.frontend.find.core.savedsearches;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchRepositoryTestConfiguration.SAVED_SEARCH_REPOSITORY_TEST_PROPERTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SavedSearchRepositoryTestConfiguration.class,
        properties = {"flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop", SAVED_SEARCH_REPOSITORY_TEST_PROPERTY},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("classpath:shared-to-user.xml")
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
                .user(user)
                .canEdit(true)
                .modifiedDate(ZonedDateTime.now())
                .sharedDate(ZonedDateTime.now())
                .build();

        repository.save(sharedToUser);

        assertThat(repository.count(), is(2L));
    }

    @Test
    public void saveMultiple() {
        final SavedQuery savedQuery = savedQueryRepository.findAll().iterator().next();
        final Iterator<UserEntity> userIterator = userEntityRepository.findAll().iterator();
        final UserEntity user = userIterator.next();
        final UserEntity user2 = userIterator.next();

        final Collection<SharedToUser> sharedToUserList = new ArrayList<>();

        sharedToUserList.add(SharedToUser.builder()
                .id(new SharedToUserPK(1L, 1L))
                .savedSearch(savedQuery)
                .user(user)
                .canEdit(true)
                .modifiedDate(ZonedDateTime.now())
                .sharedDate(ZonedDateTime.now())
                .build());

        sharedToUserList.add(SharedToUser.builder()
                .id(new SharedToUserPK(1L, 2L))
                .savedSearch(savedQuery)
                .user(user2)
                .canEdit(true)
                .modifiedDate(ZonedDateTime.now())
                .sharedDate(ZonedDateTime.now())
                .build());

        repository.save(sharedToUserList);

        assertThat(repository.count(), is(3L));
    }

    @Test
    public void findOne() {
        final SharedToUserPK pk = new SharedToUserPK(1L, 3L);
        final SharedToUser sharedToUser = repository.findOne(pk);
        assertThat(sharedToUser.getId(), equalTo(pk));
    }

    @Test
    public void exists() {
        assertThat(repository.exists(new SharedToUserPK(1L, 3L)), is(true));
        assertThat(repository.exists(new SharedToUserPK(2L, 3L)), is(false));
    }

    @Test
    public void findAll() {
        assertThat(repository.findAll().iterator().hasNext(), is(true));
    }

    @Test
    public void findAllWithIds() {
        final SharedToUserPK pk = new SharedToUserPK(1L, 3L);
        assertThat(repository.findAll(Collections.singletonList(pk)).iterator().hasNext(), is(true));
        final SharedToUserPK pkNoResults = new SharedToUserPK(1L, 2L);
        assertThat(repository.findAll(Collections.singletonList(pkNoResults)).iterator().hasNext(), is(false));
    }

    @Test
    public void count() {
        assertThat(repository.count(), is(1L));
    }

    @Test
    public void deleteWithId() {
        assertThat(repository.count(), is(1L));
        repository.delete(new SharedToUserPK(1L, 3L));
        assertThat(repository.count(), is(0L));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void deleteWithWrongId() {
        assertThat(repository.count(), is(1L));
        repository.delete(new SharedToUserPK(1L, 2L));
    }

    @Test
    public void deleteWithObject() {
        assertThat(repository.count(), is(1L));

        final SavedQuery savedQuery = savedQueryRepository.findAll().iterator().next();
        final UserEntity user = userEntityRepository.findAll().iterator().next();
        final SharedToUser sharedToUser = SharedToUser.builder()
                .id(new SharedToUserPK(1L, 3L))
                .savedSearch(savedQuery)
                .user(user)
                .canEdit(true)
                .modifiedDate(ZonedDateTime.now())
                .sharedDate(ZonedDateTime.now())
                .build();
        repository.delete(sharedToUser);
        assertThat(repository.count(), is(0L));
    }

    @Test
    public void deleteAll() {
        final SavedQuery savedQuery = savedQueryRepository.findAll().iterator().next();
        final UserEntity user = userEntityRepository.findAll().iterator().next();

        final SharedToUser sharedToUser = SharedToUser.builder()
                .id(new SharedToUserPK(2L, 2L))
                .savedSearch(savedQuery)
                .user(user)
                .canEdit(true)
                .modifiedDate(ZonedDateTime.now())
                .sharedDate(ZonedDateTime.now())
                .build();

        repository.save(sharedToUser);
        assertThat(repository.count(), is(2L));

        repository.deleteAll();
        assertThat(repository.count(), is(0L));
    }
}
