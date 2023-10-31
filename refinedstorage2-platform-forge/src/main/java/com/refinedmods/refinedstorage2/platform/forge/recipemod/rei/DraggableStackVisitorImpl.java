package com.refinedmods.refinedstorage2.platform.forge.recipemod.rei;

import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.platform.api.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ResourceSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import net.minecraft.client.gui.screens.Screen;

public class DraggableStackVisitorImpl
    implements DraggableStackVisitor<AbstractBaseScreen<? extends AbstractResourceContainerMenu>> {
    private final IngredientConverter ingredientConverter;

    public DraggableStackVisitorImpl(final IngredientConverter ingredientConverter) {
        this.ingredientConverter = ingredientConverter;
    }

    @Override
    public Stream<BoundsProvider> getDraggableAcceptingBounds(
        final DraggingContext<AbstractBaseScreen<? extends AbstractResourceContainerMenu>> context,
        final DraggableStack stack
    ) {
        final var screen = context.getScreen();
        final var menu = screen.getMenu();
        final var value = stack.getStack().getValue();
        final List<BoundsProvider> bounds = new ArrayList<>();
        ingredientConverter.convertToResource(value).ifPresent(resource -> {
            for (final ResourceSlot slot : menu.getResourceSlots()) {
                if (slot.isFilter() && slot.isValid(resource.resource())) {
                    bounds.add(BoundsProvider.ofRectangle(toRectangle(screen, slot)));
                }
            }
        });
        return bounds.stream();
    }

    @Override
    public DraggedAcceptorResult acceptDraggedStack(
        final DraggingContext<AbstractBaseScreen<? extends AbstractResourceContainerMenu>> context,
        final DraggableStack stack
    ) {
        final var screen = context.getScreen();
        final var menu = screen.getMenu();
        final Object value = stack.getStack().getValue();
        return ingredientConverter.convertToResource(value)
            .map(resourceTemplate -> accept(context, menu, screen, resourceTemplate))
            .orElse(DraggedAcceptorResult.PASS);
    }

    private <T> DraggedAcceptorResult accept(
        final DraggingContext<AbstractBaseScreen<? extends AbstractResourceContainerMenu>> context,
        final AbstractResourceContainerMenu menu,
        final AbstractBaseScreen<? extends AbstractResourceContainerMenu> screen,
        final ResourceTemplate<T> resource
    ) {
        for (final ResourceSlot slot : menu.getResourceSlots()) {
            final Rectangle slotBounds = toRectangle(screen, slot);
            if (!slotBounds.contains(context.getCurrentPosition())) {
                continue;
            }
            Platform.INSTANCE.getClientToServerCommunications().sendResourceFilterSlotChange(
                (PlatformStorageChannelType<T>) resource.storageChannelType(),
                resource.resource(),
                slot.index
            );
            return DraggedAcceptorResult.ACCEPTED;
        }
        return DraggedAcceptorResult.PASS;
    }

    private static Rectangle toRectangle(final AbstractBaseScreen<? extends AbstractResourceContainerMenu> screen,
                                         final ResourceSlot slot) {
        return new Rectangle(screen.getLeftPos() + slot.x, screen.getTopPos() + slot.y, 18, 18);
    }

    @Override
    public <R extends Screen> boolean isHandingScreen(final R screen) {
        return screen instanceof AbstractBaseScreen<?>
            && ((AbstractBaseScreen<?>) screen).getMenu() instanceof AbstractResourceContainerMenu;
    }
}
