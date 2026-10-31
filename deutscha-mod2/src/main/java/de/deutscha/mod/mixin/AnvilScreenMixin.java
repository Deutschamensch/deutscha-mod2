package de.deutscha.mod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import de.deutscha.mod.AnvilNameStore;
import de.deutscha.mod.LegacyColors;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin {
	@ModifyVariable(method = "onNameChanged", at = @At("HEAD"), argsOnly = true)
	private String deutschamod$stripColorCodes(String name) {
		String plain = LegacyColors.strip(name);
		if (!plain.equals(name)) {
			AnvilNameStore.setPending(plain, name);
		}
		return plain;
	}
}
