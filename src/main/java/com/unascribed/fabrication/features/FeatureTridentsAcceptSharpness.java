package com.unascribed.fabrication.features;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.mixin.z_combined.enchantments.AccessorEnchantment;
import com.unascribed.fabrication.mixin.z_combined.enchantments.AccessorEnchantmentDefinition;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.feature.DataPackFeature;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EligibleIf(configAvailable = "*.tridents_accept_sharpness")
public class FeatureTridentsAcceptSharpness extends DataPackFeature {
	public FeatureTridentsAcceptSharpness() {
		super("tridents_accept_sharpness");
	}

	@Override
	public void apply(World world) {
		super.apply(world);
		Agnos.runForDynamicRegistryReload(registries -> {
			Optional<RegistryEntry.Reference<Enchantment>> optional = EnchantmentHelperHelper.getEntry(registries, Enchantments.SHARPNESS);
			if (optional.isPresent()) {
				Enchantment sharpness = optional.get().value();

				// supportedItems
				Object o = sharpness.definition();
				if (!(o instanceof Enchantment.Definition)) return;
				AccessorEnchantmentDefinition accessor = (AccessorEnchantmentDefinition) o;

				List<RegistryEntry<Item>> mutableSupportedItems = new ArrayList<>(sharpness.definition().supportedItems().stream().toList());
				mutableSupportedItems.add(Registries.ITEM.getEntry(Items.TRIDENT));
				accessor.setSupportedItems(RegistryEntryList.of(mutableSupportedItems));

				// primaryItems
				if (sharpness.definition().primaryItems().isPresent()) {
					List<RegistryEntry<Item>> mutablePrimaryItems = new ArrayList<>(sharpness.definition().primaryItems().get().stream().toList());
					mutablePrimaryItems.add(Registries.ITEM.getEntry(Items.TRIDENT));
					accessor.setPrimaryItems(Optional.of(RegistryEntryList.of(mutablePrimaryItems)));
				}

				// exclusiveSet
				Object p = sharpness;
				if (!(p instanceof Enchantment)) return;
				AccessorEnchantment accessorEnchantment = (AccessorEnchantment) p;

				List<RegistryEntry<Enchantment>> mutableExlusiveSet = new ArrayList<>(sharpness.exclusiveSet().stream().toList());
				EnchantmentHelperHelper.getEntry(registries, Enchantments.IMPALING).ifPresent(mutableExlusiveSet::add);
				EnchantmentHelperHelper.getEntry(registries, Enchantments.POWER).ifPresent(mutableExlusiveSet::add);
				accessorEnchantment.setExclusiveSet(RegistryEntryList.of(mutableExlusiveSet));
			}
		});
	}
}
