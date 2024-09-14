package com.unascribed.fabrication.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.TranslatableTextContent;

public class EnchantmentHelperHelper {
	public static int getLevel(DynamicRegistryManager registries, RegistryKey<Enchantment> enchantment, ItemStack stack) {
		return registries.get(RegistryKeys.ENCHANTMENT).getEntry(enchantment).map(entry -> EnchantmentHelper.getLevel(entry, stack)).orElse(0);
	}

	public static boolean matches(Object enchantment, String description) {
		return ((Enchantment)enchantment).description().getContent() instanceof TranslatableTextContent content && ("enchantment.minecraft."+description).equals(content.getKey());
	}
}
