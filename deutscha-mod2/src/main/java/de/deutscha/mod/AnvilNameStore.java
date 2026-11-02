package de.deutscha.mod;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;

public final class AnvilNameStore {
	private static final String GRADIENT_KEY = "\u0001gradient";
	private static final int[] DEFAULT_GRADIENT = {0xAA0000, 0xFF5555};
	private static final Path FILE = FabricLoader.getInstance().getConfigDir()
		.resolve("deutschamod")
		.resolve("anvil_names.json");
	private static final Map<String, String> NAMES = new HashMap<>();

	private static String pendingPlain;
	private static String pendingColored;

	private AnvilNameStore() {
	}

	public static void load() {
		if (!Files.exists(FILE)) {
			return;
		}
		try {
			JsonObject root = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
				NAMES.put(entry.getKey(), entry.getValue().getAsString());
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static String get(String plainName) {
		return lookup(plainName);
	}

	public static String lookup(String plainName) {
		if (pendingPlain != null && pendingPlain.equals(plainName)) {
			return pendingColored;
		}
		if (GRADIENT_KEY.equals(plainName)) {
			return null;
		}
		return NAMES.get(plainName);
	}

	public static int[] gradient() {
		String stored = NAMES.get(GRADIENT_KEY);
		if (stored == null || stored.isEmpty()) {
			return DEFAULT_GRADIENT;
		}
		String[] parts = stored.split(",");
		int[] colors = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			colors[i] = Integer.parseInt(parts[i], 16);
		}
		return colors;
	}

	public static void setGradient(int[] colors) {
		StringBuilder joined = new StringBuilder();
		for (int i = 0; i < colors.length; i++) {
			if (i > 0) {
				joined.append(',');
			}
			joined.append(String.format("%06X", colors[i]));
		}
		NAMES.put(GRADIENT_KEY, joined.toString());
		save();
	}

	public static void setPending(String plain, String colored) {
		pendingPlain = plain;
		pendingColored = colored;
	}

	public static void clearPending() {
		pendingPlain = null;
		pendingColored = null;
	}

	public static void commitPending() {
		if (pendingPlain == null || pendingPlain.isEmpty()) {
			return;
		}
		NAMES.put(pendingPlain, pendingColored);
		clearPending();
		save();
	}

	private static void save() {
		JsonObject root = new JsonObject();
		NAMES.forEach(root::addProperty);
		try {
			Files.createDirectories(FILE.getParent());
			Files.writeString(FILE, new GsonBuilder().setPrettyPrinting().create().toJson(root));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
