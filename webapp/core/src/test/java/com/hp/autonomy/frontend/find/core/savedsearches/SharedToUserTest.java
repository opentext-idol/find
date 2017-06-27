package com.hp.autonomy.frontend.find.core.savedsearches;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
public class SharedToUserTest {
    private final String firstDateTime = "2007-12-03T10:15:30+01:00[Europe/Paris]";
    private SharedToUser firstUser;

    @Before
    public void setUp() {
        firstUser = SharedToUser.builder()
                .id(new SharedToUserPK(1L, 1L))
                .searchId(1L)
                .userId(1L)
                .canEdit(true)
                .modifiedDate(ZonedDateTime.parse(firstDateTime))
                .sharedDate(ZonedDateTime.parse(firstDateTime))
                .build();

    }

    @Test
    public void merge() {
        final String secondDateTime = "2020-12-03T10:15:30+01:00[Europe/Paris]";
        final SharedToUser secondUser = SharedToUser.builder()
                .id(new SharedToUserPK(2L, 2L))
                .searchId(2L)
                .userId(2L)
                .canEdit(false)
                .modifiedDate(ZonedDateTime.parse(secondDateTime))
                .sharedDate(ZonedDateTime.parse(secondDateTime))
                .build();

        secondUser.merge(firstUser);
        assertThat(secondUser.getId().getSearchId(), is(2L));
        assertThat(secondUser.getId().getUserId(), is(2L));
        assertThat(secondUser.getCanEdit(), is(false));
        assertThat(secondUser.getModifiedDate(), is(ZonedDateTime.parse(secondDateTime)));
        assertThat(secondUser.getSharedDate(), is(ZonedDateTime.parse(firstDateTime)));
    }

    @Test
    public void mergeWithNull() {
        firstUser.merge(null);
        assertThat(firstUser.getId().getSearchId(), is(1L));
        assertThat(firstUser.getId().getUserId(), is(1L));
        assertThat(firstUser.getCanEdit(), is(true));
        assertThat(firstUser.getModifiedDate(), is(ZonedDateTime.parse(firstDateTime)));
        assertThat(firstUser.getSharedDate(), is(ZonedDateTime.parse(firstDateTime)));
    }
}
