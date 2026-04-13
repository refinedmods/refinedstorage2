package com.refinedmods.refinedstorage.common.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class CustomBlockPlaceContext extends BlockPlaceContext {
    public CustomBlockPlaceContext(final Level level,
                                   @Nullable final Player player,
                                   final InteractionHand hand,
                                   final ItemStack stack,
                                   final BlockHitResult rayTraceResult) {
        super(level, player, hand, stack, rayTraceResult);
    }
}
