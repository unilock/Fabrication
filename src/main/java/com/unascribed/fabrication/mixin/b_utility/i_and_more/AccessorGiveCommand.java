package com.unascribed.fabrication.mixin.b_utility.i_and_more;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.server.command.GiveCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;

@Mixin(GiveCommand.class)
@EligibleIf(configAvailable="*.i_and_more")
public interface AccessorGiveCommand {
	@Invoker("execute")
	static int fabrication$execute(ServerCommandSource source, ItemStackArgument item, Collection<ServerPlayerEntity> targets, int count) throws CommandSyntaxException {
		throw new AssertionError();
	}
}
