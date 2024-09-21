package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(RegistryEntryArgumentType.class)
@EligibleIf(configAvailable="*.legacy_command_syntax")
public class MixinEntitySummonArgumentType {
	@Inject(at=@At("TAIL"), method= "<init>(Lnet/minecraft/command/CommandRegistryAccess;Lnet/minecraft/registry/RegistryKey;Lcom/mojang/serialization/Codec;)V")
	public void legacyCommandInput(CommandRegistryAccess registryAccess, RegistryKey key, Codec codec, CallbackInfo ci, @Share("isNotEntityArgument") LocalBooleanRef isNotEntityArgument) {
		if (key == RegistryKeys.ENTITY_TYPE) {
			isNotEntityArgument.set(false);
		} else {
			isNotEntityArgument.set(true);
		}

	}
	@WrapOperation(method="parseAsNbt(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/nbt/NbtElement;",
			at=@At(value="INVOKE", target="Lnet/minecraft/util/Identifier;fromCommandInput(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/util/Identifier;"))
	private static Identifier legacyCommandInput(StringReader sr, Operation<Identifier> original, @Share("isNotEntityArgument") LocalBooleanRef isNotEntityArgument) {
		Identifier id = original.call(sr);
		if (isNotEntityArgument.get()) return id;
		if (!FabConf.isEnabled("*.legacy_command_syntax")) return id;
		char peek = sr.peek();
		if (peek >= 'A' && peek <= 'Z') {
			int start = sr.getCursor();
			while (sr.canRead() && fabrication$isCharValid(sr.peek())) {
				sr.skip();
			}
			if (!sr.canRead()) {
				return Identifier.of("minecraft", sr.getString().substring(start, sr.getCursor())
					.replaceAll("([a-z])([A-Z])", "$1_$2")
					.toLowerCase(Locale.ROOT));
			}
		}
		return id;
	}

	@Unique
	private static boolean fabrication$isCharValid(char c) {
		return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}

}
