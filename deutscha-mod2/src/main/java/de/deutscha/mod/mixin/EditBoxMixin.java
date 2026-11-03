package de.deutscha.mod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import de.deutscha.mod.AnvilNameStore;
import de.deutscha.mod.LegacyColors;
import net.minecraft.client.gui.components.EditBox;

@Mixin(EditBox.class)
public abstract class EditBoxMixin {
	@ModifyVariable(method = "insertText", at = @At("HEAD"), argsOnly = true, require = 0)
	private String deutschamod$pastedGradient(String text) {
		if (AnvilNameStore.isAnvilOpen()) {
			return LegacyColors.collapsePastedGradient(text);
		}
		return text;
	}
}
