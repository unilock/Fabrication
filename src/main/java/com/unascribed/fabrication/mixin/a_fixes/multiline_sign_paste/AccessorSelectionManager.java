package com.unascribed.fabrication.mixin.a_fixes.multiline_sign_paste;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.util.SelectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

@Mixin(SelectionManager.class)
@EligibleIf(configAvailable="*.multiline_sign_paste", envMatches=Env.CLIENT)
public interface AccessorSelectionManager {
	@Accessor("clipboardGetter")
	Supplier<String> fabrication$getClipboardGetter();

	@Accessor("clipboardGetter")
	@Mutable
	void fabrication$setClipboardGetter(Supplier<String> value);
}
