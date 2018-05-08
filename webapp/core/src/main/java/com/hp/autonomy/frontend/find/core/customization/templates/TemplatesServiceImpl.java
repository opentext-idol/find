/*
 * Copyright 2015-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.customization.templates;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.find.core.configuration.CustomizationConfigService;
import com.hp.autonomy.frontend.find.core.configuration.TemplatesConfig;
import com.hp.autonomy.frontend.find.core.customization.ReloadableCustomizationComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hp.autonomy.frontend.find.core.configuration.FindConfigFileService.CONFIG_FILE_LOCATION_SPEL;

@Service
class TemplatesServiceImpl implements TemplatesService, ReloadableCustomizationComponent {
    static final String DIRECTORY_NAME = "templates";

    private final AtomicReference<Templates> cachedTemplates = new AtomicReference<>(null);

    private final CustomizationConfigService<TemplatesConfig> configService;
    private final Path directoryPath;

    public TemplatesServiceImpl(
            final CustomizationConfigService<TemplatesConfig> configService,
            @Value(CONFIG_FILE_LOCATION_SPEL) final String homeDirectory
    ) {
        this.configService = configService;

        final Path homePath = Paths.get(homeDirectory);
        directoryPath = homePath.resolve(CustomizationConfigService.CONFIG_DIRECTORY + File.separator + DIRECTORY_NAME);
    }

    @PostConstruct
    public void createDirectoryAndPopulateCache() throws IOException, TemplateNotFoundException {
        Files.createDirectories(directoryPath);
        loadTemplates();
    }

    @Override
    public void loadTemplates() throws IOException, TemplateNotFoundException {
        final Map<String, Path> directoryMap;

        // Use try-with-resources to ensure the stream is closed promptly
        try (final Stream<Path> stream = Files.list(directoryPath)) {
            directoryMap = stream.filter(path -> !path.toFile().isDirectory())
                    .collect(Collectors.toMap(
                            path -> path.getFileName().toString(),
                            Function.identity()
                    ));
        }

        final TemplatesConfig config = configService.getConfig();
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        for (final String fileName : config.listTemplateFiles()) {
            final Path path = Optional.ofNullable(directoryMap.get(fileName))
                    .orElseThrow(() -> new TemplateNotFoundException(fileName, directoryPath.toString()));

            final String template = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            builder.put(fileName, template);
        }

        final Templates output = Templates.builder()
                .templates(builder.build())
                .lastModified(Instant.now())
                .build();

        cachedTemplates.set(output);
    }

    @Override
    public Templates getTemplates() {
        return Optional.ofNullable(cachedTemplates.get())
                .orElseThrow(() -> new IllegalStateException("Templates service not initialised"));
    }

    @Override
    public void reload() throws Exception {
        configService.init();
        loadTemplates();
    }
}
