package de.deutscha.mod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.deutscha.mod.AnvilNameStore;
import de.deutscha.mod.LegacyColors;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
	private void deutschamod$coloredName(CallbackInfoReturnable<Component> cir) {
		ItemStack self = (ItemStack) (Object) this;
		if (!self.has(DataComponents.CUSTOM_NAME)) {
			return;
		}
		String colored = AnvilNameStore.get(cir.getReturnValue().getString());
		if (colored != null) {
			cir.setReturnValue(LegacyColors.toComponent(colored));
		}
	}
}
