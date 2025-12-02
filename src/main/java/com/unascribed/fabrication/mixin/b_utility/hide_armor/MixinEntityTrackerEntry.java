package com.unascribed.fabrication.mixin.b_utility.hide_armor;

import com.mojang.datafixers.util.Pair;
import com.unascribed.fabrication.features.FeatureHideArmor;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.EntityTrackerEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(EntityTrackerEntry.class)
@EligibleIf(configAvailable="*.hide_armor")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public class MixinEntityTrackerEntry {

	@Shadow @Final
	private Entity entity;

	@ModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/network/packet/s2c/play/EntityEquipmentUpdateS2CPacket;<init>(ILjava/util/List;)V"),
			method="sendPackets(Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V")
	public List<Pair<EquipmentSlot, ItemStack>> constructUpdatePacket(List<Pair<EquipmentSlot, ItemStack>> equipmentList) {
		return FeatureHideArmor.muddle(entity, equipmentList);
	}

}
