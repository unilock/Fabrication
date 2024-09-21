package com.unascribed.fabrication.mixin.f_balance.disable_mending;

import com.unascribed.fabrication.FabConf;
import net.minecraft.server.network.ServerPlayerEntity;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Predicate;

@Mixin(ExperienceOrbEntity.class)
@EligibleIf(configAvailable="*.disable_mending")
public class MixinExperienceOrbEntity {

	private static final Predicate<PlayerEntity> fabrication$disableMendingPredicate = ConfigPredicates.getFinalPredicate("*.disable_mending");
	@FabInject(method="repairPlayerGears(Lnet/minecraft/server/network/ServerPlayerEntity;I)I", at=@At("HEAD"), cancellable=true)
	public void no_repair(ServerPlayerEntity player, int amount, CallbackInfoReturnable<Integer> cir) {
		if (FabConf.isEnabled("*.disable_mending") && fabrication$disableMendingPredicate.test(player)) cir.setReturnValue(amount);
	}

}
