package com.unascribed.fabrication.mixin.a_fixes.fix_charm_amethyst_dupe;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import org.spongepowered.asm.mixin.Mixin;
import svenhjol.charm.module.clear_item_frames.ClearItemFrames;

@Mixin(value=ClearItemFrames.class)
@EligibleIf(configAvailable="*.fix_charm_amethyst_dupe")
@FailOn(modNotLoaded="charm")
public class MixinClearItemFrames {

	// TODO: Check if this is still an issue

//	private boolean fabrication$wasInvisible;
//
//	@Inject(at=@At("HEAD"), method="handleUseEntity(Lnet/minecraft/class_1657;Lnet/minecraft/class_1937;Lnet/minecraft/class_1268;Lnet/minecraft/class_1297;Lnet/minecraft/class_3966;)Lnet/minecraft/class_1269;")
//	private void handleUseEntityHead(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
//		fabrication$wasInvisible = entity.isInvisible();
//	}
//
//	@Inject(at=@At("RETURN"), method="handleUseEntity(Lnet/minecraft/class_1657;Lnet/minecraft/class_1937;Lnet/minecraft/class_1268;Lnet/minecraft/class_1297;Lnet/minecraft/class_3966;)Lnet/minecraft/class_1269;")
//	private void handleUseEntityTail(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
//		if (entity.isInvisible() && !fabrication$wasInvisible) {
//			((SetInvisibleByCharm)entity).fabrication$setInvisibleByCharm(true);
//		}
//	}
//
//	@Inject(at=@At("HEAD"), method="handleAttackEntity(Lnet/minecraft/class_1657;Lnet/minecraft/class_1937;Lnet/minecraft/class_1268;Lnet/minecraft/class_1297;Lnet/minecraft/class_3966;)Lnet/minecraft/class_1269;",
//			cancellable=true)
//	public void handleAttackEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
//		if (!((SetInvisibleByCharm)entity).fabrication$isInvisibleByCharm()) {
//			ci.setReturnValue(ActionResult.PASS);
//		}
//	}

}
