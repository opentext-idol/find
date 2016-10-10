package com.hp.autonomy.frontend.find.core.fieldtext;

import com.hp.autonomy.frontend.find.core.fields.ParametricRange;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

@Component
public class StringToParametricRangeConverter implements Converter<String, ParametricRange> {
    private static final Pattern COMPILE = Pattern.compile("::");

    @Override
    public ParametricRange convert(final String source) {
        final String decodedSource = decodeUriComponent(source);
        final String[] parts = COMPILE.split(decodedSource);
        return new ParametricRange(parts[0], Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), ParametricRange.Type.valueOf(parts[3]));
    }

    private String decodeUriComponent(final String part) {
        try {
            return URLDecoder.decode(part, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError("All JVMs must support UTF-8", e);
        }
    }
}