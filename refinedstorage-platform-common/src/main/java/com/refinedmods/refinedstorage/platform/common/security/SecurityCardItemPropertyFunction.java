package com.refinedmods.refinedstorage.platform.common.security;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public class SecurityCardItemPropertyFunction implements ClampedItemPropertyFunction {
    public static final ResourceLocation NAME = createIdentifier("security_card_active");

    @Override
    public float unclampedCall(final ItemStack itemStack,
                               @Nullable final ClientLevel clientLevel,
                               @Nullable final LivingEntity livingEntity,
                               final int i) {
        if (itemStack.getItem() instanceof SecurityCardItem cardItem) {
            return cardItem.isValid(itemStack) ? 1 : 0;
        }
        return 0;
    }
}
