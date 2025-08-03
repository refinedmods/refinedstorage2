package com.refinedmods.refinedstorage.fabric.mixin;

import com.refinedmods.refinedstorage.common.autocrafting.PatternRendering;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class AbstractGuiGraphicsMixin {
    @Inject(method = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/entity/LivingEntity;"
        + "Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
        at = @At(value = "HEAD"), cancellable = true)
    @SuppressWarnings("ConstantConditions")
    protected void renderItem(@Nullable final LivingEntity entity, @Nullable final Level level,
                              final ItemStack stack, final int x, final int y, final int seed, final int guiOffset,
                              final CallbackInfo ci) {
        final GuiGraphics self = (GuiGraphics) (Object) this;
        if (PatternRendering.canDisplayOutput(stack)) {
            PatternRendering.getOutput(stack).ifPresent(output -> {
                self.renderItem(entity, level, output, x, y, seed, guiOffset);
                ci.cancel();
            });
        }
    }
}
