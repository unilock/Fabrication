package com.unascribed.fabrication.mixin._general.atlas;

import com.unascribed.fabrication.client.AtlasTracking;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteAtlasTexture.class)
@EligibleIf(envMatches=Env.CLIENT)
public class MixinSpriteAtlasTexture {

	@Inject(at=@At("TAIL"), method="<init>(Lnet/minecraft/util/Identifier;)V")
	public void construct(Identifier id, CallbackInfo ci) {
		AtlasTracking.allAtlases.add((SpriteAtlasTexture)(Object)this);
	}

}
