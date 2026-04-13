package com.refinedmods.refinedstorage.common.security;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class ActiveSecurityCardItemModelProperty implements ConditionalItemModelProperty {
    public static final MapCodec<ActiveSecurityCardItemModelProperty> MAP_CODEC =
        MapCodec.unit(new ActiveSecurityCardItemModelProperty());
    public static final Identifier NAME = createIdentifier("active_security_card");

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
        return MAP_CODEC;
    }

    @Override
    public boolean get(final ItemStack stack, @Nullable final ClientLevel clientLevel,
                       @Nullable final LivingEntity livingEntity,
                       final int i, final ItemDisplayContext itemDisplayContext) {
        return stack.getItem() instanceof SecurityCardItem cardItem && cardItem.isValid(stack);
    }
}
