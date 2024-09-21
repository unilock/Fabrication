package com.unascribed.fabrication.mixin.d_minor_mechanics.furnace_minecart_any_fuel;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

@Mixin(FurnaceMinecartEntity.class)
@EligibleIf(configAvailable="*.furnace_minecart_any_fuel")
public abstract class MixinFurnaceMinecartEntity extends AbstractMinecartEntity {

	protected MixinFurnaceMinecartEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Shadow
	private int fuel;

	@Inject(at=@At("HEAD"), method="interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;")
	public void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
		if (!FabConf.isEnabled("*.furnace_minecart_any_fuel")) return;
		ItemStack itemStack = player.getStackInHand(hand);
		if (FurnaceBlockEntity.canUseAsFuel(itemStack)) {
			int value = FurnaceBlockEntity.createFuelTimeMap().get(itemStack.getItem())*2;
			if (this.fuel + value <= 32000) {
				if (!player.getAbilities().creativeMode) {
					itemStack.decrement(1);
				}
				this.fuel += value;
			}
		}
	}

	@ModifyExpressionValue(at=@At(value="INVOKE", target="Lnet/minecraft/recipe/Ingredient;test(Lnet/minecraft/item/ItemStack;)Z"),
			method="interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;")
	private boolean fabrication$disableVanillaFuel(boolean original) {
		return !FabConf.isEnabled("*.furnace_minecart_any_fuel") && original;
	}

}
