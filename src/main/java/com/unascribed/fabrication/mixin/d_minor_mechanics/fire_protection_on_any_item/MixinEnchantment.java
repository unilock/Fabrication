package com.unascribed.fabrication.mixin.d_minor_mechanics.fire_protection_on_any_item;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

@Mixin(Enchantment.class)
@EligibleIf(configAvailable="*.fire_protection_on_any_item")
public abstract class MixinEnchantment {

	@FabInject(at=@At("HEAD"), method="isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", cancellable=true)
	public void isAcceptableItem(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
		if (FabConf.isEnabled("*.fire_protection_on_any_item") && EnchantmentHelperHelper.matches(this, "fire_protection") && stack.getItem().isEnchantable(stack)) {
			ci.setReturnValue(true);
		}
	}

}
