package com.autonomy.abc.selenium.query;

public interface QueryService<T extends QueryResultsPage> {
    T search(String term);
    T search(Query query);
}
