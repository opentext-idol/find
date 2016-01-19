package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.page.HSOElementFactory;
import org.openqa.selenium.WebDriver;

public class HSODFindApplication extends FindApplication<HSOElementFactory> {
    @Override
    public HSOElementFactory createElementFactory(WebDriver driver) {
        return new HSOElementFactory(driver);
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.HOSTED;
    }
}
