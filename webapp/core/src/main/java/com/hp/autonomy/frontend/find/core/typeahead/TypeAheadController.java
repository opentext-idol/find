/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.typeahead;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.searchcomponents.core.typeahead.TypeAheadService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class TypeAheadController<E extends Exception> {
    public static final String URL = "/api/public/typeahead";
    static final String TEXT_PARAMETER = "text";

    private final TypeAheadService<E> typeAheadService;
    private final ConfigService<? extends FindConfig<?, ?>> configService;
    private final List<String> customTerms;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public TypeAheadController(
        final TypeAheadService<E> typeAheadService,
        final ConfigService<? extends FindConfig<?, ?>> configService
    ) throws IOException {
        this.typeAheadService = typeAheadService;
        this.configService = configService;

        customTerms = loadCustomTerms();
    }

    private List<String> loadCustomTerms() throws IOException {
        final String path = configService.getConfig().getUiCustomization().getCustomTypeaheadPath();
        if (path == null) {
            return new ArrayList<>();
        } else {
            try (final InputStream customTermsStream = Files.newInputStream(Paths.get(path))) {
                 return IOUtils.readLines(customTermsStream, StandardCharsets.UTF_8).stream()
                     .filter(term -> !term.isEmpty())
                     .collect(Collectors.toList());
            }
        }
    }

    @RequestMapping(URL)
    public List<String> getSuggestions(@RequestParam(TEXT_PARAMETER) final String text) throws E {
        final List<String> suggestions = new ArrayList<>(getCustomSuggestions(text));
        final Set<String> existing = new HashSet<>(suggestions);
        suggestions.addAll(typeAheadService.getSuggestions(text).stream()
            .filter(term -> !existing.contains(term))
            .collect(Collectors.toList()));
        return suggestions;
    }

    private List<String> getCustomSuggestions(final String text) {
        final Pattern textPattern = Pattern.compile("\\b" + Pattern.quote(text.toLowerCase()));
        return customTerms.stream()
            .filter(term -> textPattern.matcher(term).find())
            .collect(Collectors.toList());
    }

}
