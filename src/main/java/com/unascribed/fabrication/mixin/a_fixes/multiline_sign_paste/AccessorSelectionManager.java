package com.unascribed.fabrication.mixin.a_fixes.multiline_sign_paste;

import net.minecraft.client.util.SelectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

@Mixin(SelectionManager.class)
public interface AccessorSelectionManager {
	@Accessor("clipboardGetter")
	Supplier<String> getClipboardGetter();

	@Accessor("clipboardGetter")
	@Mutable
	void setClipboardGetter(Supplier<String> value);
}
