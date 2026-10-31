package de.deutscha.mod.cosmetics;

public final class Cosmetics {
	private static boolean wingsEnabled = true;
	private static boolean cloakEnabled = true;

	private Cosmetics() {
	}

	public static boolean wingsEnabled() {
		return wingsEnabled;
	}

	public static boolean cloakEnabled() {
		return cloakEnabled;
	}

	public static void toggleWings() {
		wingsEnabled = !wingsEnabled;
	}

	public static void toggleCloak() {
		cloakEnabled = !cloakEnabled;
	}
}
