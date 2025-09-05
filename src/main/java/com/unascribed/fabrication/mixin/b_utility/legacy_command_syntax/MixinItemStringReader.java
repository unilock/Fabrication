package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.StringReader;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.util.ItemStringReaderReaderReader;
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

	private Integer fabrication$legacyDamage = null;

	@WrapOperation(method="consume(Lcom/mojang/brigadier/StringReader;Lnet/minecraft/command/argument/ItemStringReader$Callbacks;)V", at=@At(value="INVOKE", target="Lnet/minecraft/command/argument/ItemStringReader$Reader;read()V"))
	public void consume(ItemStringReader.Reader subject, Operation<Void> original) {
		original.call(subject);
		fabrication$legacyDamage = ((ItemStringReaderReaderReader) subject).fabrication$getLegacyDamage();
	}

	@ModifyReturnValue(at=@At("RETURN"), method="consume(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/command/argument/ItemStringReader$ItemResult;")
	public ItemStringReader.ItemResult consume(ItemStringReader.ItemResult result, StringReader reader) {
		if (fabrication$legacyDamage != null) {
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
			return new ItemStringReader.ItemResult(result.item(), builder.build());
		}
		return result;
	}

}
