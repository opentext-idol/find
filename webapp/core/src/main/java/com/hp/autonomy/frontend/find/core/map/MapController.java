package com.hp.autonomy.frontend.find.core.map;

/*
 * $Id:$
 *
 * Copyright (c) 2017, Autonomy Systems Ltd.
 *
 * Last modified by $Author$ on $Date$ 
 */


import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(MapController.MAP_PATH)
public class MapController {
    public static final String MAP_PATH = "/api/public/map";
    public static final String TILE_PATH = "/tile";

    @Autowired
    public MapController(final ConfigService<? extends FindConfig> configService) {
        this.configService = configService;
    }

    private final ConfigService<? extends FindConfig> configService;

    @RequestMapping(value = TILE_PATH, method = RequestMethod.GET)
    public ResponseEntity<byte[]> tile(
            @RequestParam("x") final String x,
            @RequestParam("y") final String y,
            @RequestParam("z") final String z
    ) throws IOException {
        final String tileUrlTemplate = configService.getConfig().getMap().getTileUrlTemplate();
        final String url = tileUrlTemplate.replace("{x}", x).replace("{y}", y).replace("{z}", z);

        final URLConnection urlConnection = new URL(url).openConnection();
        final String contentType = urlConnection.getContentType();
        try (final InputStream is = urlConnection.getInputStream(); final ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
            IOUtils.copyLarge(is, baos);

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(baos.toByteArray());
        }
    }
}