package com.unascribed.fabrication.mixin.f_balance.bedrock_impaling;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.enchantment.effect.value.AddEnchantmentEffect;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityTypePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Mixin(Enchantment.class)
@EligibleIf(configAvailable = "*.bedrock_impaling")
public class MixinEnchantment {

	@Shadow
	@Final
	@Mutable
	private ComponentMap effects;

	@FabInject(at=@At("RETURN"), method="<init>")
	private void modify(Text description, Enchantment.Definition definition, RegistryEntryList<Enchantment> exclusiveSet, ComponentMap effects, CallbackInfo ci) {
		if (FabConf.isEnabled("*.bedrock_impaling") && EnchantmentHelperHelper.matches(this, Enchantments.IMPALING)) {
			if (this.effects.contains(EnchantmentEffectComponentTypes.DAMAGE)) {
				List<EnchantmentEffectEntry<EnchantmentValueEffect>> mutableEffects = new ArrayList<>(this.effects.get(EnchantmentEffectComponentTypes.DAMAGE));

				mutableEffects.removeIf(entry ->
					entry.effect().getCodec() == AddEnchantmentEffect.CODEC
					&& entry.requirements().isPresent()
					&& entry.requirements().get() instanceof EntityPropertiesLootCondition condition
					&& condition.predicate().isPresent()
					&& condition.predicate().get().type().isPresent()
					&& new HashSet<>(condition.predicate().get().type().get().types().stream().toList())
						.equals(new HashSet<>(Registries.ENTITY_TYPE.getEntryList(EntityTypeTags.SENSITIVE_TO_IMPALING).orElseThrow().stream().toList()))
				);

				mutableEffects.add(new EnchantmentEffectEntry<>(
					new AddEnchantmentEffect(EnchantmentLevelBasedValue.linear(2.5F, 2.5F)),
					// TODO: Ye gods...
					Optional.of(new EntityPropertiesLootCondition(Optional.of(EntityPredicate.Builder.create().type(EntityTypePredicate.create(EntityTypeTags.SENSITIVE_TO_IMPALING)).build()), LootContext.EntityTarget.THIS) {
						@Override
						public boolean test(LootContext lootContext) {
							Entity entity = lootContext.get(this.entity().getParameter());
							return entity != null && (entity.getType().isIn(EntityTypeTags.SENSITIVE_TO_IMPALING) || entity.isWet());
						}
					})
				));

				ComponentMap.Builder builder = ComponentMap.builder();
				builder.add(EnchantmentEffectComponentTypes.DAMAGE, mutableEffects);
				this.effects = ComponentMap.of(this.effects, builder.build());
			}
		}
	}

}
