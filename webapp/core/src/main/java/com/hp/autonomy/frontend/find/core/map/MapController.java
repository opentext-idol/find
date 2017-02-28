/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.map;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(MapController.MAP_PATH)
public class MapController {
    public static final String MAP_PATH = "/api/public/map";
    public static final String PROXY_PATH = "/proxy";

    @Autowired
    public MapController(final ConfigService<? extends FindConfig> configService) {
        this.configService = configService;
    }

    private final ConfigService<? extends FindConfig> configService;

    @RequestMapping(value = PROXY_PATH, method = RequestMethod.GET)
    @ResponseBody
    public String tile(
        @RequestParam("url") final String url,
        @RequestParam("callback") final String callback
    ) {
        if(!callback.matches("\\w+")) {
            throw new IllegalArgumentException("Invalid callback function name");
        }

        try {
            final String tileUrlTemplate = configService.getConfig().getMap().getTileUrlTemplate();

            final URL target = new URL(url), validate = new URL(tileUrlTemplate);

            if (!validate.getProtocol().equals(target.getProtocol()) || !validate.getHost().equals(target.getHost()) || validate.getPort() != target.getPort()) {
                throw new IllegalArgumentException("We only allow proxying to the tile server");
            }

            final URLConnection urlConnection = target.openConnection();
            final String contentType = urlConnection.getContentType();
            try (final InputStream is = urlConnection.getInputStream(); final ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
                IOUtils.copyLarge(is, baos);

                return callback + "(\"data:" + contentType + ";base64," + new String(Base64.encodeBase64(baos.toByteArray(), false, false)) + "\")";
            }
        }
        catch(IOException e) {
            return callback + "(\"error:Application error\")";
        }
    }
}