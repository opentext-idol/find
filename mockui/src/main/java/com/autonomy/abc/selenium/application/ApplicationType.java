package com.autonomy.abc.selenium.application;

// could remove this to fully separate hosted/on-prem, but left for convenience
// allows shared tests to do a simple "if hosted then x else y"
// instead of excessively complicated polymorphic method calls
public enum ApplicationType {
	ON_PREM("On Premise", "com.autonomy.abc.selenium.application.OPISOApplication"),
	HOSTED("Hosted", "com.autonomy.abc.selenium.application.HSOApplication");

	private final String name;
	private final String className;

	ApplicationType(final String name, final String className) {
		this.name = name;
		this.className = className;
	}

	public String getName() {
		return name;
	}

	// instead exposed as factory method Application.ofType
	SearchOptimizerApplication makeSearchApplication() {
		try {
			return (SearchOptimizerApplication) Class.forName(className).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new IllegalStateException("Could not create Application object - check that the correct mockui package is included in the POM", e);
		}
	}

	public static ApplicationType fromString(String type) {
		return type.toLowerCase().equals("hosted") ? HOSTED : ON_PREM;
	}
}
