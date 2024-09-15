package com.unascribed.fabrication.mixin.b_utility.weapons_accept_silk;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.mixin.z_combined.enchantments.AccessorEnchantmentDefinition;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.component.ComponentMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Enchantment.class)
@EligibleIf(configAvailable="*.weapons_accept_silk")
public class MixinEnchantment {

	@Shadow
	@Final
	private Enchantment.Definition definition;

	@Shadow
	@Final
	@Mutable
	private RegistryEntryList<Enchantment> exclusiveSet;

	@FabInject(at=@At("RETURN"), method="<init>")
	private void modify(Text description, Enchantment.Definition definition, RegistryEntryList<Enchantment> exclusiveSet, ComponentMap effects, CallbackInfo ci) {
		if (FabConf.isAnyEnabled("*.weapons_accept_silk") && EnchantmentHelperHelper.matches(this, Enchantments.SILK_TOUCH)) {
			// supportedItems
			Object o = this.definition;
			if (!(o instanceof Enchantment.Definition)) return;
			AccessorEnchantmentDefinition accessor = (AccessorEnchantmentDefinition) o;

			List<RegistryEntry<Item>> mutableSupportedItems = new ArrayList<>(this.definition.supportedItems().stream().toList());
			mutableSupportedItems.addAll(Registries.ITEM.getOrCreateEntryList(ItemTags.WEAPON_ENCHANTABLE).stream().toList());
			accessor.setSupportedItems(RegistryEntryList.of(mutableSupportedItems));

			// exclusiveSet
			List<RegistryEntry<Enchantment>> mutableExlusiveSet = new ArrayList<>(this.exclusiveSet.stream().toList());
			EnchantmentHelperHelper.getEntry(world.getRegistryManager(), Enchantments.LOOTING).ifPresent(mutableExlusiveSet::add);
			this.exclusiveSet = RegistryEntryList.of(mutableExlusiveSet);
		}
	}

}
