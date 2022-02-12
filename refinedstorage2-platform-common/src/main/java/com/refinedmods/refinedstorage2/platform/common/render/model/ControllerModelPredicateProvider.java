package com.refinedmods.refinedstorage2.platform.common.render.model;

import com.refinedmods.refinedstorage2.platform.common.item.block.ControllerBlockItem;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ControllerModelPredicateProvider implements ClampedItemPropertyFunction {
    @Override
    public float unclampedCall(ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
        return ControllerBlockItem.getPercentFull(stack);
    }
}
