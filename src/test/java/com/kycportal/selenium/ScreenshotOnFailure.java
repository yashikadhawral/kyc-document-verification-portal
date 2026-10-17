package com.kycportal.selenium;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/** Saves a screenshot of the browser when a test fails, before the browser is closed. */
public class ScreenshotOnFailure implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        Object instance = context.getRequiredTestInstance();
        if (instance instanceof BrowserTest test && test.driver instanceof TakesScreenshot shot) {
            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            String name = context.getRequiredTestClass().getSimpleName()
                + "-" + context.getRequiredTestMethod().getName() + ".png";
            Files.write(dir.resolve(name), shot.getScreenshotAs(OutputType.BYTES));
        }
        throw throwable;
    }
}