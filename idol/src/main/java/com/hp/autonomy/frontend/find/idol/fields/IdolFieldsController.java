/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.fields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class IdolFieldsController extends FieldsController<IdolFieldsRequest, AciErrorException> {
    @Autowired
    public IdolFieldsController(final FieldsService<IdolFieldsRequest, AciErrorException> fieldsService) {
        super(fieldsService);
    }
}
