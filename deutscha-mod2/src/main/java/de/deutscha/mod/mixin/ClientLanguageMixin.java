package de.deutscha.mod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.deutscha.mod.RainbowText;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

@Mixin(ClientLanguage.class)
public abstract class ClientLanguageMixin {
	@Inject(method = "getVisualOrder", at = @At("RETURN"), cancellable = true)
	private void deutschamod$rainbowName(FormattedText text, CallbackInfoReturnable<FormattedCharSequence> cir) {
		cir.setReturnValue(RainbowText.apply(cir.getReturnValue()));
	}
}
