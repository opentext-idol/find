package com.hp.autonomy.frontend.configuration;

import com.autonomy.frontend.configuration.SingleUserAuthentication;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/*
 * $Id:$
 *
 * Copyright (c) 2014, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className", defaultImpl = SingleUserAuthentication.class)
public class AuthenticationMixins {
}
