package com.unascribed.fabrication.mixin.i_woina.no_swim;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.no_swim")
public class MixinEntity {

	private static final Predicate<Entity> fabrication$noSwimPredicate = ConfigPredicates.getFinalPredicate("*.no_swim");
	@WrapOperation(method="updateSwimming()V", at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;isSprinting()Z"))
	private boolean fabrication$disableSwimming(Entity self, Operation<Boolean> original) {
		return !(FabConf.isEnabled("*.no_swim") && fabrication$noSwimPredicate.test(self)) && original.call(self);
	}
}
