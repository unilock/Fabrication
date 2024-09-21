package com.unascribed.fabrication.mixin.c_tweaks.no_sneak_bypass;

import com.llamalad7.mixinextras.sugar.Local;
import com.unascribed.fabrication.interfaces.SetActualBypassState;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.Vibrations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Vibrations.Callback.class)
@EligibleIf(configAvailable="*.no_sneak_bypass")
public interface MixinVibrationListener {

	@Inject(method="canAccept(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/world/event/GameEvent$Emitter;)Z", at=@At(value= "INVOKE", target="Lnet/minecraft/entity/Entity;bypassesSteppingEffects()Z"))
	private void fabrication$getActualBypassesStepping(RegistryEntry<GameEvent> gameEvent, GameEvent.Emitter emitter, CallbackInfoReturnable<Boolean> cir, @Local Entity entity) {
		if (entity instanceof SetActualBypassState) {
			((SetActualBypassState)entity).fabrication$setActualBypassesStepOn();
		}
	}

}
