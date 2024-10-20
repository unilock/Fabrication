package com.unascribed.fabrication.mixin.i_woina.instant_eat;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(Item.class)
@EligibleIf(configAvailable="*.instant_eat")
public class MixinItem {

	@FabInject(method= "getMaxUseTime(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)I", at=@At(value="HEAD"), cancellable = true)
	private void getMaxUseTime(ItemStack stack, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
		if (FabConf.isEnabled("*.instant_eat") && stack.get(DataComponentTypes.FOOD) != null) cir.setReturnValue(1);
	}
}
