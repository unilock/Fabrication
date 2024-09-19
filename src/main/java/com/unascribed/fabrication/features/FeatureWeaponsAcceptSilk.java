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
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EligibleIf(configAvailable = "*.weapons_accept_silk")
public class FeatureWeaponsAcceptSilk extends DataPackFeature {
	public FeatureWeaponsAcceptSilk() {
		super("weapons_accept_silk");
	}

	@Override
	public void apply(World world) {
		super.apply(world);
		Agnos.runForDynamicRegistryReload(registries -> {
			Optional<RegistryEntry.Reference<Enchantment>> optional = EnchantmentHelperHelper.getEntry(registries, Enchantments.SILK_TOUCH);
			if (optional.isPresent()) {
				Enchantment silkTouch = optional.get().value();

				// supportedItems
				Object o = silkTouch.definition();
				if (!(o instanceof Enchantment.Definition)) return;
				AccessorEnchantmentDefinition accessorDefinition = (AccessorEnchantmentDefinition) o;

				List<RegistryEntry<Item>> mutableSupportedItems = new ArrayList<>(silkTouch.definition().supportedItems().stream().toList());
				mutableSupportedItems.addAll(Registries.ITEM.getOrCreateEntryList(ItemTags.WEAPON_ENCHANTABLE).stream().toList());
				accessorDefinition.setSupportedItems(RegistryEntryList.of(mutableSupportedItems));

				// exclusiveSet
				Object p = silkTouch;
				if (!(p instanceof Enchantment)) return;
				AccessorEnchantment accessorEnchantment = (AccessorEnchantment) p;

				List<RegistryEntry<Enchantment>> mutableExlusiveSet = new ArrayList<>(silkTouch.exclusiveSet().stream().toList());
				EnchantmentHelperHelper.getEntry(registries, Enchantments.LOOTING).ifPresent(mutableExlusiveSet::add);
				accessorEnchantment.setExclusiveSet(RegistryEntryList.of(mutableExlusiveSet));
			}
		});
	}
}
