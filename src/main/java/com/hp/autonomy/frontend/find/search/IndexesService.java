/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import com.hp.autonomy.hod.client.api.resource.Resource;
import com.hp.autonomy.hod.client.api.resource.Resources;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;

import java.util.List;

public interface IndexesService {

    Resources listIndexes() throws HodErrorException;

    Resources listIndexes(TokenProxy tokenProxy) throws HodErrorException;

    List<Resource> listActiveIndexes();

    List<Resource> listVisibleIndexes() throws HodErrorException;
}
