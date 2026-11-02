package de.deutscha.mod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import de.deutscha.mod.LegacyColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;

@Mixin(EditBox.class)
public abstract class EditBoxMixin {
	@ModifyVariable(method = "insertText", at = @At("HEAD"), argsOnly = true, require = 0)
	private String deutschamod$pastedGradient(String text) {
		if (Minecraft.getInstance().screen instanceof AnvilScreen) {
			return LegacyColors.collapsePastedGradient(text);
		}
		return text;
	}
}
