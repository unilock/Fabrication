package com.unascribed.fabrication.features;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.mixin.z_combined.enchantments.AccessorEnchantmentDefinition;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.feature.DataPackFeature;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.util.Optional;

@EligibleIf(configAvailable="*.feather_falling_five")
public class FeatureFeatherFallingFive extends DataPackFeature {
	public FeatureFeatherFallingFive() {
		super("feather_falling_five");
	}

	@Override
	public void apply(MinecraftServer minecraftServer, World world) {
		Agnos.runForDynamicRegistryReload(this.getConfigKey(), registries -> {
			Optional<RegistryEntry.Reference<Enchantment>> optional = EnchantmentHelperHelper.getEntry(registries, Enchantments.FEATHER_FALLING);
			if (optional.isPresent()) {
				Enchantment featherFalling = optional.get().value();

				// maxLevel
				Object o = featherFalling.definition();
				if (!(o instanceof Enchantment.Definition)) return;
				AccessorEnchantmentDefinition accessor = (AccessorEnchantmentDefinition) o;

				accessor.setMaxLevel(5);
			}
		});
		super.apply(minecraftServer, world);
	}

	@Override
	public boolean undo(MinecraftServer minecraftServer, World world) {
		Agnos.undoRunForDynamicRegistryReload(this.getConfigKey());
		return super.undo(minecraftServer, world);
	}
}
