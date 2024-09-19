package com.unascribed.fabrication.util.forgery_nonsense;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.entry.RegistryEntry;

public class ForgeryStatusEffect {
	//This exists because forgery is jank
	public static StatusEffectInstance get(RegistryEntry<StatusEffect> type, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
		return new StatusEffectInstance(type, duration, amplifier, ambient, showParticles, showIcon);
	}
}
