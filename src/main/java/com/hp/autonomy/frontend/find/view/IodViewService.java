package com.hp.autonomy.frontend.find.view;

import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;

import java.io.IOException;
import java.io.OutputStream;

public interface IodViewService {
    void viewDocument(OutputStream outputStream, String documentReference, ResourceIdentifier indexes) throws IOException, HodErrorException;
}
