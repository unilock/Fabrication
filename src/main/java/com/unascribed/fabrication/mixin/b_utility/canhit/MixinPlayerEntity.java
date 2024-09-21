package com.unascribed.fabrication.mixin.b_utility.canhit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.canhit")
public abstract class MixinPlayerEntity extends LivingEntity {

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@WrapOperation(at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;isAttackable()Z"), method="attack(Lnet/minecraft/entity/Entity;)V")
	private boolean fabrication$canHit(Entity entity, Operation<Boolean> original) {
		if (!FabConf.isEnabled("*.canhit") || CanHitUtil.isExempt(this)) return original.call(entity);
		if (!entity.isAttackable()) return false;
		ItemStack stack = this.getStackInHand(Hand.MAIN_HAND);
		return CanHitUtil.canHit(stack, entity);
	}


}
