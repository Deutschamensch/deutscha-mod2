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
		return NAMES.get(plainName);
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
