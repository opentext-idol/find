package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.AbstractConfig;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationUtils;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = TemplatesConfig.TemplatesConfigBuilder.class)
public class TemplatesConfig extends AbstractConfig<TemplatesConfig> {
    @Singular("searchResultTemplate")
    private final List<Template> searchResult;

    @Singular("promotionTemplate")
    private final List<Template> promotion;

    @Singular("previewPanelTemplate")
    private final List<Template> previewPanel;

    @Singular("documentFactsTemplate")
    private final List<Template> documentFacts;

    @Singular("entitySearchTemplate")
    private final List<Template> entitySearch;

    @Singular("entityFactsTemplate")
    private final List<Template> entityFacts;

    @Singular("entityFactsDetailTemplate")
    private final List<Template> entityFactsDetail;

    @Override
    public TemplatesConfig merge(final TemplatesConfig other) {
        return ConfigurationUtils.defaultMerge(this, other);
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
        validateTemplateList(searchResult);
        validateTemplateList(promotion);
        validateTemplateList(previewPanel);
        validateTemplateList(documentFacts);
        validateTemplateList(entitySearch);
        validateTemplateList(entityFacts);
        validateTemplateList(entityFactsDetail);
    }

    @JsonIgnore
    public Set<String> listTemplateFiles() {
        return Stream.of(searchResult, promotion, previewPanel, documentFacts, entitySearch, entityFacts, entityFactsDetail)
                .flatMap(Collection::stream)
                .map(Template::getFile)
                .collect(Collectors.toSet());
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
