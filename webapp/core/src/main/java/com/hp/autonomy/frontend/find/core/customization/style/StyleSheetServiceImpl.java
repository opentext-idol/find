/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.customization.style;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.beanconfiguration.AppConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.style.StyleConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StyleSheetServiceImpl implements StyleSheetService {
    // LESS files which give rise to CSS files directly requested by the
    // browser have to be included here
    private static final Map<String, String> LESS_TO_CSS_FILE_MAP = ImmutableMap.<String, String>builder()
        .put("login.less", "login")
        .put("app.less", "app")
        .build();

    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("\\.css");

    private final AtomicReference<Pair<Instant, Map<String, String>>> cssCache = new AtomicReference<>(null);

    // Provides configured custom LESS variables
    private final ConfigService<StyleConfiguration> configService;

    @Value(AppConfiguration.GIT_COMMIT_PROPERTY)
    private String gitCommitHash;

    @Autowired
    public StyleSheetServiceImpl(final ConfigService<StyleConfiguration> configService) {
        this.configService = configService;
    }

    @Override
    @PostConstruct
    public void generateCss() throws CssGenerationException {
        final Map<String, String> extraVariables = lessVariables();
        final StyleCompiler styleCompiler = new Less4jStyleCompiler(extraVariables);

        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        for(final Map.Entry<String, String> entry : LESS_TO_CSS_FILE_MAP.entrySet()) {
            final Path path = new InMemoryPath("less/" + entry.getKey());
            final String css = styleCompiler.compile(path);

            builder.put(entry.getValue(), css);
        }

        cssCache.set(Pair.of(Instant.now(), builder.build()));
    }

    @Override
    @SuppressWarnings("WeakerAccess")
    public Optional<StyleSheet> getCss(final String fileName) {
        final Pair<Instant, Map<String, String>> pair = Optional.ofNullable(cssCache.get())
            .orElseThrow(() -> new IllegalStateException("CSS not loaded"));

        final String fileKey = FILE_EXTENSION_PATTERN.split(fileName)[0];

        return Optional.ofNullable(pair.getRight().get(fileKey)).map(css -> StyleSheet.builder()
            .lastModified(pair.getLeft())
            .styleSheet(css)
            .build());
    }

    // LESS variables used to customize the stylesheet, including git commit hash
    private Map<String, String> lessVariables() {
        final Map<String, String> variables = new HashMap<>();
        // path to ./static-COMMIT directory relative to the StyleController.getCss() endpoint
        variables.put("staticResourcesPath", "'../../static-" + gitCommitHash + '\'');
        // path to ./static directory inside the core .jar file relative to the ./less directory
        variables.put("compilationResourcesPath", "'../static'");
        // Path for making public API calls (logo customization, etc.)
        variables.put("publicApiPath", "'../../api/public'");
        // Query term highlight colour and background
        variables.put("termHighlightColor", configService.getConfig().getTermHighlightColor());
        variables.put("termHighlightBackground", configService.getConfig().getTermHighlightBackground());

        final Map<String, String> customVariables = configService.getConfig().getSimpleVariables();
        if(customVariables != null) {
            final Map<String, String> mappedVariables = customVariables.entrySet()
                .stream()
                .collect(
                    Collectors.toMap(
                        e -> "USER_CUSTOM_" + e.getKey(),
                        Map.Entry::getValue
                    )
                );
            variables.putAll(mappedVariables);
        }
        return variables;
    }
}
