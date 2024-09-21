package com.unascribed.fabrication.mixin.d_minor_mechanics.crawling;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.SetCrawling;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.crawling")
public class MixinPlayerEntity implements SetCrawling {

	private boolean fabrication$crawling;

	@ModifyExpressionValue(method="updatePose()V", at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;isSwimming()Z"))
	public boolean fabrication$updateSwimming(boolean old) {
		return old || !FabConf.isEnabled("*.crawling") ? old : fabrication$crawling;
	}

	@Override
	public void fabrication$setCrawling(boolean b) {
		fabrication$crawling = b;
	}

	@Override
	public boolean fabrication$isCrawling() {
		return fabrication$crawling;
	}

}
