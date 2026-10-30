package de.deutscha.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;

public class DeutschaMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AnvilNameStore.load();

		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof AnvilScreen) {
				ScreenEvents.remove(screen).register(closed -> AnvilNameStore.commitPending());
			}
		});
	}
}
