package com.autonomy.abc.queryHelper;

import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryService;
import com.autonomy.abc.shared.SharedResult;
import org.openqa.selenium.WebElement;

import java.util.Iterator;

public class IdolQueryTermResult extends SharedResult {

    public IdolQueryTermResult(final String term, final ResultsView page){super(term,page);}

    public WebElement correctedQuery(){return ((ResultsView)getPage()).correctedQuery();}


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
    static private IdolQueryTermResult getTheResult(final String queryTerm, QueryService service ){
        final Query query = new Query(queryTerm)
                .withFilter(new LanguageFilter(Language.ENGLISH));
        final ResultsView page = (ResultsView) service.search(query);
        return new IdolQueryTermResult(queryTerm, page);
    }
}
