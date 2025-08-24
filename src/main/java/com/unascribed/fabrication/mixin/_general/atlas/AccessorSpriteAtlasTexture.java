package com.unascribed.fabrication.mixin._general.atlas;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(SpriteAtlasTexture.class)
@EligibleIf(envMatches=Env.CLIENT)
public interface AccessorSpriteAtlasTexture {
	@Accessor("sprites")
	Map<Identifier, Sprite> fabrication$getSprites();

	@Accessor("sprites")
	void fabrication$setSprites(Map<Identifier, Sprite> value);

	@Accessor("animatedSprites")
	List<Sprite.TickableAnimation> fabrication$getAnimatedSprites();

	@Accessor("animatedSprites")
	void fabrication$setAnimatedSprites(List<Sprite.TickableAnimation> value);
}
