package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.FindService;
import com.hp.autonomy.frontend.selenium.application.Application;

public abstract class FindApplication<T extends FindElementFactory> implements Application<T> {
    public abstract FindService findService();
}
