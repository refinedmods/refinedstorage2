package com.refinedmods.refinedstorage2.platform.common.recipemod.jei;

import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.platform.api.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ResourceSlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;

@SuppressWarnings("rawtypes")
class GhostIngredientHandler implements IGhostIngredientHandler<AbstractBaseScreen> {
    private final IngredientConverter ingredientConverter;

    GhostIngredientHandler(final IngredientConverter ingredientConverter) {
        this.ingredientConverter = ingredientConverter;
    }

    @Override
    public <I> List<Target<I>> getTargetsTyped(final AbstractBaseScreen screen,
                                               final ITypedIngredient<I> ingredient,
                                               final boolean doStart) {
        if (screen.getMenu() instanceof AbstractResourceContainerMenu menu) {
            return getTargets(screen, ingredient.getIngredient(), menu);
        }
        return Collections.emptyList();
    }

    private <I> List<Target<I>> getTargets(final AbstractBaseScreen screen,
                                           final I ingredient,
                                           final AbstractResourceContainerMenu menu) {
        final List<Target<I>> targets = new ArrayList<>();
        ingredientConverter.convertToResource(ingredient).ifPresent(resource -> {
            for (final ResourceSlot slot : menu.getResourceSlots()) {
                if (slot.isFilter() && slot.isValid(resource.resource())) {
                    final Rect2i bounds = getBounds(screen, slot);
                    targets.add(new TargetImpl<>(bounds, slot.index));
                }
            }
        });
        return targets;
    }

    private Rect2i getBounds(final AbstractBaseScreen screen, final ResourceSlot slot) {
        return new Rect2i(screen.getLeftPos() + slot.x, screen.getTopPos() + slot.y, 17, 17);
    }

    @Override
    public void onComplete() {
        // no op
    }

    private class TargetImpl<I> implements Target<I> {
        private final Rect2i area;
        private final int slotIndex;

        TargetImpl(final Rect2i area, final int slotIndex) {
            this.area = area;
            this.slotIndex = slotIndex;
        }

        @Override
        public Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(final I ingredient) {
            ingredientConverter.convertToResource(ingredient).ifPresent(this::accept);
        }

        private <T> void accept(final ResourceTemplate<T> resource) {
            Platform.INSTANCE.getClientToServerCommunications().sendResourceFilterSlotChange(
                (PlatformStorageChannelType<T>) resource.storageChannelType(),
                resource.resource(),
                slotIndex
            );
        }
    }
}

