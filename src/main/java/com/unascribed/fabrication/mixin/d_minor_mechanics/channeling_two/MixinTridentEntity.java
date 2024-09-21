package com.unascribed.fabrication.mixin.d_minor_mechanics.channeling_two;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TridentEntity.class)
@EligibleIf(configAvailable="*.channeling_two")
abstract public class MixinTridentEntity extends PersistentProjectileEntity {

	protected MixinTridentEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	// TODO
//	@ModifyExpressionValue(method="onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V", at=@At(value="INVOKE", target="Lnet/minecraft/world/World;isThundering()Z"))
//	public boolean fabrication$channellingTwo(boolean bool) {
//		if (!(FabConf.isEnabled("*.channeling_two")) && EnchantmentHelperHelper.getLevel(getRegistryManager(), Enchantments.CHANNELING, this.getItemStack()) > 1) return bool;
//		return this.getWorld().hasRain(this.getBlockPos());
//	}

}
