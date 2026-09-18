package com.unascribed.fabrication.mixin.f_balance.environmentally_friendly_creepers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(World.class)
@EligibleIf(configAvailable="*.environmentally_friendly_creepers")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public class MixinWorld {

	@WrapOperation(at=@At(value="INVOKE", target="Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"),
			method="createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;ZLnet/minecraft/particle/ParticleEffect;Lnet/minecraft/particle/ParticleEffect;Lnet/minecraft/registry/entry/RegistryEntry;)Lnet/minecraft/world/explosion/Explosion;")
	private static boolean fabrication$nonMobGriefingDestructionType(GameRules rules, GameRules.Key<GameRules.BooleanRule> gamerule, Operation<Boolean> original, Entity entity) {
		return FabConf.isEnabled("*.environmentally_friendly_creepers") && entity instanceof CreeperEntity && gamerule == GameRules.DO_MOB_GRIEFING ? false : original.call(rules, gamerule);
	}

}
