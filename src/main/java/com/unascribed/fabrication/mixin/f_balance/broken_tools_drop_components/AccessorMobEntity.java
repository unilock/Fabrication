package com.unascribed.fabrication.mixin.f_balance.broken_tools_drop_components;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MobEntity.class)
public interface AccessorMobEntity {
	@Invoker("getDropChance")
	float fabrication$getDropChance(EquipmentSlot slot);
}
