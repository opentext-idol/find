/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.indexes;

import com.hp.autonomy.databases.Database;
import com.hp.autonomy.frontend.find.core.indexes.IndexesService;
import com.hp.autonomy.frontend.find.hod.beanconfiguration.HodCondition;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.types.IdolDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@Conditional(HodCondition.class) // TODO remove this
public class ListIndexesController {

    @Autowired
    private IndexesService<HodErrorException> indexesService;

    @RequestMapping(value = "/api/public/search/list-indexes", method = RequestMethod.GET)
    @ResponseBody
    public List<? extends IdolDatabase> listActiveIndexes() throws HodErrorException {
        return indexesService.listVisibleIndexes();
    }
}
