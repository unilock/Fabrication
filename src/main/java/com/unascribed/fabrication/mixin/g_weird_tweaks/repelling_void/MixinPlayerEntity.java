package com.unascribed.fabrication.mixin.g_weird_tweaks.repelling_void;

import java.util.List;

import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.block.BlockState;
import com.unascribed.fabrication.FabConf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import com.google.common.collect.Lists;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.repelling_void")
public abstract class MixinPlayerEntity extends LivingEntity {

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	private Vec3d fabrication$lastGroundPos;
	private BlockPos fabrication$lastLandingPos;
	private final List<Vec3d> fabrication$voidFallTrail = Lists.newArrayList();
	private boolean fabrication$debted;

	@FabInject(at=@At("TAIL"), method="tick()V")
	public void tick(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.repelling_void")) return;
		Entity entity = this;
		Entity vehicle = this.getVehicle();
		while (vehicle != null) {
			entity = vehicle;
			vehicle = vehicle.getVehicle();
		}
		if (entity.isOnGround()) {
			fabrication$lastGroundPos = entity.getPos();
			fabrication$lastLandingPos = entity.getSteppingPos();
			fabrication$voidFallTrail.clear();
		} else if (fabrication$voidFallTrail.size() < 20) {
			fabrication$voidFallTrail.add(getPos());
		}
		if (fabrication$debted) {
			fabrication$debted = false;
			damage(getWorld().getDamageSources().outOfWorld(), 12);
		}
	}


	@FabInject(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", cancellable=true)
	public void remove(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.repelling_void") && !fabrication$debted && source.isOf(DamageTypes.OUT_OF_WORLD) && fabrication$lastLandingPos != null && this.getY() < getWorld().getBottomY() -10) {
			World world = getWorld();
			BlockPos bp = fabrication$lastLandingPos;
			Vec3d pos = fabrication$lastGroundPos;
			BlockState state = world.getBlockState(bp);
			if (!state.getCollisionShape(world, bp).isEmpty()) {
				Box bounds = state.getCollisionShape(getWorld(), bp).getBoundingBox();
				pos = new Vec3d(bp.getX()+bounds.minX+(bounds.maxX-bounds.minX)/2, bp.getY()+bounds.maxY+0.1, bp.getZ()+bounds.minZ+(bounds.maxZ-bounds.minZ)/2);
			} else {
				out: for (int d = 1; d <= 3; d++) {
					for (int x = -d; x <= d; x++) {
						for (int z = -d; z <= d; z++) {
							bp = fabrication$lastLandingPos.add(x, 0, z);
							state = world.getBlockState(bp);
							if (!state.getCollisionShape(world, bp).isEmpty()) {
								Box bounds = state.getCollisionShape(world, bp).getBoundingBox();
								pos = new Vec3d(bp.getX()+bounds.minX+(bounds.maxX-bounds.minX)/2, bp.getY()+bounds.maxY+0.1, bp.getZ()+bounds.minZ+(bounds.maxZ-bounds.minZ)/2);
								break out;
							}
						}
					}
				}
			}
			dismountVehicle();
			requestTeleport(pos.x, pos.y, pos.z);
			fallDistance = 0;
			world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 0.5f);
			world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BLOCK_SHROOMLIGHT_PLACE, SoundCategory.PLAYERS, 1, 0.5f);
			world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BLOCK_SHROOMLIGHT_PLACE, SoundCategory.PLAYERS, 1, 0.75f);
			world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 0.2f, 0.5f);
			Object self = this;
			if (!world.isClient && self instanceof ServerPlayerEntity) {
				Box box = getBoundingBox();
				((ServerWorld)world).spawnParticles((ServerPlayerEntity)self, ParticleTypes.PORTAL, true, pos.x, pos.y+(box.getLengthY()/2), pos.z, 32, box.getLengthX()/2, box.getLengthY()/2, box.getLengthZ()/2, 0.2);
				((ServerWorld)world).spawnParticles(ParticleTypes.PORTAL, pos.x, pos.y+(box.getLengthY()/2), pos.z, 32, box.getLengthX()/2, box.getLengthY()/2, box.getLengthZ()/2, 0.2);
				for (Vec3d vec : fabrication$voidFallTrail) {
					((ServerWorld)world).spawnParticles((ServerPlayerEntity)self, ParticleTypes.CLOUD, true, vec.x, vec.y, vec.z, 0, 0, 1, 0, 0.05);
					((ServerWorld)world).spawnParticles(ParticleTypes.CLOUD, vec.x, vec.y, vec.z, 0, 0, 1, 0, 0.05);
				}
			}
			if (!(self instanceof PlayerEntity && (((PlayerEntity)self).getAbilities().invulnerable))) {
				fabrication$debted = true;
			}
			cir.setReturnValue(false);
		}
	}

	@FabInject(at = @At("TAIL"), method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void writeCustomDataToTag(NbtCompound tag, CallbackInfo ci) {
		if (fabrication$lastGroundPos != null) {
			Vec3d pos = fabrication$lastGroundPos;
			tag.putDouble("fabrication:LastGroundPosX", pos.x);
			tag.putDouble("fabrication:LastGroundPosY", pos.y);
			tag.putDouble("fabrication:LastGroundPosZ", pos.z);
		}
		if (fabrication$debted) {
			tag.putBoolean("fabrication:Debted", fabrication$debted);
		}
	}

	@FabInject(at = @At("TAIL"), method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void readCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
		if (tag.contains("fabrication:LastGroundPosX")) {
			fabrication$lastGroundPos = new Vec3d(
					tag.getDouble("fabrication:LastGroundPosX"),
					tag.getDouble("fabrication:LastGroundPosY"),
					tag.getDouble("fabrication:LastGroundPosZ")
					);
		}
		fabrication$debted = tag.getBoolean("fabrication:Debted");
	}

}
