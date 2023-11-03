package com.refinedmods.refinedstorage2.platform.common.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

record NetworkReceiverKey(GlobalPos pos) {
    int getDistance(final BlockPos from) {
        return (int) Math.sqrt(
            Math.pow(from.getX() - (double) pos.pos().getX(), 2)
                + Math.pow(from.getY() - (double) pos.pos().getY(), 2)
                + Math.pow(from.getZ() - (double) pos.pos().getZ(), 2)
        );
    }

    MutableComponent getDimensionName() {
        return getDimensionName(pos.dimension());
    }

    static MutableComponent getDimensionName(final ResourceKey<Level> dimension) {
        if (dimension == Level.OVERWORLD) {
            return Component.literal("Overworld");
        } else if (dimension == Level.END) {
            return Component.literal("The End");
        } else if (dimension == Level.NETHER) {
            return Component.literal("The Nether");
        }
        return Component.translatable(
            "dimension.%s.%s".formatted(dimension.location().getNamespace(), dimension.location().getPath())
        );
    }
}
