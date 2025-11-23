package com.unascribed.fabrication.mixin.f_balance.disable_prior_work_penalty;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(anyConfigAvailable={"*.disable_prior_work_penalty", "*.anvil_no_xp_cost"})
public class MixinAnvilScreenHandler {

	private static final Predicate<ItemStack> fabrication$disablePriorWorkPenalty = ConfigPredicates.getFinalPredicate("*.disable_prior_work_penalty");
	@WrapOperation(method="updateResult()V", at = @At(value="INVOKE", target="Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
	public Object updateResult(ItemStack stack, ComponentType<?> type, Object value, Operation<Void> original) {
		if (!DataComponentTypes.REPAIR_COST.equals(type)) return original.call(stack, type, value);
		if (!(
				FabConf.isEnabled("*.disable_prior_work_penalty") && fabrication$disablePriorWorkPenalty.test(stack)
				|| FabConf.isEnabled("*.anvil_no_xp_cost")
		)) return original.call(stack, type, value);
		return original.call(stack, type, 0);
	}

}
