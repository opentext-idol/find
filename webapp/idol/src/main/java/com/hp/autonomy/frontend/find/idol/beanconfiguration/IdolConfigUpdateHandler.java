package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;

@FunctionalInterface
public interface IdolConfigUpdateHandler {

    void update(IdolFindConfig config);

}
