package com.hp.autonomy.frontend.find.parametricfields;

import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;

import java.util.Set;

public interface IndexFieldsService {

    Set<String> getParametricFields(ResourceIdentifier index) throws HodErrorException;

}