package com.autonomy.abc.framework.rules;

import com.autonomy.abc.config.SeleniumTest;
import com.autonomy.abc.framework.ArtifactSaveVisitor;
import com.autonomy.abc.framework.TestState;
import com.autonomy.abc.selenium.control.Session;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;

public class TestArtifactRule extends TestWatcher implements ArtifactSaveVisitor.ArtifactSaver {
    private final ArtifactSaveVisitor saveVisitor = new ArtifactSaveVisitor();
    private final SeleniumTest<?, ?> testBase;
    private final String timestamp;
    private Description currentDescription;

    public TestArtifactRule(SeleniumTest<?, ?> testBase) {
        this.testBase = testBase;
        timestamp = TestState.get().getTimestamp();
    }

    @Override
    protected void failed(Throwable e, Description description) {
        currentDescription = description;
        if (e instanceof MultipleFailureException) {
            for (Throwable failure : ((MultipleFailureException) e).getFailures()) {
                if (!(failure instanceof AssertionError)) {
                    saveVisitor.visit(this);
                    break;
                }
            }
        } else if (!(e instanceof AssertionError)) {
            saveVisitor.visit(this);
        }
    }

    @Override
    public String baseLocation() {
        String[] splitName = currentDescription.getMethodName().split("\\[");
        return ".output/"
                + splitName[0] + '/'
                + timestamp + '[' + splitName[splitName.length - 1];
    }

    @Override
    public Iterable<Session> getSessions() {
        return testBase.getSessionRegistry();
    }
}
