package com.autonomy.abc.framework.statements;


import com.autonomy.abc.config.SeleniumTest;
import com.autonomy.abc.framework.ArtifactSaveVisitor;
import com.autonomy.abc.framework.TestState;
import com.autonomy.abc.framework.TestStatement;
import com.autonomy.abc.selenium.control.Session;

public class StatementArtifactHandler implements StatementHandler, ArtifactSaveVisitor.ArtifactSaver {
    private final ArtifactSaveVisitor saveVisitor = new ArtifactSaveVisitor();
    private final SeleniumTest<?, ?> testBase;
    private final String timestamp;
    private TestStatement currentStatement;

    public StatementArtifactHandler(SeleniumTest<?, ?> test) {
        testBase = test;
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
        return testBase.getSessionRegistry();
    }
}
