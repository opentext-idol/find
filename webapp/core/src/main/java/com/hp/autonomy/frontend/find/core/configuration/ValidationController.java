/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.Config;
import com.hp.autonomy.frontend.configuration.validation.ValidationResults;
import com.hp.autonomy.frontend.configuration.validation.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({"/api/admin/config", "/api/config/config"})
public abstract class ValidationController<C extends Config<C>> {

    @Autowired
    private ValidationService<C> validationService;

    @RequestMapping(value = "/config-validation", method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseBody
    public ValidationResults validConfig(@RequestBody final C config) {
        return validationService.validateConfig(config);
    }
}
