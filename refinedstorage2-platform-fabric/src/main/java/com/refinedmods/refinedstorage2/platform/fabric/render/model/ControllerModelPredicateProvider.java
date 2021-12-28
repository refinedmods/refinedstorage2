package com.refinedmods.refinedstorage2.platform.fabric.render.model;

import com.refinedmods.refinedstorage2.platform.common.item.block.ControllerBlockItem;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ControllerModelPredicateProvider implements ClampedItemPropertyFunction {
    @Override
    public float unclampedCall(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        return ControllerBlockItem.getPercentFull(stack);
    }
}
