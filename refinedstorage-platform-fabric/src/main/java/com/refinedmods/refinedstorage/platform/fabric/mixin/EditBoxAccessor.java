package com.refinedmods.refinedstorage.platform.fabric.mixin;

import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EditBox.class)
public interface EditBoxAccessor {
    @Accessor("canLoseFocus")
    boolean getCanLoseFocus();
}
