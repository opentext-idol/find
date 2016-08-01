/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.ConfigException;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public abstract class ConfigurationComponentTest<C extends ConfigurationComponent<C>> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private C component;

    @Before
    public void setUp() {
        component = constructComponent();
    }

    protected abstract C constructComponent();

    protected abstract Class<C> getComponentType();

    protected abstract String sampleJson() throws IOException;

    protected abstract void validateJson(final String json);

    protected abstract void validateParsedComponent(final C component);

    protected abstract void validateMergedComponent(final C mergedComponent);

    @Test
    public void toJson() throws IOException {
        final String json = writeObjectToJson(component);
        validateJson(json);
    }

    @Test
    public void fromJson() throws IOException {
        final String sampleJson = sampleJson();
        final C component = readObjectFromJson(sampleJson);
        validateParsedComponent(component);
    }

    @Test
    public void jsonSymmetry() throws IOException {
        final String sampleJson = sampleJson();
        final C component = readObjectFromJson(sampleJson);
        final String json = writeObjectToJson(component);
        assertThat(json, sameJSONAs(sampleJson).allowingAnyArrayOrdering());
    }

    @Test
    public void merge() throws IOException {
        final String sampleJson = sampleJson();
        final C defaults = readObjectFromJson(sampleJson);
        final C mergedComponent = component.merge(defaults);
        validateMergedComponent(mergedComponent);
    }

    @Test
    public void mergeWithNothing() {
        final C mergedComponent = component.merge(null);
        assertEquals(component, mergedComponent);
    }

    @Test
    public void validateGoodConfig() throws ConfigException {
        component.basicValidate();
    }

    private String writeObjectToJson(final C component) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, component);
        return outputStream.toString();
    }

    private C readObjectFromJson(final String sampleJson) throws IOException {
        return objectMapper.readValue(sampleJson, getComponentType());
    }
}
