package com.unascribed.fabrication.mixin.b_utility.canhit;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.function.Predicate;

@Mixin(EntitySelector.class)
@EligibleIf(configAvailable="*.canhit")
public interface AccessorEntitySelector {
	@Accessor
	List<Predicate<Entity>> getPredicates();
}
