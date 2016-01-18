package com.autonomy.abc.framework.statements;


import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.framework.*;
import com.autonomy.abc.selenium.control.Session;
import com.autonomy.abc.selenium.control.SessionRegistry;

public class StatementArtifactHandler implements StatementHandler, ArtifactSaveVisitor.ArtifactSaver {
    private final ArtifactSaveVisitor saveVisitor = new ArtifactSaveVisitor();
    private final SessionRegistry sessions;
    private final String timestamp;
    private TestStatement currentStatement;

    public StatementArtifactHandler(ABCTestBase test) {
        sessions = test.getSessionRegistry();
        timestamp = TestState.get().getTimestamp();
    }

    @Override
    public void handle(TestStatement testStatement) {
        if (testStatement.failed()) {
            currentStatement = testStatement;
            saveVisitor.visit(this);
        }
    }

    @Override
    public String baseLocation() {
        String[] splitName = currentStatement.getMethodName().split("\\[");
        return ".output/"
                + splitName[0] + '/'
                + timestamp + '[' + splitName[splitName.length-1]
                + "_" + currentStatement.getNumber();
    }

    @Override
    public Iterable<Session> getSessions() {
        return sessions;
    }
}
