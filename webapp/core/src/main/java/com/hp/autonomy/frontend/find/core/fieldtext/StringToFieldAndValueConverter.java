package com.hp.autonomy.frontend.find.core.fieldtext;

import com.hp.autonomy.frontend.find.core.fields.FieldAndValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

@Component
public class StringToFieldAndValueConverter implements Converter<String, FieldAndValue> {
    private static final Pattern COMPILE = Pattern.compile("::");

    @Override
    public FieldAndValue convert(final String source) {
        final String decodedSource = decodeUriComponent(source);
        final String[] parts = COMPILE.split(decodedSource);
        return new FieldAndValue(parts[0], parts[1]);
    }

    private String decodeUriComponent(final String part) {
        try {
            return URLDecoder.decode(part, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError("All JVMs must support UTF-8", e);
        }
    }
}
