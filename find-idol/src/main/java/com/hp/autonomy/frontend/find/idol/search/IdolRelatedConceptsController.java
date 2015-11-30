/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.RelatedConceptsController;
import com.hp.autonomy.types.idol.QsElement;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/public/search/find-related-concepts")
public class IdolRelatedConceptsController extends RelatedConceptsController<QsElement, String, AciErrorException> {
}
