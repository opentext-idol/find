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

import com.github.sommeri.less4j.LessSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class LessResource extends LessSource {
    private final Path path;
    private final AtomicReference<String> contentCache = new AtomicReference<>(null);

    @SuppressWarnings("WeakerAccess")
    public LessResource(final Path path) {
        this.path = path.normalize();
    }

    @Override
    public LessResource relativeSource(final String filename) throws LessSource.FileNotFound,
                                                                     LessSource.CannotReadFile,
                                                                     LessSource.StringSourceException {
        final Path relativePath = path.getParent().resolve(filename);
        return new LessResource(relativePath);
    }

    @Override
    public String getContent() throws LessSource.FileNotFound, LessSource.CannotReadFile {
        return getAndCacheContent();
    }

    private String getAndCacheContent() throws CannotReadFile {
        final String pathString = path.toString();
        final String cachedContent = contentCache.get();

        if(cachedContent == null) {
            log.debug("Attempt to get resource: " + pathString);
            try(final InputStream resource = getClass().getClassLoader().getResourceAsStream(pathString)) {
                if(resource == null) {
                    log.error("Resource not found or not a resource: " + pathString);
                    throw new CannotReadFile();
                } else {
                    final String content = IOUtils.toString(resource, StandardCharsets.UTF_8);
                    contentCache.set(content);
                    log.debug("Successfully read " + pathString);
                    return content;
                }
            } catch(final IOException e) {
                log.error("Failed to open resource: " + pathString, e);
                throw new CannotReadFile();
            }
        } else {
            log.debug("Get cached resource for " + pathString);
            return cachedContent;
        }
    }

    @Override
    public byte[] getBytes() throws LessSource.FileNotFound, LessSource.CannotReadFile {
        return getAndCacheContent().getBytes();
    }
}
