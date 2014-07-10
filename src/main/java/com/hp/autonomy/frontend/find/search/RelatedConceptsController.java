package com.hp.autonomy.frontend.find.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/search/find-related-concepts")
public class RelatedConceptsController {

    @Autowired
    private RelatedConceptsService relatedConceptsService;

    @RequestMapping(value = "/text/{text}", method = RequestMethod.GET)
    @ResponseBody
    public List<Entity> findRelatedConcepts(
            @PathVariable("text") final String text
    ) {
        return relatedConceptsService.findRelatedConcepts(text);
    }

}
