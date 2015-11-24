/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.indexes;

import com.hp.autonomy.types.IdolDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ListIndexesController {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private IndexesService<?, ?> indexesService;

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @RequestMapping(value = "/api/public/search/list-indexes", method = RequestMethod.GET)
    @ResponseBody
    public List<? extends IdolDatabase> listActiveIndexes() throws Exception {
        return indexesService.listVisibleIndexes();
    }
}
