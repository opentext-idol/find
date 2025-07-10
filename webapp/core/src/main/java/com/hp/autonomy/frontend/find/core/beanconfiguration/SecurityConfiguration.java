/*
 * Copyright 2015-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;

import org.springframework.security.web.firewall.StrictHttpFirewall;

public class SecurityConfiguration {

    public static StrictHttpFirewall firewallAllowingUrlEncodedCharacters() {
        final StrictHttpFirewall firewall = new StrictHttpFirewall();

        // We use encoded IDOL field names, e.g.
        //   'api/public/parametric/numeric/buckets/NODE_PLACE%252FPLACE_POPULATION'
        // so we have to allow these fields through.
        firewall.setAllowUrlEncodedPercent(true);
        firewall.setAllowUrlEncodedPeriod(true);
        firewall.setAllowUrlEncodedSlash(true);

        // for themetracker
        firewall.setAllowUrlEncodedLineFeed(true);

        return firewall;
    }

}
