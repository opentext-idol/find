package com.autonomy.abc.selenium.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class I18nRequest {

	private final String name;
	private final String[] args;
	private String value = null;

	public I18nRequest(final String name, final String... args) {
		this.name = name;
		this.args = args;
	}

	public I18nRequest(final I18nRequest request) {
		this(request.name, request.args);
	}

	public String getName() {
		return name;
	}

	public String[] getArgs() {
		return args;
	}

	String getValue() {
		return value;
	}

	void setValue(final String value) {
		this.value = value;
	}

	List<Object> forJavascript() {
		final List<Object> output = new ArrayList<>();
		output.add(name);
		output.addAll(Arrays.asList(args));
		return output;
	}

}
