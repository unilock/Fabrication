package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.brigadier.StringReader;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStringReader.class)
@EligibleIf(configAvailable="*.legacy_command_syntax")
public class MixinItemStringReader {

	@Inject(at=@At("RETURN"), method="consume(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/command/argument/ItemStringReader$ItemResult;")
	public void consume(StringReader reader, CallbackInfoReturnable<ItemStringReader.ItemResult> cir, @Share("legacyDamage") LocalIntRef legacyDamage) {
		if (legacyDamage.get() != -1) {
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
			builder.add(DataComponentTypes.DAMAGE, legacyDamage.get());
			cir.setReturnValue(new ItemStringReader.ItemResult(result.item(), builder.build()));
		}
	}

}
