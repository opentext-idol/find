package com.hp.autonomy.frontend.find.core.map;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.api.MapApi;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

@Slf4j
@RestController
class MapController implements MapApi {
    private static final Pattern CALLBACK_FUNCTION_NAME_PATTERN = Pattern.compile("\\w+");

    private final ConfigService<? extends FindConfig<?, ?>> configService;

    @Autowired
    MapController(@SuppressWarnings("SpringJavaAutowiringInspection") final ConfigService<? extends FindConfig<?, ?>> configService) {
        this.configService = configService;
    }

    @SuppressWarnings("OverlyBroadCatchBlock")
    @Override
    public ResponseEntity<String> proxyMapTile(final String url, final String callback) {
        if (!CALLBACK_FUNCTION_NAME_PATTERN.matcher(callback).matches()) {
            throw new IllegalArgumentException("Invalid callback function name");
        }

        try {
            final String tileUrlTemplate = configService.getConfig().getMap().getTileUrlTemplate();

            final URL targetURL = new URL(url);
            final URL validationURL = new URL(tileUrlTemplate);

            if (!validationURL.getProtocol().equals(targetURL.getProtocol()) || !validationURL.getHost().equals(targetURL.getHost()) || validationURL.getPort() != targetURL.getPort()) {
                throw new IllegalArgumentException("We only allow proxying to the tile server");
            }

            final URLConnection urlConnection = targetURL.openConnection();
            final String contentType = urlConnection.getContentType();
            return ResponseEntity.ok(getMapData(callback, urlConnection, contentType));
        } catch (final IOException e) {
            log.error("Error retrieving map data", e);
            return ResponseEntity.ok(callback + "(\"error:Application error\")");
        }
    }

    private String getMapData(final String callback, final URLConnection urlConnection, final String contentType) throws IOException {
        try (final InputStream inputStream = urlConnection.getInputStream(); final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            IOUtils.copyLarge(inputStream, outputStream);
            return callback + "(\"data:" + contentType + ";base64," + new String(Base64.encodeBase64(outputStream.toByteArray(), false, false)) + "\")";
        }
    }
}
