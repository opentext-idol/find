package com.autonomy.abc.config;

public enum ApplicationType {
	ON_PREM("On Premise"),
	HOSTED("Hosted");

	private final String name;

	ApplicationType(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
