package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStringReader.class)
@EligibleIf(configAvailable="*.legacy_command_syntax")
public class MixinItemStringReader {

	@ModifyReturnValue(at=@At("RETURN"), method="consume(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/command/argument/ItemStringReader$ItemResult;")
	public ItemStringReader.ItemResult consume(ItemStringReader.ItemResult result, @Share("legacyDamage") LocalIntRef legacyDamage) {
		if (legacyDamage.get() != -1) {
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
			builder.add(DataComponentTypes.DAMAGE, legacyDamage.get());
			return new ItemStringReader.ItemResult(result.item(), builder.build());
		}
		return result;
	}

}
