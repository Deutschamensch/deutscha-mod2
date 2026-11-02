package de.deutscha.mod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.deutscha.mod.MusicPlayer;
import de.deutscha.mod.PauseButtons;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
	protected PauseScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"), require = 0)
	private void deutschamod$addButtons(CallbackInfo ci) {
		this.addRenderableWidget(
			Button.builder(Component.empty(), button -> PauseButtons.toggleServerPack())
				.bounds(PauseButtons.serverPackX(this.width), PauseButtons.y(this.height), PauseButtons.SIZE, PauseButtons.SIZE)
				.tooltip(Tooltip.create(Component.literal("Serverpack an/aus (grün = an, rot = aus)")))
				.build()
		);
		this.addRenderableWidget(
			Button.builder(Component.empty(), button -> MusicPlayer.toggle())
				.bounds(PauseButtons.musicX(this.width), PauseButtons.y(this.height), PauseButtons.SIZE, PauseButtons.SIZE)
				.tooltip(Tooltip.create(Component.literal("Musik: Little Adventures (grün = läuft, rot = aus)")))
				.build()
		);
	}
}
