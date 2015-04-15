/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import com.hp.autonomy.iod.client.api.search.*;
import com.hp.autonomy.iod.client.error.IodErrorException;

import java.util.List;

public interface DocumentsService {

    Documents queryTextIndex(String text, int maxResults, Summary summary, List<String> indexes) throws IodErrorException;

}
