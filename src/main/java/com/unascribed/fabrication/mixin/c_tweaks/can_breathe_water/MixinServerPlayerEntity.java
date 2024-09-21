package com.unascribed.fabrication.mixin.c_tweaks.can_breathe_water;

import com.mojang.authlib.GameProfile;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(ServerPlayerEntity.class)
@EligibleIf(configAvailable="*.can_breathe_water")
public abstract class MixinServerPlayerEntity extends PlayerEntity {

	private static final Predicate<PlayerEntity> fabrication$canBreatheWaterPredicate = ConfigPredicates.getFinalPredicate("*.can_breathe_water");

	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Inject(at=@At("TAIL"), method="tick()V")
	public void tick(CallbackInfo ci) {
		if (FabConf.isEnabled("*.can_breathe_water") && fabrication$canBreatheWaterPredicate.test(this)) {
			setAir(getMaxAir());
		}
	}

}
