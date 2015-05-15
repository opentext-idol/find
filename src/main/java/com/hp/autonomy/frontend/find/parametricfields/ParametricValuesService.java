package com.hp.autonomy.frontend.find.parametricfields;

import com.hp.autonomy.iod.client.error.IodErrorException;

import java.util.Set;

public interface ParametricValuesService {

    Set<ParametricFieldName> getAllParametricValues(ParametricRequest parametricRequest) throws IodErrorException;

}
