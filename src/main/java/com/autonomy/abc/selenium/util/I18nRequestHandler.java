package com.autonomy.abc.selenium.util;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class I18nRequestHandler {

	private final WebDriver driver;
	private final List<I18nRequest> toDo = new ArrayList<>();

	private static final String JS_FILE_NAME = "/com/autonomy/idoladmin/selenium/util/i18n.js";

	private final String js;

	public I18nRequestHandler(final WebDriver driver) {
		this.driver = driver;

		final StringBuilder builder;
		try {
			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(I18nRequestHandler.class.getResourceAsStream(JS_FILE_NAME)));
			builder = new StringBuilder();

			String line = bufferedReader.readLine();

			while (line != null) {
				builder.append(line);
				line = bufferedReader.readLine();
			}
		} catch (IOException e) {
			// the file defined above has issues
			throw new AssertionError(e);
		}

		js = builder.toString();
	}

	public void addRequests(final Collection<I18nRequest> requests) {
		toDo.addAll(requests);
	}

	public void addRequests(final I18nRequest request, final I18nRequest... requests) {
		toDo.add(request);
		addRequests(Arrays.asList(requests));
	}

	public String evaluateRequest(final I18nRequest request) {
		addRequests(request);
		String value = request.getValue();

		if (value == null) {
			evaluatePendingRequests();
			value = request.getValue();
		}

		return value;
	}

	private void evaluatePendingRequests() {
		final JavascriptExecutor executor = (JavascriptExecutor) driver;
		driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);

		final Object forJavascript = forJavascript(toDo);

		final Object fromJavascript = executor.executeAsyncScript(js, forJavascript);

		// javascript returns an array of strings
		@SuppressWarnings("unchecked")
		final List<String> responses = (List<String>) fromJavascript;

		if (toDo.size() != responses.size()) {
			throw new IllegalStateException("Number of responses does not match number of requests");
		}

		final Iterator<String> responseIterator = responses.iterator();

		for (final I18nRequest request : toDo) {
			final String response = responseIterator.next();
			request.setValue(response);
		}

		toDo.clear();
	}

	private static List<List<Object>> forJavascript(final List<I18nRequest> requests) {
		final List<List<Object>> output = new ArrayList<>(requests.size());
		for (final I18nRequest req : requests) {
			output.add(req.forJavascript());
		}
		return output;
	}

}
