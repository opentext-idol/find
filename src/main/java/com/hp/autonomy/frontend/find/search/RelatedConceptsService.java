package com.hp.autonomy.frontend.find.search;

import java.util.List;

public interface RelatedConceptsService {

    public List<Entity> findRelatedConcepts(String text);

}
