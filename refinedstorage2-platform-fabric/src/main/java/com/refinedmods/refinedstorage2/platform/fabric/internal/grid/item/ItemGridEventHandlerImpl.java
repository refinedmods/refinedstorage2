package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item;

import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class ItemGridEventHandlerImpl implements ItemGridEventHandler {
    private final ScreenHandler screenHandler;
    private final GridService<ItemResource> gridService;

    public ItemGridEventHandlerImpl(ScreenHandler screenHandler, GridService<ItemResource> gridService) {
        this.screenHandler = screenHandler;
        this.gridService = gridService;
    }

    @Override
    public void insert(GridInsertMode insertMode) {
        if (screenHandler.getCursorStack().isEmpty()) {
            return;
        }
        ResourceAmount<ItemResource> toInsert = Rs2PlatformApiFacade.INSTANCE.toItemResourceAmount(screenHandler.getCursorStack());
        Optional<ResourceAmount<ItemResource>> remainder = gridService.insert(toInsert, insertMode);
        screenHandler.setCursorStack(remainder.map(Rs2PlatformApiFacade.INSTANCE::toItemStack).orElse(ItemStack.EMPTY));
    }

    @Override
    public ItemStack transfer(ItemStack stack) {
        ResourceAmount<ItemResource> toInsert = Rs2PlatformApiFacade.INSTANCE.toItemResourceAmount(stack);
        Optional<ResourceAmount<ItemResource>> remainder = gridService.insert(toInsert, GridInsertMode.ENTIRE_RESOURCE);
        return remainder.map(Rs2PlatformApiFacade.INSTANCE::toItemStack).orElse(ItemStack.EMPTY);
    }
}
