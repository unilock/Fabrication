package com.unascribed.fabrication.mixin.e_mechanics.fire_resistance_two;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.fire_resistance_two")
public abstract class MixinLivingEntity extends Entity {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@ModifyReturn(method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", target="Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z")
	private static boolean fabrication$fireResistTwo(boolean hasEffect, LivingEntity self, RegistryEntry<StatusEffect> effect, LivingEntity selfAgain, DamageSource source) {
		if (!FabConf.isEnabled("*.fire_resistance_two")) return hasEffect;
		if (hasEffect && effect.matches(StatusEffects.FIRE_RESISTANCE) && source.isOf(DamageTypes.LAVA)) {
			StatusEffectInstance instance = self.getStatusEffect(StatusEffects.FIRE_RESISTANCE);
			return instance == null || instance.getAmplifier() >= 1;
		}
		return hasEffect;
	}
}
