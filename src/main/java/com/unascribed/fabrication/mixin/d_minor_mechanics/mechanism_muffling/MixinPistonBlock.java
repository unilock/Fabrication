package com.unascribed.fabrication.mixin.d_minor_mechanics.mechanism_muffling;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.MechanismMuffling;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonBlock.class)
@EligibleIf(configAvailable="*.mechanism_muffling")
public class MixinPistonBlock {

	@WrapWithCondition(at=@At(value="INVOKE", target="Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"),
			method="onSyncedBlockEvent(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;II)Z")
	private boolean fabrication$muffleSound(World subject, PlayerEntity source, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
		return !(FabConf.isEnabled("*.mechanism_muffling") && MechanismMuffling.isMuffled(subject, pos));
	}

}
