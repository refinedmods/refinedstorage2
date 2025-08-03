package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.common.content.DataComponents;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class PatternItemOverrides extends ItemOverrides {
    private final BakedModel emptyModel;
    private final BakedModel craftingModel;
    private final BakedModel processingModel;
    private final BakedModel stonecutterModel;
    private final BakedModel smithingTableModel;

    @SuppressWarnings({"DataFlowIssue", "deprecation"}) // null is allowed as long as we don't pass overrides
    public PatternItemOverrides(final ModelBaker modelBaker,
                                final BakedModel emptyModel,
                                final BakedModel craftingModel,
                                final BakedModel processingModel,
                                final BakedModel stonecutterModel,
                                final BakedModel smithingTableModel) {
        super(modelBaker, null, List.of());
        this.emptyModel = emptyModel;
        this.craftingModel = craftingModel;
        this.processingModel = processingModel;
        this.stonecutterModel = stonecutterModel;
        this.smithingTableModel = smithingTableModel;
    }

    @Override
    public BakedModel resolve(final BakedModel model,
                              final ItemStack stack,
                              @Nullable final ClientLevel level,
                              @Nullable final LivingEntity entity,
                              final int seed) {
        final PatternState state = stack.get(DataComponents.INSTANCE.getPatternState());
        if (state == null) {
            return emptyModel;
        }
        return switch (state.type()) {
            case CRAFTING -> craftingModel;
            case PROCESSING -> processingModel;
            case STONECUTTER -> stonecutterModel;
            case SMITHING_TABLE -> smithingTableModel;
        };
    }
}
