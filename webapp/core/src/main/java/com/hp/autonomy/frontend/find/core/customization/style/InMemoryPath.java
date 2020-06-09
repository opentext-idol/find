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

import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Partial implementation of a relative, String-based path for use with
 * {@link LessResource}. Allows for compilation of LESS resources
 * that reside both on disk or within .war file, with proper handling of LESS imports fetched using relative URLs.
 */
public class InMemoryPath implements Path {
    private final List<String> pathElements;
    private final String sep = "/";

    private InMemoryPath(final List<String> pathElements) {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(pathElements);
        this.pathElements = list;
    }

    public InMemoryPath(final String relativePath) {
        final String[] split = relativePath.split(sep);
        pathElements = Arrays.stream(split).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private boolean isEmpty() {
        final List<String> normal = normalizeModel(pathElements);
        return normal.isEmpty();
    }

    private List<String> normalizeModel(final List<String> currentList) {
        final List<String> newList = new ArrayList<>();
        for(final String next : currentList) {
            if(!(next.equals("."))) {
                if(next.equals("..")) {
                    if(newList.isEmpty()) {
                        throw new IllegalArgumentException("Path contains illegal traversal: " + String.join(sep, currentList));
                    } else {
                        newList.remove(newList.size() - 1);
                    }
                } else {
                    newList.add(next);
                }
            }
        }

        return newList;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof InMemoryPath && toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return pathElements == null
            ? "<path undefined>"
            : isEmpty() ? "." : String.join(sep, pathElements);
    }

    @Override
    public Path getParent() {
        final List<String> normalizedPath = normalizeModel(pathElements);
        if(normalizedPath.isEmpty()) {
            return null;
        } else {
            final List<String> subPath = normalizedPath.subList(0, normalizedPath.size() - 1);
            return new InMemoryPath(subPath);
        }
    }

    @Override
    public Path normalize() {
        return new InMemoryPath(normalizeModel(pathElements));
    }

    @Override
    public Path resolve(Path other) {
        final List<String> list = new ArrayList<>();
        final List<String> otherString = Arrays.asList(other.toString().split(sep));

        list.addAll(pathElements);
        list.addAll(otherString);

        return new InMemoryPath(list);
    }

    @Override
    public Path resolve(String other) {
        final List<String> list = new ArrayList<>();
        List<String> otherString = Arrays.asList(other.split(sep));

        list.addAll(pathElements);
        list.addAll(otherString);

        return new InMemoryPath(list);
    }

    @Override
    public Path getFileName() {
        throw new NotImplementedException("getFileName() has not been implemented");
    }

    @Override
    public URI toUri() {
        throw new NotImplementedException("toUri() has not been implemented");
    }

    @Override
    public Path relativize(Path other) {
        throw new NotImplementedException("relativize() has not been implemented");
    }

    @Override
    public Path resolveSibling(Path other) {
        throw new NotImplementedException("resolveSibling() has not been implemented");
    }

    @Override
    public Path resolveSibling(String other) {
        throw new NotImplementedException("resolveSibling() has not been implemented");
    }

    @Override
    public Iterator<Path> iterator() {
        throw new NotImplementedException("iterator() has not been implemented");
    }

    @Override
    public Path getName(int index) {
        throw new NotImplementedException("getName() has not been implemented");
    }

    @Override
    public FileSystem getFileSystem() {
        throw new NotImplementedException("getFileSystem() has not been implemented");
    }

    @Override
    public Path toAbsolutePath() {
        throw new NotImplementedException("toAbsolutePath() has not been implemented");    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        throw new NotImplementedException("toRealPath() has not been implemented");
    }

    @Override
    public File toFile() {
        throw new NotImplementedException("toFile() has not been implemented");
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        throw new NotImplementedException("register() has not been implemented");
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events) throws IOException {
        throw new NotImplementedException("register() has not been implemented");
    }

    @Override
    public int compareTo(Path other) {
        throw new NotImplementedException("compareTo() has not been implemented");
    }

    @Override
    public boolean isAbsolute() {
        throw new NotImplementedException("isAbsolute() has not been implemented");
    }

    @Override
    public Path getRoot() {
        throw new NotImplementedException("getRoot() has not been implemented");
    }

    @Override
    public int getNameCount() {
        throw new NotImplementedException("getNameCount() has not been implemented");
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        throw new NotImplementedException("subpath() has not been implemented");
    }

    @Override
    public boolean startsWith(Path other) {
        throw new NotImplementedException("startsWith() has not been implemented");
    }

    @Override
    public boolean startsWith(String other) {
        throw new NotImplementedException("startsWith() has not been implemented");
    }

    @Override
    public boolean endsWith(Path other) {
        throw new NotImplementedException("endsWith() has not been implemented");
    }

    @Override
    public boolean endsWith(String other) {
        throw new NotImplementedException("endsWith() has not been implemented");
    }
}
