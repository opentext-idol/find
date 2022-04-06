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

import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Less4jStyleCompilerTest {
    private StyleCompiler styleCompiler;
    @Mock
    private DefaultLessCompiler lessCompiler;
    @Mock
    private LessCompiler.Configuration lessCompilerConfig;
    @Mock
    private LessCompiler.CompilationResult result;

    private HashMap<String, String> extraVariables = new HashMap<>();
    private HashMap<String, String> extraVariablesExpected = new HashMap<>();
    private String output;

    @Before
    public void setUp() throws Exception {
        extraVariables.put("name", "value");
        extraVariablesExpected.put("@name", "value");

        when(result.getCss()).thenReturn("css!");
        when(lessCompiler.compile(any(LessResource.class), eq(lessCompilerConfig))).thenReturn(result);
        styleCompiler = new Less4jStyleCompiler(extraVariables, lessCompiler, lessCompilerConfig);
        output = styleCompiler.compile(new InMemoryPath("./less/file.less"));
    }

    @Test
    public void testCompileCallsThroughToCompiler() throws Exception {
        verify(lessCompiler, times(1))
            .compile(any(LessResource.class), eq(lessCompilerConfig));
        assertEquals(output, "css!");
    }

    @Test
    public void testCompilePrependsAtToVariableNames() throws Exception {
        verify(lessCompilerConfig, times(1))
            .addExternalVariables(eq(extraVariablesExpected));
    }
}
