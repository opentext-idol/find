package com.hp.autonomy.frontend.find.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/search/query-text-index")
public class DocumentsController {

    @Autowired
    private DocumentsService documentsService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Document> query(
        @RequestParam("text") final String text
    ) {
        return documentsService.queryTextIndex(text);
    }

}
