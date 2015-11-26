package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.types.Identifier;
import com.hp.autonomy.types.requests.idol.actions.query.QuerySummaryElement;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class AbstractRelatedConceptsServiceIT<Q extends QuerySummaryElement, I extends Identifier, E extends Exception> extends AbstractFindIT {
    @Autowired
    protected RelatedConceptsController<Q, I, E> relatedConceptsController;

    protected final List<I> indexes;

    public AbstractRelatedConceptsServiceIT(final List<I> indexes) {
        this.indexes = new ArrayList<>(indexes);
    }

    @Test
    public void findRelatedConcepts() throws E {
        final List<Q> results = relatedConceptsController.findRelatedConcepts("*", indexes, "");
        assertThat(results, is(not(empty())));
    }
}
