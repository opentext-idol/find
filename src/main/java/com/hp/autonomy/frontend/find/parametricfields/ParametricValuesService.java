package com.hp.autonomy.frontend.find.parametricfields;

import com.hp.autonomy.hod.client.error.HodErrorException;

import java.util.Set;

public interface ParametricValuesService {

    Set<ParametricFieldName> getAllParametricValues(ParametricRequest parametricRequest) throws HodErrorException;

}
