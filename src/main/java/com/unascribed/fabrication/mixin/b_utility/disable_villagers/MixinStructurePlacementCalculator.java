package com.unascribed.fabrication.mixin.b_utility.disable_villagers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureSetKeys;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

@Mixin(StructurePlacementCalculator.class)
@EligibleIf(configAvailable="*.disable_villagers")
public abstract class MixinStructurePlacementCalculator {
	@Unique
	private static boolean fabrication$testForVillages(RegistryEntry<StructureSet> structureSetRegistryEntry) {
		return structureSetRegistryEntry.matchesKey(StructureSetKeys.VILLAGES);
	}
	@WrapOperation(method="create(Lnet/minecraft/world/gen/noise/NoiseConfig;JLnet/minecraft/world/biome/source/BiomeSource;Ljava/util/stream/Stream;)Lnet/minecraft/world/gen/chunk/placement/StructurePlacementCalculator;",
	at=@At(value="INVOKE", target="Ljava/util/stream/Stream;toList()Ljava/util/List;"))
	private static List<RegistryEntry<StructureSet>> fabrication$disableVillages1(Stream<RegistryEntry<StructureSet>> instance, Operation<List<RegistryEntry<StructureSet>>> original) {
		List<RegistryEntry<StructureSet>> list = original.call(instance);
		if (!FabConf.isEnabled("*.disable_villagers")) return list;
		(list = new ArrayList<>(list)).removeIf(MixinStructurePlacementCalculator::fabrication$testForVillages);
		return list;
	}
	@WrapOperation(method="create(Lnet/minecraft/world/gen/noise/NoiseConfig;JLnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/registry/RegistryWrapper;)Lnet/minecraft/world/gen/chunk/placement/StructurePlacementCalculator;",
		at=@At(value="INVOKE", target="Ljava/util/stream/Stream;collect(Ljava/util/stream/Collector;)Ljava/lang/Object;"))
	private static Object fabrication$disableVillages2(Stream instance, Collector collector, Operation<List<RegistryEntry<StructureSet>>> original) {
		List<RegistryEntry<StructureSet>> list = original.call(instance, collector);
		if (!FabConf.isEnabled("*.disable_villagers")) return list;
		(list = new ArrayList<>(list)).removeIf(MixinStructurePlacementCalculator::fabrication$testForVillages);
		return list;
	}
}
