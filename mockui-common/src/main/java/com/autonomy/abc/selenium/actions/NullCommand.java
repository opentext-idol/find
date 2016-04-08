package com.autonomy.abc.selenium.actions;

public final class NullCommand implements Command {
    private final static NullCommand INSTANCE = new NullCommand();

    private NullCommand() {}

    @Override
    public void execute() {}

    public static NullCommand getInstance() {
        return INSTANCE;
    }
}
