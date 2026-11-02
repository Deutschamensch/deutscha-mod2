package de.deutscha.mod.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
	@Inject(method = "getPlayerInfos", at = @At("RETURN"), cancellable = true, require = 0)
	private void deutschamod$selfOnTop(CallbackInfoReturnable<List<PlayerInfo>> cir) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener connection = minecraft.getConnection();
		if (connection == null || minecraft.player == null) {
			return;
		}
		PlayerInfo self = connection.getPlayerInfo(minecraft.player.getUUID());
		if (self == null) {
			return;
		}
		List<PlayerInfo> reordered = new ArrayList<>(cir.getReturnValue());
		if (reordered.remove(self)) {
			reordered.add(0, self);
			cir.setReturnValue(reordered);
		}
	}
}
