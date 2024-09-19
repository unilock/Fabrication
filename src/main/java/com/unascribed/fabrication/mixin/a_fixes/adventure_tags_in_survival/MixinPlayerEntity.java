package com.unascribed.fabrication.mixin.a_fixes.adventure_tags_in_survival;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.RegistryKeys;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.adventure_tags_in_survival")
public abstract class MixinPlayerEntity extends LivingEntity {

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow @Final
	private PlayerAbilities abilities;

	@FabInject(at=@At("HEAD"), method="isBlockBreakingRestricted(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/GameMode;)Z",
			cancellable=true)
	public void isBlockBreakingRestricted(World world, BlockPos pos, GameMode mode, CallbackInfoReturnable<Boolean> ci) {
		if (!FabConf.isEnabled("*.adventure_tags_in_survival") || mode.isCreative() || mode.isBlockBreakingRestricted()) return;
		ItemStack stack = getMainHandStack();
		if (!stack.isEmpty()) {
			if (stack.contains(DataComponentTypes.CUSTOM_DATA) && stack.get(DataComponentTypes.CUSTOM_DATA).getNbt().contains("CanDestroy")) {
				ci.setReturnValue(!stack.canBreak(new CachedBlockPosition(world, pos, false)));
			}
		}
	}

	@FabInject(at=@At("HEAD"), method="canPlaceOn(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Lnet/minecraft/item/ItemStack;)Z",
			cancellable=true)
	public void canPlaceOn(BlockPos pos, Direction dir, ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
		// note: this isn't called for block placement for some reason; that's hardcoded into ItemStack
		// however, this *is* used for buckets, spawn eggs, etc, and may be used by other mods
		if (!FabConf.isEnabled("*.adventure_tags_in_survival") || abilities.creativeMode || !abilities.allowModifyWorld) return;
		if (!stack.isEmpty()) {
			if (stack.contains(DataComponentTypes.CUSTOM_DATA) && stack.get(DataComponentTypes.CUSTOM_DATA).getNbt().contains("CanPlaceOn")) {
				World world = getWorld();
				ci.setReturnValue(stack.canPlaceOn(new CachedBlockPosition(world, pos.offset(dir.getOpposite()), false)));
			}
		}
	}

}
