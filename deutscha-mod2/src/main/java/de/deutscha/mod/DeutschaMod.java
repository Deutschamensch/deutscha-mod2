package de.deutscha.mod;

import org.lwjgl.sdl.SDLScancode;

import de.deutscha.mod.cosmetics.Cosmetics;
import de.deutscha.mod.cosmetics.CosmeticsLayer;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;

public class DeutschaMod implements ClientModInitializer {
	private static final String MOD_ID = "deutschamod";

	@Override
	public void onInitializeClient() {
		AnvilNameStore.load();

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "dgh"));
		KeyMapping rainbowKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key." + MOD_ID + ".rainbow_mode",
			InputConstants.Type.KEYBOARD,
			SDLScancode.SDL_SCANCODE_R,
			category
		));

		KeyMapping wingsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key." + MOD_ID + ".wings",
			InputConstants.Type.KEYBOARD,
			SDLScancode.SDL_SCANCODE_G,
			category
		));
		KeyMapping cloakKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key." + MOD_ID + ".cloak",
			InputConstants.Type.KEYBOARD,
			SDLScancode.SDL_SCANCODE_H,
			category
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (rainbowKey.consumeClick()) {
				RainbowOverlay.toggle();
			}
			while (wingsKey.consumeClick()) {
				Cosmetics.toggleWings();
			}
			while (cloakKey.consumeClick()) {
				Cosmetics.toggleCloak();
			}
		});

		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof AvatarRenderer) {
				registrationHelper.register(new CosmeticsLayer((AvatarRenderer) entityRenderer, context.getItemModelResolver()));
			}
		});

		HudElementRegistry.addLast(
			Identifier.fromNamespaceAndPath(MOD_ID, "rainbow_mode"),
			(graphics, deltaTracker) -> RainbowOverlay.draw(graphics)
		);

		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			ScreenEvents.afterExtract(screen).register(
				(shownScreen, graphics, mouseX, mouseY, tickProgress) -> RainbowOverlay.draw(graphics)
			);
			if (screen instanceof AnvilScreen) {
				ScreenEvents.remove(screen).register(closed -> AnvilNameStore.commitPending());
			}
		});
	}
}
