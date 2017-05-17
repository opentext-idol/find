/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.customization;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Less4jStyleCompiler implements StyleCompiler {
    private final DefaultLessCompiler compiler;
    private final LessCompiler.Configuration config;

    public Less4jStyleCompiler(final Map<String, String> extraVariables) {
        this(extraVariables, new DefaultLessCompiler(),
             new LessCompiler.Configuration());
    }

    // Takes a map of less variable names (without '@') to their values
    Less4jStyleCompiler(final Map<String, String> extraVariables,
                        final DefaultLessCompiler compiler,
                        final LessCompiler.Configuration config) {
        this.compiler = compiler;
        this.config = config;
        config.setCompressing(true);

        config.addExternalVariables(extraVariables.entrySet().stream().collect(
            Collectors.toMap(
                e -> '@' + e.getKey(),
                Map.Entry::getValue
            )
        ));
    }

    @Override
    public String compile(final Path path) throws CssGenerationException {
        final LessResource source = new LessResource(path);
        log.info("Generating CSS from root file: " + path);
        try {
            return compiler.compile(source, config).getCss();
        } catch(final Less4jException e) {
            throw new CssGenerationException("Failed to generate CSS from " + path, e);
        }
    }
}
