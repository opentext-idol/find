package com.autonomy.abc.shared;

import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.query.QueryResult;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.control.Session;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.util.Waits;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;

public class SharedPreviewTests {

    public static void testDocumentPreviews(final Session session, final List<? extends QueryResult> searchResults, final Index index) {
        for(final QueryResult queryResult : searchResults) {
            final DocumentViewer documentViewer = queryResult.openDocumentPreview();
            testDocumentPreview(session, documentViewer, index);
            documentViewer.close();
        }
    }

    public static void testDocumentPreview(final Session session, final DocumentViewer documentViewer, final Index index){
        if(index == null){
            verifyThat("index is displayed", documentViewer.getIndexName(), not(nullValue()));
        } else {
            verifyThat(documentViewer.getIndex(), is(index));
        }

        verifyThat("reference is displayed", documentViewer.getReference(), not(isEmptyOrNullString()));
        //TODO this isn't being used on HSOD; CSA-1986 needs to be completed
        //verifyThat("content type is displayed", documentViewer.getContentType(), not(isEmptyOrNullString()));

        final String frameText = new Frame(session.getActiveWindow(), documentViewer.frame()).getText();

        verifyThat("frame has content", frameText, not(isEmptyOrNullString()));
        verifyThat(frameText, not(containsString("server error")));

//        TODO Can no longer open in a new tab from document preview - should probably check whether can open full preview
    }

    public static void testDocumentPreview(final Session session, final DocumentViewer documentViewer){
        testDocumentPreview(session, documentViewer, null);
    }
}
