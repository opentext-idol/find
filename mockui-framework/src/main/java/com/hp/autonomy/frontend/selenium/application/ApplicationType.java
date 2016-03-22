package com.hp.autonomy.frontend.selenium.application;

// could remove this to fully separate hosted/on-prem, but left for convenience
// allows shared tests to do a simple "if hosted then x else y"
// instead of excessively complicated polymorphic method calls
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

	// should only be used for parsing config
	public static ApplicationType fromString(String type) {
		return type.toLowerCase().equals("hosted") ? HOSTED : ON_PREM;
	}
}
