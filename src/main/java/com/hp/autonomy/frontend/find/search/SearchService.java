package com.hp.autonomy.frontend.find.search;

import java.util.List;

public interface SearchService {

    public List<Entity> findRelatedConcepts(final String text);

}
