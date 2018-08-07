/*
 * Copyright (c) 2018, Micro Focus International plc.
 */

package com.hp.autonomy.frontend.find.idol.search;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
public class Holder {
    private String basicToken;
    private String idPortaleRis;
}
