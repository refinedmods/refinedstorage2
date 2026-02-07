package com.refinedmods.refinedstorage.fabric.mixin;

import com.refinedmods.refinedstorage.common.autocrafting.PatternRendering;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class AbstractGuiGraphicsExtractorMixin {
    @Inject(method = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;item(Lnet/minecraft/world/entity/LivingEntity;"
        + "Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
        at = @At(value = "HEAD"), cancellable = true)
    @SuppressWarnings("ConstantConditions")
    protected void item(@Nullable final LivingEntity entity, @Nullable final Level level,
                        final ItemStack stack, final int x, final int y, final int seed,
                        final CallbackInfo ci) {
        final GuiGraphicsExtractor self = (GuiGraphicsExtractor) (Object) this;
        if (PatternRendering.canDisplayOutput(stack)) {
            PatternRendering.getOutput(stack).ifPresent(output -> {
                self.item(entity, level, output, x, y, seed);
                ci.cancel();
            });
        }
    }
}
