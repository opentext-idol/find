package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.AbstractConfig;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationUtils;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = TemplatesConfig.TemplatesConfigBuilder.class)
public class TemplatesConfig extends AbstractConfig<TemplatesConfig> {
    private final List<Template> resultsList;
    private final List<Template> previewPanel;

    @Override
    public TemplatesConfig merge(final TemplatesConfig other) {
        return ConfigurationUtils.defaultMerge(this, other);
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
        validateTemplateList(resultsList);
        validateTemplateList(previewPanel);
    }

    private void validateTemplateList(final Collection<Template> templateList) throws ConfigException {
        if (templateList.stream().anyMatch(template -> StringUtils.isBlank(template.getFile()))) {
            throw new ConfigException("Templates Configuration", "Templates must contain a file parameter");
        }
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class TemplatesConfigBuilder {}
}
