/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.types.requests.idol.actions.query.QuerySummaryElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Controller
@RequestMapping("/api/public/search/find-related-concepts")
public class RelatedConceptsController<Q extends QuerySummaryElement, S extends Serializable, E extends Exception> {

    @Autowired
    private RelatedConceptsService<Q, S, E> relatedConceptsService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Q> findRelatedConcepts(
            @RequestParam("text") final String text,
            @RequestParam("index") final List<S> index,
            @RequestParam("field_text") final String fieldText
    ) throws E {
        return relatedConceptsService.findRelatedConcepts(text, index, fieldText);
    }
}
