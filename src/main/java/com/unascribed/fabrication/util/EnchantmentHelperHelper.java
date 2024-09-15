package com.unascribed.fabrication.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.TranslatableTextContent;

import java.util.Optional;

public class EnchantmentHelperHelper {
	public static Optional<RegistryEntry.Reference<Enchantment>> getEntry(DynamicRegistryManager registries, RegistryKey<Enchantment> enchantment) {
		return registries.get(RegistryKeys.ENCHANTMENT).getEntry(enchantment);
	}

	public static Optional<RegistryEntryList.Named<Enchantment>> getEntryList(DynamicRegistryManager registries, TagKey<Enchantment> tag) {
		return registries.get(RegistryKeys.ENCHANTMENT).getEntryList(tag);
	}

	public static int getLevel(DynamicRegistryManager registries, RegistryKey<Enchantment> enchantment, ItemStack stack) {
		return getEntry(registries, enchantment).map(entry -> EnchantmentHelper.getLevel(entry, stack)).orElse(0);
	}

	public static boolean matches(Object enchantment, RegistryKey<Enchantment> key) {
		return ((Enchantment)enchantment).description().getContent() instanceof TranslatableTextContent content && ("enchantment."+key.getValue().getNamespace()+"."+key.getValue().getPath()).equals(content.getKey());
	}
}
