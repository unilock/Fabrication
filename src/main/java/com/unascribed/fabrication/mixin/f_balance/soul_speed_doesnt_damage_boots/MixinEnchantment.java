package com.unascribed.fabrication.mixin.f_balance.soul_speed_doesnt_damage_boots;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Enchantment.class)
@EligibleIf(configAvailable="*.soul_speed_doesnt_damage_boots")
public class MixinEnchantment {

	@Shadow
	@Final
	@Mutable
	private ComponentMap effects;

	@FabInject(at=@At("RETURN"), method="<init>")
	private void modify(Text description, Enchantment.Definition definition, RegistryEntryList<Enchantment> exclusiveSet, ComponentMap effects, CallbackInfo ci) {
		if (FabConf.isEnabled("*.soul_speed_doesnt_damage_boots") && EnchantmentHelperHelper.matches(this, Enchantments.SOUL_SPEED)) {
			if (this.effects.contains(EnchantmentEffectComponentTypes.ITEM_DAMAGE)) {
				this.effects = this.effects.filtered(type -> !EnchantmentEffectComponentTypes.ITEM_DAMAGE.equals(type));
			}
		}
	}

}
