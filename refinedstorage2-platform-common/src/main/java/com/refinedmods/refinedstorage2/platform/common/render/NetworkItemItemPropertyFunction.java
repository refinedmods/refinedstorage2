package com.refinedmods.refinedstorage2.platform.common.render;

import com.refinedmods.refinedstorage2.platform.common.item.AbstractNetworkBoundItem;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class NetworkItemItemPropertyFunction implements ClampedItemPropertyFunction {
    public static final ResourceLocation NAME = new ResourceLocation("active");

    @Override
    public float unclampedCall(final ItemStack itemStack,
                               @Nullable final ClientLevel clientLevel,
                               @Nullable final LivingEntity livingEntity,
                               final int i) {
        if (itemStack.getItem() instanceof AbstractNetworkBoundItem boundItem) {
            return boundItem.isBound(itemStack) ? 1 : 0;
        }
        return 0;
    }
}
