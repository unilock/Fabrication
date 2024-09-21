package com.unascribed.fabrication.mixin.z_combined.old_armor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.AbstractMap;
import java.util.function.Predicate;
import java.util.stream.Collector;

@Mixin(LivingEntity.class)
@EligibleIf(anyConfigAvailable={"*.old_armor_scale", "*.old_armor"})
public abstract class MixinLivingEntity extends Entity {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	private static final Predicate<LivingEntity> fabrication$oldArmorScalePredicate = ConfigPredicates.getFinalPredicate("*.old_armor_scale");
	private static final Predicate<LivingEntity> fabrication$oldArmorPredicate = ConfigPredicates.getFinalPredicate("*.old_armor");
//	@ModifyReturnValue(at=@At(value="INVOKE", target="Lnet/minecraft/item/ItemStack;getAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;"),
//			method="getEquipmentChanges()Ljava/util/Map;")
//	private Multimap<EntityAttribute, EntityAttributeModifier> fabrication$oldArmor(Multimap<EntityAttribute, EntityAttributeModifier> map, ItemStack stack, EquipmentSlot slot, LivingEntity self) {
//		final boolean scale = FabConf.isEnabled("*.old_armor_scale") && fabrication$oldArmorScalePredicate.test(self);
//		final boolean old = FabConf.isEnabled("*.old_armor") && fabrication$oldArmorPredicate.test(self);
//		if (!(((scale && stack.isDamageable()) || old) && stack.getItem() instanceof ArmorItem && ((ArmorItem)stack.getItem()).getSlotType() == slot)) return map;
//		return map.entries().stream().map(
//				entry ->
//					(entry.getKey() == EntityAttributes.GENERIC_ARMOR && entry.getValue().operation() == EntityAttributeModifier.Operation.ADD_VALUE ?
//							new AbstractMap.SimpleEntry<>(
//									entry.getKey(),
//									new EntityAttributeModifier(
//											entry.getValue().id(),
//											(old ? ArmorMaterials.DIAMOND.value().getProtection(((ArmorItem)stack.getItem()).getType()) : entry.getValue().value())
//													* (scale ? ((stack.getMaxDamage() - stack.getDamage()) / (double) stack.getMaxDamage()) : 1),
//											EntityAttributeModifier.Operation.ADD_VALUE))
//							: new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()))
//				).collect(Collector.of(ArrayListMultimap::create, (m, entry) ->m.put(entry.getKey(), entry.getValue()), (m1, m2) -> {m1.putAll(m2); return m1;}));
//
//	}
}
