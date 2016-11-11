package com.autonomy.abc.queryHelper;

import com.autonomy.abc.selenium.find.results.ListView;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryService;
import com.autonomy.abc.shared.SharedResult;
import org.openqa.selenium.WebElement;

import java.util.Iterator;

//TODO: assumes you're already on the list view
public class IdolQueryTermResult extends SharedResult {

    public IdolQueryTermResult(final String term, final ListView page){ super(term,page); }

    public WebElement correctedQuery() { return ((ListView)getPage()).correctedQuery(); }

    public boolean errorWellExists() { return ((ListView)getPage()).errorContainerShown(); }

    protected static Iterable<IdolQueryTermResult> idolResultsFor(final Iterable<String> queries, final QueryService service) {
        return new Iterable<IdolQueryTermResult>() {
            @Override
            public Iterator<IdolQueryTermResult> iterator() {
                final Iterator<String> queryIterator = queries.iterator();
                return new Iterator<IdolQueryTermResult>() {
                    @Override
                    public boolean hasNext() {
                        return queryIterator.hasNext();
                    }

                    @Override
                    public IdolQueryTermResult next() {
                        return getTheResult(queryIterator.next(),service);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("remove");
                    }
                };
            }
        };
    }
    static private IdolQueryTermResult getTheResult(final String queryTerm, QueryService service){
        final Query query = new Query(queryTerm)
                .withFilter(new LanguageFilter(Language.ENGLISH));
        //TODO: If BI is on topic map & needs to be on the list
        final ListView page = (ListView) service.search(query);
        return new IdolQueryTermResult(queryTerm, page);
    }
}
