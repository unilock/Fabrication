package com.unascribed.fabrication.mixin.i_woina.no_experience;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.unascribed.fabrication.FabConf;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentScreenHandler.class)
@EligibleIf(configAvailable="*.no_experience")
public class MixinEnchantmentScreenHandler {

	@ModifyExpressionValue(at=@At(value="FIELD", target="net/minecraft/entity/player/PlayerEntity.experienceLevel:I", opcode=Opcodes.GETFIELD),
			method={
					"onButtonClick(Lnet/minecraft/entity/player/PlayerEntity;I)Z",
					/*"onContentChanged(Lnet/minecraft/inventory/Inventory;)V"*/
	})
	private int fabrication$amendExperienceLevel(int old) {
		if (FabConf.isEnabled("*.no_experience")) return 65535;
		return old;
	}

}
