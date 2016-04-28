package com.hp.autonomy.frontend.find.core.savedsearches;


import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryRepository;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest({
        "application.buildNumber=test",
        "server.port=0",
        "saved-search-test=true"
})
@SpringApplicationConfiguration(classes = SavedSearchTestApplicationConfiguration.class)
public class SavedQueryRepositoryTest {
    @Autowired
    protected UserEntityRepository userEntityRepository;

    @Autowired
    protected SavedQueryRepository savedQueryRepository;

    @Before
    public void setUp() {
        final UserEntity userEntity = new UserEntity();
        userEntity.setUserStore("userStore");
        userEntityRepository.save(userEntity);
    }

    @After
    public void tearDown() {
        savedQueryRepository.deleteAll();
        userEntityRepository.deleteAll();
    }

    @Test
    public void checkValidZeroMinScores() {
        final SavedQuery query = new SavedQuery.Builder()
                .setId(1L)
                .setTitle("All")
                .setDateCreated(DateTime.now())
                .setDateModified(DateTime.now())
                .setQueryText("*")
                .setActive(true)
                .setMinScore(0)
                .build();
        query.setUser(userEntityRepository.findAll().iterator().next());

        final SavedQuery savedQuery = savedQueryRepository.save(query);
        assertThat(savedQuery.getMinScore(), is(0));
    }

    @Test
    public void checkValidMinScore() {
        final SavedQuery query = new SavedQuery.Builder()
                .setId(1L)
                .setTitle("More")
                .setDateCreated(DateTime.now())
                .setDateModified(DateTime.now())
                .setQueryText("Lots of text")
                .setActive(true)
                .setMinScore(45)
                .build();
        query.setUser(userEntityRepository.findAll().iterator().next());

        final SavedQuery savedQuery = savedQueryRepository.save(query);
        assertThat(savedQuery.getMinScore(), is(45));
    }



    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void checkNoMinScore() {
        final SavedQuery query = new SavedQuery.Builder()
                .setId(1L)
                .setTitle("No min score")
                .setDateCreated(DateTime.now())
                .setDateModified(DateTime.now())
                .setQueryText("*")
                .setActive(true)
                .build();
        query.setUser(userEntityRepository.findAll().iterator().next());
        savedQueryRepository.save(query);
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void checkNullMinScore() {
        final SavedQuery query = new SavedQuery.Builder()
                .setId(1L)
                .setTitle("No min score")
                .setDateCreated(DateTime.now())
                .setDateModified(DateTime.now())
                .setQueryText("*")
                .setActive(true)
                .setMinScore(null)
                .build();
        query.setUser(userEntityRepository.findAll().iterator().next());
        savedQueryRepository.save(query);
    }
}
