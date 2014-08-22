package com.hp.autonomy.frontend.find.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;

@Controller
public class ListIndexesController {

    @Autowired
    private IndexesService indexesService;

    @RequestMapping(value = "/search/list-indexes", method = RequestMethod.GET)
    @ResponseBody
    public List<Index> listActiveIndexes() {
        return indexesService.listActiveIndexes();
    }

}