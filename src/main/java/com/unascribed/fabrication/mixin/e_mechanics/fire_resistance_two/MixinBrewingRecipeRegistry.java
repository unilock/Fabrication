package com.unascribed.fabrication.mixin.e_mechanics.fire_resistance_two;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(BrewingRecipeRegistry.class)
@EligibleIf(configAvailable="*.fire_resistance_two")
public abstract class MixinBrewingRecipeRegistry {

	@Inject(method="hasRecipe(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", at=@At("HEAD"), cancellable=true)
	private void fabrication$fireResistTwoRecipe(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.fire_resistance_two")) return;
		if (ingredient.isOf(Items.GLOWSTONE_DUST) && input.contains(DataComponentTypes.POTION_CONTENTS) && input.get(DataComponentTypes.POTION_CONTENTS).matches(Potions.FIRE_RESISTANCE)) {
			cir.setReturnValue(true);
		}
	}
	@Inject(method="craft(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", at=@At("HEAD"), cancellable=true)
	private void fabrication$fireResistTwoCraft(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
		if (!FabConf.isEnabled("*.fire_resistance_two")) return;
		if (ingredient.isOf(Items.GLOWSTONE_DUST) && input.contains(DataComponentTypes.POTION_CONTENTS) && input.get(DataComponentTypes.POTION_CONTENTS).matches(Potions.FIRE_RESISTANCE)) {
			ItemStack ret = Items.POTION.getDefaultStack();
			ret.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(StatusEffects.FIRE_RESISTANCE.value().getColor()), List.of(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 3600, 1))));
			ret.set(DataComponentTypes.CUSTOM_NAME, Text.of("Potion of Lava Resistance"));
			cir.setReturnValue(ret);
		}
	}
}
