/*
 * Copyright 2014-2017 Open Text.
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

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {
    @Value("${server.reverseProxy}")
    private boolean useReverseProxy;

    @Value("${server.ajp.port}")
    private int ajpPort;

    @Value("${server.tomcat.resources.max-cache-kb}")
    private long webResourcesCacheSize;

    @Value("${server.tomcat.connector.max-post-size}")
    private int connectorMaxPostSize;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> connectorCustomizer() {
        return (tomcat) -> {
            if(useReverseProxy) {
                tomcat.addAdditionalTomcatConnectors(createAjpConnector());
            }

            // Set the web resources cache size (this defaults to 10MB but that is too small for Find)
            tomcat.addContextCustomizers(context -> {
                final WebResourceRoot resources = new StandardRoot(context);
                resources.setCacheMaxSize(webResourcesCacheSize);
                context.setResources(resources);
            });

            tomcat.addConnectorCustomizers(connector -> {
                connector.setMaxPostSize(connectorMaxPostSize);
            });
        };
    }

    private Connector createAjpConnector() {
        final Connector connector = new Connector("AJP/1.3");
        connector.setProperty("address", "0.0.0.0");
        connector.setPort(ajpPort);
        connector.setProperty("tomcatAuthentication", "false");
        connector.setProperty("allowedRequestAttributesPattern", ".*");
        ((AbstractAjpProtocol<?>) connector.getProtocolHandler()).setSecretRequired(false);
        return connector;
    }
}
