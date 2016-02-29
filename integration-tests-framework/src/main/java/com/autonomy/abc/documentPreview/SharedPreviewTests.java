package com.autonomy.abc.documentPreview;

import com.autonomy.abc.selenium.control.Frame;
import com.autonomy.abc.selenium.control.Session;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.search.SearchResult;
import com.autonomy.abc.selenium.util.Waits;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;

public class SharedPreviewTests {

    public static void testDocumentPreview(Session session, DocumentViewer documentViewer, Index index){
        if(index == null){
            verifyThat(documentViewer.getIndex(), not(nullValue()));
        } else {
            verifyThat(documentViewer.getIndex(), is(index));
        }

        verifyThat(documentViewer.getReference(), not(isEmptyOrNullString()));
        verifyThat(documentViewer.getContentType(), not(isEmptyOrNullString()));

        String frameText = new Frame(session.getActiveWindow(), documentViewer.frame()).getText();

        verifyThat(frameText, not(isEmptyOrNullString()));
        verifyThat(frameText, not(containsString("500")));

        testOpenInNewTabFromViewer(session, documentViewer);
    }

    public static void testDocumentPreview(Session session, DocumentViewer documentViewer){
        testDocumentPreview(session, documentViewer, null);
    }

    public static void testOpenInNewTabFromViewer(Session session, DocumentViewer documentViewer){
        String reference = documentViewer.getReference();

        Window original = session.getActiveWindow();
        documentViewer.openInNewTab();
        Waits.loadOrFadeWait();
        Window newWindow = session.switchWindow(session.countWindows() - 1);
        newWindow.activate();

        verifyThat(session.getDriver().getCurrentUrl(), is(reference));

        newWindow.close();
        original.activate();
    }
}
