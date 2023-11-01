package com.refinedmods.refinedstorage2.platform.common.networking;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class NetworkCardItemPropertyFunction implements ClampedItemPropertyFunction {
    public static final ResourceLocation NAME = new ResourceLocation("active");

    @Override
    public float unclampedCall(final ItemStack itemStack,
                               @Nullable final ClientLevel clientLevel,
                               @Nullable final LivingEntity livingEntity,
                               final int i) {
        if (itemStack.getItem() instanceof NetworkCardItem cardItem) {
            return cardItem.isActive(itemStack) ? 1 : 0;
        }
        return 0;
    }
}
