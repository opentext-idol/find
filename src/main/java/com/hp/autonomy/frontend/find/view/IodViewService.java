package com.hp.autonomy.frontend.find.view;

import com.hp.autonomy.iod.client.error.IodErrorException;

import java.io.IOException;
import java.io.OutputStream;

public interface IodViewService {
    void viewDocument(OutputStream outputStream, String documentReference, String indexes) throws IodErrorException, IOException;
}
