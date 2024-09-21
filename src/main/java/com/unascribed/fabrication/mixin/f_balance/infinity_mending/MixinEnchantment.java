package com.unascribed.fabrication.mixin.f_balance.infinity_mending;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import org.spongepowered.asm.mixin.injection.Inject;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.component.ComponentMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
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
@EligibleIf(configAvailable="*.infinity_mending")
public abstract class MixinEnchantment {

	@Shadow
	@Final
	@Mutable
	private RegistryEntryList<Enchantment> exclusiveSet;

	@Inject(at=@At("RETURN"), method="<init>")
	private void modify(Text description, Enchantment.Definition definition, RegistryEntryList<Enchantment> exclusiveSet, ComponentMap effects, CallbackInfo ci) {
		if (FabConf.isEnabled("*.infinity_mending") && EnchantmentHelperHelper.matches(this, Enchantments.INFINITY)) {
			List<RegistryEntry<Enchantment>> mutableExlusiveSet = new ArrayList<>(this.exclusiveSet.stream().toList());
			mutableExlusiveSet.removeIf(entry -> entry.matches(Enchantments.INFINITY::equals));
			mutableExlusiveSet.removeIf(entry -> entry.matches(Enchantments.MENDING::equals));
			this.exclusiveSet = RegistryEntryList.of(mutableExlusiveSet);
		}
	}

}
