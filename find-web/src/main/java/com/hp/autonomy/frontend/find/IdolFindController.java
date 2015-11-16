/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find;

import com.hp.autonomy.frontend.find.beanconfiguration.IdolCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Conditional(IdolCondition.class)
public class IdolFindController extends FindController {

    @RequestMapping("/api/config/test")
    @ResponseBody
    public String foo() {
        return "It works!";
    }

}
