package com.unascribed.fabrication.mixin.i_woina.no_player_death_animation;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
@EligibleIf(configAvailable="*.no_player_death_animation", envMatches=Env.CLIENT)
public abstract class MixinLivingEntityRenderer {

	@ModifyExpressionValue(method= "setupTransforms(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V", at=@At(value="FIELD", target="net/minecraft/entity/LivingEntity.deathTime:I", opcode=Opcodes.GETFIELD))
	private int fabrication$oldPlayerDeathTime(int old, LivingEntity instance){
		if (FabConf.isEnabled("*.no_player_death_animation") && instance instanceof PlayerEntity) return 0;
		return old;
	}

}
