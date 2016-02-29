package com.autonomy.abc.documentPreview;

import com.autonomy.abc.config.Browser;
import com.autonomy.abc.selenium.control.Session;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.util.Waits;
import org.openqa.selenium.remote.RemoteWebDriver;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
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

        if (((RemoteWebDriver) session.getDriver()).getCapabilities().getBrowserName().equals(Browser.FIREFOX.toString())){
            verifyThat(documentViewer.frame(), not(containsText("500")));
        }

        testOpenInNewTab(session, documentViewer);
    }

    public static void testDocumentPreview(Session session, DocumentViewer documentViewer){
        testDocumentPreview(session, documentViewer, null);
    }

    public static void testOpenInNewTab(Session session, DocumentViewer documentViewer){
        String reference = documentViewer.getReference();

        Window original = session.getActiveWindow();
        documentViewer.openInNewTab();
        Waits.loadOrFadeWait();
        Window newWindow = session.switchWindow(1);
        newWindow.activate();

        verifyThat(session.getDriver().getCurrentUrl(), is(reference));

        newWindow.close();
        original.activate();
    }
}
