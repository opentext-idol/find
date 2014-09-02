package com.hp.autonomy.frontend.find.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/api/search/find-related-concepts")
public class RelatedConceptsController {

    @Autowired
    private RelatedConceptsService relatedConceptsService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Entity> findRelatedConcepts(
            @RequestParam("text") final String text,
            @RequestParam(value = "index") final String index
    ) {
        return relatedConceptsService.findRelatedConcepts(text, index);
    }

}
