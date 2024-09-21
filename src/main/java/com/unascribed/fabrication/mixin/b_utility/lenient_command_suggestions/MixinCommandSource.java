package com.unascribed.fabrication.mixin.b_utility.lenient_command_suggestions;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommandSource.class)
@EligibleIf(configAvailable="*.lenient_command_suggestions")
public interface MixinCommandSource {

	@ModifyReturnValue(method="forEachMatching(Ljava/lang/Iterable;Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Consumer;)V", at=@At(value="INVOKE", target="Ljava/lang/String;equals(Ljava/lang/Object;)Z"))
	private static boolean commandSourceSkipNamespaceCheck(boolean old) {
		return FabConf.isEnabled("*.lenient_command_suggestions") || old;
	}

}
