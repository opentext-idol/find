/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.customization;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.find.core.beanconfiguration.AppConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.CustomizationConfigService;
import com.hp.autonomy.frontend.find.core.configuration.style.StyleConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StyleSheetService implements ReloadableCustomizationComponent {
    // LESS files which give rise to CSS files directly requested by the
    // browser have to be included here
    private static final Map<String, String> LESS_TO_CSS_FILE_MAP = ImmutableMap.<String, String>builder()
        .put("login.less", "login")
        .put("app.less", "app")
        .build();
    private final Map<String, String> fileNameToCssMap = new ConcurrentHashMap<>();

    // Provides configured custom LESS variables
    private final CustomizationConfigService<StyleConfiguration> configService;

    @Value(AppConfiguration.GIT_COMMIT_PROPERTY)
    private String gitCommitHash;

    @Autowired
    public StyleSheetService(final CustomizationConfigService<StyleConfiguration> configService) {
        this.configService = configService;
    }

    @PostConstruct
    private void generateCss() throws CssGenerationException {
        final Map<String, String> extraVariables = lessVariables();

        final StyleCompiler styleCompiler = new Less4jStyleCompiler(extraVariables);
        for(final String sourcePath : LESS_TO_CSS_FILE_MAP.keySet()) {
            final Path path = new InMemoryPath("less/" + sourcePath);
            final String css = styleCompiler.compile(path);

            this.fileNameToCssMap.put(LESS_TO_CSS_FILE_MAP.get(sourcePath), css);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public Optional<String> getCss(final String fileName) {
        final Optional<String> maybeCss = Optional.ofNullable(this.fileNameToCssMap.get(fileName));

        return maybeCss.isPresent()
            ? maybeCss
            : Optional.ofNullable(this.fileNameToCssMap.get(fileName.split("\\.css")[0]));
    }

    // LESS variables used to customize the stylesheet, including git commit hash
    private Map<String, String> lessVariables() {
        final Map<String, String> variables = new HashMap<>();
        // path to ./static-COMMIT directory relative to the StyleController.getCss() endpoint
        variables.put("staticResourcesPath", "'../../static-" + gitCommitHash + "'");
        // path to ./static directory inside the core .jar file relative to the ./less directory
        variables.put("compilationResourcesPath", "'../static'");

        variables.putAll(configService.getConfig().getSimpleVariables().entrySet()
                             .stream()
                             .collect(
                                 Collectors.toMap(
                                     e -> "USER_CUSTOM_" + e.getKey(),
                                     Map.Entry::getValue
                                 )
                             ));
        return variables;
    }

    @Override
    public void reload() throws Exception {
        configService.init();
        generateCss();
    }
}
