package com.hp.autonomy.frontend.find.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;

@Controller
@RequestMapping("/search/list-indexes")
public class ListIndexesController {

    @Autowired
    private IndexesService indexesService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Index> listIndexes() {
        return indexesService.listIndexes();
    }

}