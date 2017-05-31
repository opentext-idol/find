/*
 * Copyright 2014-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;

import com.hp.autonomy.frontend.configuration.validation.ValidationResults;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static com.hp.autonomy.frontend.find.idol.customisations.AssetValidationServiceTest.ValidationResultsMatcher.valid;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetValidationServiceTest {

    private static final String EXISTING_FILE_NAME = "existing-big.png";
    private static final String NON_EXISTENT_FILE_NAME = "non-existent-big.png";
    @Mock
    private CustomisationService customisationService;

    @Mock
    private File existingAsset;

    @Mock
    private File nonExistentAsset;

    @Mock
    private AssetConfig assetConfig;

    private AssetValidationService assetValidationService;

    @Before
    public void setUp() {
        when(existingAsset.exists()).thenReturn(true);
        when(nonExistentAsset.exists()).thenReturn(false);

        when(customisationService.getAsset(AssetType.BIG_LOGO, EXISTING_FILE_NAME)).thenReturn(existingAsset);
        when(customisationService.getAsset(AssetType.BIG_LOGO, NON_EXISTENT_FILE_NAME)).thenReturn(nonExistentAsset);

        assetValidationService = new AssetValidationService(customisationService);
    }

    @Test
    public void testValidateConfig() {
        when(assetConfig.getAssetPath(AssetType.BIG_LOGO)).thenReturn(EXISTING_FILE_NAME);

        final ValidationResults validationResults = assetValidationService.validateConfig(assetConfig);

        assertThat(validationResults, is(valid()));
    }

    @Test
    public void testNullAssetIsValid() {
        when(assetConfig.getAssetPath(AssetType.BIG_LOGO)).thenReturn(null);

        final ValidationResults validationResults = assetValidationService.validateConfig(assetConfig);

        assertThat(validationResults, is(valid()));
    }

    @Test
    public void testInvalidConfig() {
        when(assetConfig.getAssetPath(AssetType.BIG_LOGO)).thenReturn(NON_EXISTENT_FILE_NAME);

        final ValidationResults validationResults = assetValidationService.validateConfig(assetConfig);

        assertThat(validationResults, is(not(valid())));
    }

    static class ValidationResultsMatcher extends TypeSafeMatcher<ValidationResults> {

        private ValidationResultsMatcher() {}

        static ValidationResultsMatcher valid() {
            return new ValidationResultsMatcher();
        }

        @Override
        protected boolean matchesSafely(final ValidationResults item) {
            return item.isValid();
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("a valid result");
        }

        @Override
        protected void describeMismatchSafely(final ValidationResults item, final Description mismatchDescription) {
            mismatchDescription.appendText("not a valid result");
        }
    }

}