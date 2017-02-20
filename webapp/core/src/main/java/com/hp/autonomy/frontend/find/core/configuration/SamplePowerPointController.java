/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.reports.powerpoint.TemplateSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping({"/api/admin/config", "/api/config/config"})
public class SamplePowerPointController {

    @RequestMapping(value = "/template.pptx", method = RequestMethod.GET)
    public HttpEntity<byte[]> template() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copyLarge(TemplateSource.DEFAULT.getInputStream(), baos);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        headers.set("Content-Disposition", "inline; filename=template.pptx");
        return new HttpEntity<>(baos.toByteArray(), headers);
    }
}
