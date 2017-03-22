package com.hp.autonomy.frontend.find.core.beanconfiguration;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.configuration.export.PowerPointConfig;
import com.hp.autonomy.frontend.reports.powerpoint.PowerPointService;
import com.hp.autonomy.frontend.reports.powerpoint.PowerPointServiceImpl;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateSettings;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateSettingsSource;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateSource;
import com.hp.autonomy.frontend.reports.powerpoint.dto.Anchor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.util.Optional;

@Configuration
public class ExportConfiguration {
    @SuppressWarnings("resource")
    @Bean
    public PowerPointService powerPointService(final ConfigService<? extends FindConfig<?, ?>> configService) {
        final TemplateSource templateSource = () -> {
            final Optional<String> maybeTemplateFile = getConfig(configService).flatMap(powerPointConfig -> Optional.ofNullable(StringUtils.defaultIfBlank(powerPointConfig.getTemplateFile(), null)));
            return maybeTemplateFile.isPresent() ? new FileInputStream(maybeTemplateFile.get()) : TemplateSource.DEFAULT.getInputStream();
        };
        final TemplateSettingsSource templateSettingsSource = () -> getConfig(configService)
                .map(this::getTemplateSettings)
                .orElse(TemplateSettingsSource.DEFAULT.getSettings());

        return new PowerPointServiceImpl(templateSource, templateSettingsSource);
    }

    private Optional<PowerPointConfig> getConfig(final ConfigService<? extends FindConfig<?, ?>> configService) {
        return Optional.ofNullable(configService.getConfig().getExport())
                .flatMap(exportConfig -> Optional.ofNullable(exportConfig.getPowerpoint()));
    }

    private TemplateSettings getTemplateSettings(final PowerPointConfig powerPointConfig) {
        final Anchor anchor = new Anchor();

        final double tmpMarginTop = readMarginConfiguration(powerPointConfig.getMarginTop());
        final double tmpMarginLeft = readMarginConfiguration(powerPointConfig.getMarginLeft());
        final double tmpMarginRight = readMarginConfiguration(powerPointConfig.getMarginRight());
        final double tmpMarginBottom = readMarginConfiguration(powerPointConfig.getMarginBottom());

        anchor.setX(tmpMarginLeft);
        anchor.setY(tmpMarginTop);
        anchor.setWidth(1 - tmpMarginLeft - tmpMarginRight);
        anchor.setHeight(1 - tmpMarginTop - tmpMarginBottom);

        return new TemplateSettings(anchor);
    }

    private double readMarginConfiguration(final Double margin) {
        return Optional.ofNullable(margin).orElse(0D);
    }
}
