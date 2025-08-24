package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import com.mojang.brigadier.context.CommandContext;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.server.command.GameModeCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;

@Mixin(GameModeCommand.class)
@EligibleIf(configAvailable="*.legacy_command_syntax")
public interface AccessorGameModeCommand {
	@Invoker("execute")
	static int fabrication$execute(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets, GameMode gameMode) {
		throw new AssertionError();
	}
}
