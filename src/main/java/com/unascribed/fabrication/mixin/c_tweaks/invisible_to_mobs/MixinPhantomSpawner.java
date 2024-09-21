package com.unascribed.fabrication.mixin.c_tweaks.invisible_to_mobs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(PhantomSpawner.class)
@EligibleIf(anyConfigAvailable={"*.no_phantoms", "*.invisible_to_mobs"})
public class MixinPhantomSpawner {

	private static final Predicate<PlayerEntity> fabrication$noPhantomsPredicate = ConfigPredicates.getFinalPredicate("*.no_phantoms");
	private static final Predicate<PlayerEntity> fabrication$invisMobsPredicate = ConfigPredicates.getFinalPredicate("*.invisible_to_mobs");

	@ModifyReturnValue(method="spawn(Lnet/minecraft/server/world/ServerWorld;ZZ)I", at=@At(value="INVOKE", target="Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
	private boolean fabrication$InvisTaggablePlayersIsSpectator(boolean original, PlayerEntity subject) {
		if (FabConf.isEnabled("*.invisible_to_mobs") && fabrication$invisMobsPredicate.test(subject)) return true;
		if (FabConf.isEnabled("*.no_phantoms") && fabrication$noPhantomsPredicate.test(subject)) return true;
		return original;
	}

}
