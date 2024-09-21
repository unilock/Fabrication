package com.unascribed.fabrication.mixin.d_minor_mechanics.collision_based_landing_pos;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.collision_based_landing_pos")
public abstract class MixinEntity {

	private static final Predicate<Entity> fabrication$collisionBasedLandingPos = ConfigPredicates.getFinalPredicate("*.collision_based_landing_pos");

	@Inject(method="getLandingPos()Lnet/minecraft/util/math/BlockPos;", at=@At(value="RETURN"), cancellable=true)
	public void getLandingPos(CallbackInfoReturnable<BlockPos> cir) {
		if (!FabConf.isEnabled("*.collision_based_landing_pos")) return;
		Entity self = (Entity)(Object)this;
		World world = self.getWorld();
		if (!fabrication$collisionBasedLandingPos.test(self)) return;
		VoxelShape inp = world.getBlockState(cir.getReturnValue()).getCollisionShape(world, cir.getReturnValue());
		if (!inp.isEmpty()) return;
		Box boundingBox = self.getBoundingBox();
		Vec3d pos = self.getPos();
		Optional<VoxelShape> ret = StreamSupport.stream(world.getBlockCollisions(self, new Box(boundingBox.minX, boundingBox.minY - 0.20000000298023224D, boundingBox.minZ, boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)).spliterator(), false).min(Comparator.comparing(a -> a.getClosestPointTo(pos).map(vec3d -> vec3d.distanceTo(pos)).orElse(Double.POSITIVE_INFINITY)));
		if (ret.isPresent()) {
			VoxelShape shape = ret.get();
			cir.setReturnValue(new BlockPos(
				MathHelper.floor(shape.getMin(Direction.Axis.X)),
				MathHelper.floor(shape.getMin(Direction.Axis.Y)),
				MathHelper.floor(shape.getMin(Direction.Axis.Z))
			));
		}
	}

}
