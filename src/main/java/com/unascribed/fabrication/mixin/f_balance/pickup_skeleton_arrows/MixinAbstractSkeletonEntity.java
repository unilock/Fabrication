package com.unascribed.fabrication.mixin.f_balance.pickup_skeleton_arrows;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;

@Mixin(AbstractSkeletonEntity.class)
@EligibleIf(configAvailable="*.pickup_skeleton_arrows")
public abstract class MixinAbstractSkeletonEntity {
	@FabInject(at=@At("RETURN"), method="createArrowProjectile(Lnet/minecraft/item/ItemStack;FLnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;", cancellable=true)
	public void createArrowProjectile(ItemStack arrow, float damageModifier, ItemStack shotFrom, CallbackInfoReturnable<PersistentProjectileEntity> cir) {
		if(!FabConf.isEnabled("*.pickup_skeleton_arrows")) return;

		PersistentProjectileEntity arrowEntity = cir.getReturnValue();
		arrowEntity.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
		cir.setReturnValue(arrowEntity);
	}
}
