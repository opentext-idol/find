package com.hp.autonomy.frontend.find.core.fields;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldType;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import com.hp.autonomy.searchcomponents.core.fields.FieldsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
class FindFieldsMapper implements FieldsMapper {

    private final ConfigService<? extends FindConfig> configService;

    @Autowired
    public FindFieldsMapper(final ConfigService<? extends FindConfig> configService) {
        this.configService = configService;
    }

    @Override
    public String transformFieldName(final String name) {
        final Optional<FieldInfo<?>> fieldInfo = getFieldDisplayNameFromName(name);
        return fieldInfo.map(FieldInfo::getDisplayName).orElseGet(() -> {
            final StringBuilder stringBuilder = new StringBuilder();
            for (final String s : name.substring(name.lastIndexOf('/') + 1).replace('_', ' ').split(" ")) {
                stringBuilder.append(s.substring(0, 1).toUpperCase()).append(s.substring(1).toLowerCase()).append(' ');
            }

            return stringBuilder.toString().trim();
        });
    }

    @Override
    public String transformFieldValue(final String fieldName, final String value) {
        final Optional<FieldInfo<?>> fieldInfo = getFieldDisplayNameFromName(fieldName);
        if (fieldInfo.isPresent() && fieldInfo.get().getType() == FieldType.STRING) {
            final String displayValue = fieldInfo.get().getValueDisplayNames().get(value);
            return displayValue != null ? displayValue : value;
        }
        return value;
    }

    @Override
    public Collection<String> restoreFieldValue(final String fieldName, final String displayValue) {
        final Optional<FieldInfo<?>> fieldInfo = getFieldDisplayNameFromName(fieldName);
        if (fieldInfo.isPresent()) {

            final List<String> strings = fieldInfo.get().getValueDisplayNames().entrySet().stream().filter(entry -> entry.getValue().equals(displayValue)).map(Map.Entry::getKey).collect(Collectors.toList());
            return strings.isEmpty() ? Collections.singleton(displayValue) : strings;
        }

        return Collections.singleton(displayValue);
    }

    private Optional<FieldInfo<?>> getFieldDisplayNameFromName(final String name) {
        final FieldsInfo fieldsInfo = configService.getConfig().getFieldsInfo();
        return Optional.ofNullable(fieldsInfo.getFieldConfigByName().get(name));
    }
}
