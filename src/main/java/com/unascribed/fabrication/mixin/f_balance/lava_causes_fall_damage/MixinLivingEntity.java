package com.unascribed.fabrication.mixin.f_balance.lava_causes_fall_damage;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.lava_causes_fall_damage")
public abstract class MixinLivingEntity extends Entity {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	private static final Predicate<LivingEntity> fabrication$lavaFallDamagePredicate = ConfigPredicates.getFinalPredicate("*.lava_causes_fall_damage");

	@ModifyVariable(method="fall(DZLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V", at=@At("HEAD"), argsOnly=true)
	public boolean fabrication$treatLavaAsGround(boolean onGround) {
		if (!FabConf.isEnabled("*.lava_causes_fall_damage")) return onGround;
		if (!this.getWorld().isClient && this.fallDistance > 6F && this.updateMovementInFluid(FluidTags.LAVA, 0.014D)) {
			if (fabrication$lavaFallDamagePredicate.test((LivingEntity)(Object) this)) {
				this.fallDistance /= 2F;
				this.setVelocity(this.getVelocity().multiply(.4));
				Object self = this;
				if (self instanceof ServerPlayerEntity && ((ServerPlayerEntity) self).networkHandler.isConnectionOpen()) {
					((ServerPlayerEntity) self).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(this));
				}
				return true;
			}
		}
		return onGround;
	}
}
