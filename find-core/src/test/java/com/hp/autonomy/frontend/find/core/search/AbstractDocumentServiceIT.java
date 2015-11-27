package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.types.requests.Documents;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class AbstractDocumentServiceIT<S extends Serializable, D extends FindDocument, E extends Exception> extends AbstractFindIT {
    @Autowired
    protected DocumentsController<S, D, E> documentsController;

    protected final List<S> indexes;

    public AbstractDocumentServiceIT(final List<S> indexes) {
        this.indexes = new ArrayList<>(indexes);
    }

    @Test
    public void query() throws E {
        final Documents<D> documents = documentsController.query("*", 50, null, indexes, null, null, null, null);
        assertThat(documents.getDocuments(), is(not(empty())));
    }

    @Test
    public void queryForPromotions() throws E {
        final Documents<D> documents = documentsController.queryForPromotions("*", 50, null, indexes, null, null, null, null);
        assertThat(documents.getDocuments(), is(empty())); // TODO: configure this later
    }

    @Test
    public void findSimilar() throws E {
        final Documents<D> documents = documentsController.query("*", 50, null, indexes, null, null, null, null);
        final List<D> results = documentsController.findSimilar(documents.getDocuments().get(0).getReference(), new HashSet<>(indexes));
        assertThat(results, is(not(empty())));
    }
}
