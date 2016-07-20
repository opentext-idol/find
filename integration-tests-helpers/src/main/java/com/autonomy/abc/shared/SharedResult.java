package com.autonomy.abc.shared;

import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryResultsPage;
import com.autonomy.abc.selenium.query.QueryService;
import org.openqa.selenium.WebElement;

import java.util.Iterator;

public class SharedResult<T extends QueryResultsPage> {
    final String term;
    final T page;
    private String text;

    public SharedResult(final String term, final T page) {
        this.term = term;
        this.page = page;
    }

    public String getErrorMessage() {
        if (text == null) {
            text = errorContainer().getText();
        }
        return text;
    }

    public T getPage(){return this.page;}

    public WebElement errorContainer() {
        return page.errorContainer();
    }


    protected static Iterable<SharedResult> resultsFor(final Iterable<String> queries, final QueryService service) {
        return new Iterable<SharedResult>() {
            @Override
            public Iterator<SharedResult> iterator() {
                final Iterator<String> queryIterator = queries.iterator();
                return new Iterator<SharedResult>() {
                    @Override
                    public boolean hasNext() {
                        return queryIterator.hasNext();
                    }

                    @Override
                    public SharedResult next() {
                        return resultFor(queryIterator.next(),service);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("remove");
                    }
                };
            }
        };
    }

    static private SharedResult resultFor(final String queryTerm, QueryService service) {
        final Query query = new Query(queryTerm)
                .withFilter(new LanguageFilter(Language.ENGLISH));
        final QueryResultsPage page = service.search(query);
        return new SharedResult(queryTerm, page);
    }
}
