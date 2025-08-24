package com.unascribed.fabrication.mixin.g_weird_tweaks.encroaching_emeralds;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GenerationSettings.LookupBackedBuilder.class)
@EligibleIf(configAvailable="*.encroaching_emeralds")
public interface AccessorGenerationSettingsLookupBackedBuilder {
	@Accessor
	RegistryEntryLookup<PlacedFeature> getPlacedFeatureLookup();
}

