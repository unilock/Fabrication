package com.unascribed.fabrication.mixin.d_minor_mechanics.protection_on_any_item;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
@EligibleIf(configAvailable="*.protection_on_any_item")
public abstract class MixinEnchantment {

	@FabInject(at=@At("HEAD"), method="isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", cancellable=true)
	public void isAcceptableItem(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
		if (FabConf.isEnabled("*.protection_on_any_item") && EnchantmentHelperHelper.matches(this, Enchantments.PROTECTION) && stack.getItem().isEnchantable(stack)) {
			ci.setReturnValue(true);
		}
	}

}
