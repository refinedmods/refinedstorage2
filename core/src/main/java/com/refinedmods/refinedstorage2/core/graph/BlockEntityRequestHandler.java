package com.refinedmods.refinedstorage2.core.graph;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;

public class BlockEntityRequestHandler<T extends BlockEntity> implements RequestHandler<T, BlockEntityRequest> {
    private final Class<T> blockEntityClass;

    public BlockEntityRequestHandler(Class<T> blockEntityClass) {
        this.blockEntityClass = blockEntityClass;
    }

    @Override
    public void handle(BlockEntityRequest request, GraphScannerContext<T, BlockEntityRequest> context) {
        request.getWorldAdapter().getBlockEntity(request.getPos()).ifPresent(blockEntity -> {
            if (blockEntity.getClass().isAssignableFrom(blockEntityClass) && context.addEntry((T) blockEntity)) {
                for (Direction direction : Direction.values()) {
                    context.addRequest(new BlockEntityRequest(request.getWorldAdapter(), request.getPos().offset(direction)));
                }
            }
        });
    }
}
