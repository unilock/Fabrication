package com.unascribed.fabrication.mixin.c_tweaks.no_hunger;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.NoHungerAdd;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.block.BlockState;
import net.minecraft.block.CakeBlock;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(CakeBlock.class)
@EligibleIf(configAvailable="*.no_hunger")
public abstract class MixinCakeBlock {
	private static final Predicate<PlayerEntity> fabrication$noHungerPredicate = ConfigPredicates.getFinalPredicate("*.no_hunger");
	@WrapWithCondition(method="tryEat(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/ActionResult;",
	at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/HungerManager;add(IF)V"))
	private static boolean fabrication$noHunger$eatCake(HungerManager hungerManager, int hunger, float saturation, WorldAccess world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (!(FabConf.isEnabled("*.no_hunger") && hungerManager instanceof NoHungerAdd && fabrication$noHungerPredicate.test(player))) return true;
		((NoHungerAdd)hungerManager).setFabrication$noHungerHeal(hunger, saturation);
		return false;
	}

}
