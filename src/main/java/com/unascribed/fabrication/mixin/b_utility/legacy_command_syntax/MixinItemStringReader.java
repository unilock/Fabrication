package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import com.google.common.base.CharMatcher;
import com.mojang.brigadier.StringReader;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.LegacyIDs;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStringReader.class)
@EligibleIf(configAvailable="*.legacy_command_syntax")
public class MixinItemStringReader {

	@Unique
	private Integer fabrication$legacyDamage = null;

	@Hijack(target="Lnet/minecraft/registry/RegistryWrapper;getOptional(Lnet/minecraft/registry/RegistryKey;)Ljava/util/Optional;",
			method="readItem()V")
	public HijackReturn fabrication$legacyDamageGetOrEmpty(RegistryWrapper<Item> subject, RegistryKey<Item> rid) {
		fabrication$legacyDamage = null;
		if (FabConf.isEnabled("*.legacy_command_syntax")) {
			String numId;
			String meta;
			Identifier id = rid.getValue();
			if (id.getNamespace().equals("minecraft")) {
				if (!id.getPath().isEmpty() && CharMatcher.digit().matchesAllOf(id.getPath())) {
					numId = id.getPath();
					meta = "0";
				} else {
					numId = null;
					meta = null;
				}
			} else {
				if (!id.getNamespace().isEmpty() && !id.getPath().isEmpty() && CharMatcher.digit().matchesAllOf(id.getNamespace()) && CharMatcher.digit().matchesAllOf(id.getPath())) {
					numId = id.getNamespace();
					meta = id.getPath();
				} else {
					numId = null;
					meta = null;
				}
			}
			if (numId != null) {
				int numIdI = Integer.parseInt(numId);
				int metaI = Integer.parseInt(meta);
				boolean metaAsDamage = false;
				Item i = LegacyIDs.lookup(numIdI, metaI);
				if (i == null) {
					i = LegacyIDs.lookup(numIdI, 0);
					metaAsDamage = true;
					if (i == null) {
						return HijackReturn.OPTIONAL_EMPTY;
					}
				}
				if (i.getComponents().contains(DataComponentTypes.MAX_DAMAGE) && metaAsDamage) {
					fabrication$legacyDamage = metaI;
				}
				return new HijackReturn(subject.getOptional(RegistryKey.of(RegistryKeys.ITEM, LegacyIDs.lookup_id(numIdI, metaI))));
			}
		}
		return null;
	}

	@FabInject(at=@At("RETURN"), method="consume(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/command/argument/ItemStringReader$ItemResult;")
	public void consume(StringReader reader, CallbackInfoReturnable<ItemStringReader.ItemResult> cir) {
		if (fabrication$legacyDamage != null) {
			ItemStringReader.ItemResult result = cir.getReturnValue();
			ComponentChanges.AddedRemovedPair addedRemovedPair = result.components().toAddedRemovedPair();
			ComponentChanges.Builder builder = ComponentChanges.builder();
			for (Component added : addedRemovedPair.added()) {
				if (!DataComponentTypes.DAMAGE.equals(added.type())) {
					builder.add(added.type(), added.value()); // TODO: is this bad?
				}
			}
			for (ComponentType<?> removed : addedRemovedPair.removed()) {
				if (!DataComponentTypes.DAMAGE.equals(removed)) {
					builder.remove(removed);
				}
			}
			builder.add(DataComponentTypes.DAMAGE, fabrication$legacyDamage);
			cir.setReturnValue(new ItemStringReader.ItemResult(result.item(), builder.build()));
		}
	}

}
