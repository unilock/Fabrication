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
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EligibleIf(configAvailable = "*.tridents_accept_power")
public class FeatureTridentsAcceptPower extends DataPackFeature {
	public FeatureTridentsAcceptPower() {
		super("tridents_accept_power");
	}

	@Override
	public void apply(MinecraftServer server) {
		Agnos.runForDynamicRegistryReload(this.getConfigKey(), registries -> {
			Optional<RegistryEntry.Reference<Enchantment>> optional = EnchantmentHelperHelper.getEntry(registries, Enchantments.POWER);
			if (optional.isPresent()) {
				Enchantment power = optional.get().value();

				// supportedItems
				Object o = power.definition();
				if (!(o instanceof Enchantment.Definition)) return;
				AccessorEnchantmentDefinition accessor = (AccessorEnchantmentDefinition) o;

				List<RegistryEntry<Item>> mutableSupportedItems = new ArrayList<>(power.definition().supportedItems().stream().toList());
				mutableSupportedItems.add(Registries.ITEM.getEntry(Items.TRIDENT));
				accessor.setSupportedItems(RegistryEntryList.of(mutableSupportedItems));

				// exclusiveSet
				Object p = power;
				if (!(p instanceof Enchantment)) return;
				AccessorEnchantment accessorEnchantment = (AccessorEnchantment) p;

				List<RegistryEntry<Enchantment>> mutableExlusiveSet = new ArrayList<>(power.exclusiveSet().stream().toList());
				EnchantmentHelperHelper.getEntryList(registries, EnchantmentTags.DAMAGE_EXCLUSIVE_SET).ifPresent(entries -> mutableExlusiveSet.addAll(entries.stream().toList()));
				accessorEnchantment.setExclusiveSet(RegistryEntryList.of(mutableExlusiveSet));
			}
		});
		super.apply(server);
	}

	@Override
	public boolean undo(MinecraftServer server) {
		Agnos.undoRunForDynamicRegistryReload(this.getConfigKey());
		return super.undo(server);
	}
}
