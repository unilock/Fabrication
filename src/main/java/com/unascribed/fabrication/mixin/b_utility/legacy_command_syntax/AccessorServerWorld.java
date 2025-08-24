package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerWorld.class)
@EligibleIf(configAvailable="*.legacy_command_syntax")
public interface AccessorServerWorld {
	@Accessor
	ServerWorldProperties getWorldProperties();
}
