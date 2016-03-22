package com.hp.autonomy.frontend.selenium.framework.artifacts;


import com.hp.autonomy.frontend.selenium.base.SeleniumTest;
import com.hp.autonomy.frontend.selenium.framework.state.TestState;
import com.hp.autonomy.frontend.selenium.framework.state.TestStatement;
import com.hp.autonomy.frontend.selenium.control.Session;
import com.hp.autonomy.frontend.selenium.util.Handler;

public class StatementArtifactHandler implements Handler<TestStatement>, ArtifactSaveVisitor.ArtifactSaver {
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
