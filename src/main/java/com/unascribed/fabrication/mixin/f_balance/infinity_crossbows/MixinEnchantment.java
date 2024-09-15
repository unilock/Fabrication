package com.unascribed.fabrication.mixin.f_balance.infinity_crossbows;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.mixin.z_combined.enchantments.AccessorEnchantmentDefinition;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.component.ComponentMap;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

@Mixin(Enchantment.class)
@EligibleIf(anyConfigAvailable={"*.infinity_crossbows", "*.infinity_crossbows_modded"})
public abstract class MixinEnchantment {

	@Shadow
	@Final
	private Enchantment.Definition definition;

	@FabInject(at=@At("RETURN"), method="<init>")
	private void modify(Text description, Enchantment.Definition definition, RegistryEntryList<Enchantment> exclusiveSet, ComponentMap effects, CallbackInfo ci) {
		if (FabConf.isAnyEnabled("*.infinity_crossbows") && EnchantmentHelperHelper.matches(this, Enchantments.INFINITY)) {
			Object o = this.definition;
			if (!(o instanceof Enchantment.Definition)) return;
			AccessorEnchantmentDefinition accessor = (AccessorEnchantmentDefinition) o;

			List<RegistryEntry<Item>> mutableSupportedItems = new ArrayList<>(this.definition.supportedItems().stream().toList());
			if (FabConf.isAnyEnabled("*.infinity_crossbows_modded")) {
				mutableSupportedItems.addAll(Registries.ITEM.getOrCreateEntryList(ItemTags.CROSSBOW_ENCHANTABLE).stream().toList());
			} else {
				mutableSupportedItems.add(Registries.ITEM.getEntry(Items.CROSSBOW));
			}
			accessor.setSupportedItems(RegistryEntryList.of(mutableSupportedItems));
		}
	}

}
