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
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

public class DeutschaMod implements ClientModInitializer {
	private static final String MOD_ID = "deutschamod";
	private static final int THUNDER_INTERVAL_TICKS = 160;
	private static final float THUNDER_VOLUME = 0.25F;

	private static boolean thunderEnabled = true;
	private static int thunderTicks;

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
		KeyMapping thunderKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key." + MOD_ID + ".thunder",
			InputConstants.Type.KEYBOARD,
			SDLScancode.SDL_SCANCODE_J,
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
			while (thunderKey.consumeClick()) {
				thunderEnabled = !thunderEnabled;
			}

			if (thunderEnabled && client.level != null && !client.isPaused()) {
				thunderTicks++;
				if (thunderTicks >= THUNDER_INTERVAL_TICKS) {
					thunderTicks = 0;
					float pitch = 0.85F + client.level.getRandom().nextFloat() * 0.3F;
					client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.LIGHTNING_BOLT_THUNDER, pitch, THUNDER_VOLUME));
				}
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
			ScreenEvents.afterExtract(screen).register((shownScreen, graphics, mouseX, mouseY, tickProgress) -> {
				if (shownScreen instanceof PauseScreen) {
					PauseButtons.draw(graphics, shownScreen.width, shownScreen.height);
				}
				RainbowOverlay.draw(graphics);
			});
			if (screen instanceof AnvilScreen) {
				ScreenEvents.remove(screen).register(closed -> AnvilNameStore.commitPending());
			}
		});
	}
}
