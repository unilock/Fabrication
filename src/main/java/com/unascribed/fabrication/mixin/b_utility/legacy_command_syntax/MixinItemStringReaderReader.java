package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import com.google.common.base.CharMatcher;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.LegacyIDs;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(targets = "net.minecraft.command.argument.ItemStringReader.Reader")
@EligibleIf(configAvailable="*.legacy_command_syntax")
public class MixinItemStringReaderReader {

	@WrapOperation(at=@At(value="INVOKE", target="Lnet/minecraft/registry/RegistryWrapper$Impl;getOptional(Lnet/minecraft/registry/RegistryKey;)Ljava/util/Optional;"),
			method="readItem()V")
	public Optional<RegistryEntry.Reference<Item>> fabrication$legacyDamageGetOrEmpty(RegistryWrapper.Impl<Item> subject, RegistryKey<Item> rid, Operation<Optional<RegistryEntry.Reference<Item>>> original, @Share("legacyDamage") LocalIntRef legacyDamage) {
		legacyDamage.set(-1);
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
						return Optional.empty();
					}
				}
				if (i.getComponents().contains(DataComponentTypes.MAX_DAMAGE) && metaAsDamage) {
					legacyDamage.set(metaI);
				}
				return subject.getOptional(RegistryKey.of(RegistryKeys.ITEM, LegacyIDs.lookup_id(numIdI, metaI)));
			}
		}
		return original.call(subject, rid);
	}

}
