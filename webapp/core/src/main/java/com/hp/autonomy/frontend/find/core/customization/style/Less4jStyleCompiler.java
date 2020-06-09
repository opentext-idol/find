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

package com.hp.autonomy.frontend.find.core.customization.style;

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
