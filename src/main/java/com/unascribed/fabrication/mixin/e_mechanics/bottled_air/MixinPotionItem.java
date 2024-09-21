package com.unascribed.fabrication.mixin.e_mechanics.bottled_air;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
@EligibleIf(configAvailable="*.bottled_air")
public class MixinPotionItem {

	@Inject(at=@At("RETURN"), method="finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;", cancellable=true)
	public void finishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> ci) {
		if (FabConf.isEnabled("*.bottled_air") && ci.getReturnValue().getItem() == Items.GLASS_BOTTLE && user.isSubmergedInWater()) {
			ci.setReturnValue(PotionContentsComponent.createStack(Items.POTION, Potions.WATER));
		}
	}

	@ModifyReturnValue(at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"),
			method="finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;")
	private boolean fabrication$bottledAir(boolean original, PlayerInventory inv, ItemStack stack) {
		if (FabConf.isEnabled("*.bottled_air") && stack.getItem() == Items.GLASS_BOTTLE && inv.player.isSubmergedInWater()) {
			return inv.insertStack(PotionContentsComponent.createStack(Items.POTION, Potions.WATER));
		}
		return original;
	}

}
