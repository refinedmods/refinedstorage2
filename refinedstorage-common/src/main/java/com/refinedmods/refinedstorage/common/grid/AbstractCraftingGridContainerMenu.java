package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryFilter;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.grid.view.ItemGridResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

public abstract class AbstractCraftingGridContainerMenu extends AbstractGridContainerMenu {
    private static final int Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_FIRST_CRAFTING_MATRIX_SLOT = 69;

    private final Player gridPlayer;
    private final CraftingGrid craftingGrid;
    private final List<Slot> craftingMatrixSlots = new ArrayList<>();

    @Nullable
    private Consumer<Boolean> activenessListener;
    @Nullable
    private ResourceRepositoryFilter<GridResource> filterBeforeFilteringBasedOnCraftingMatrixItems;

    protected AbstractCraftingGridContainerMenu(final MenuType<? extends AbstractGridContainerMenu> menuType,
                                                final int syncId,
                                                final Inventory playerInventory,
                                                final GridData gridData) {
        super(menuType, syncId, playerInventory, gridData);
        this.craftingGrid = new ClientCraftingGrid();
        this.gridPlayer = playerInventory.player;
    }

    protected AbstractCraftingGridContainerMenu(final MenuType<? extends AbstractGridContainerMenu> menuType,
                                                final int syncId,
                                                final Inventory playerInventory,
                                                final CraftingGrid craftingGrid) {
        super(menuType, syncId, playerInventory, craftingGrid);
        this.craftingGrid = craftingGrid;
        this.gridPlayer = playerInventory.player;
    }

    public void setActivenessListener(@Nullable final Consumer<Boolean> activenessListener) {
        this.activenessListener = activenessListener;
    }

    @Override
    public void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        if (activenessListener != null) {
            activenessListener.accept(newActive);
        }
    }

    @Override
    public boolean canTakeItemForPickAll(final ItemStack stack, final Slot slot) {
        if (slot instanceof CraftingGridResultSlot) {
            return false;
        }
        return super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    @SuppressWarnings("resource")
    public ItemStack quickMoveStack(final Player actor, final int slotIndex) {
        final Slot slot = getSlot(slotIndex);
        if (!actor.level().isClientSide()
            && slot instanceof CraftingGridResultSlot resultSlot
            && resultSlot.hasItem()) {
            final ItemStack craftedStack = resultSlot.onQuickCraft(actor);
            craftingGrid.acceptQuickCraft(actor, craftedStack);
            return ItemStack.EMPTY;
        }
        return super.quickMoveStack(actor, slotIndex);
    }

    @Override
    public void resized(final int playerInventoryY, final int topYStart, final int topYEnd) {
        super.resized(playerInventoryY, topYStart, topYEnd);
        craftingMatrixSlots.clear();
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                final int slotX = 26 + ((x % 3) * 18);
                final int slotY = playerInventoryY
                    - Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_FIRST_CRAFTING_MATRIX_SLOT
                    + ((y % 3) * 18);
                craftingMatrixSlots.add(addSlot(new Slot(craftingGrid.getCraftingMatrix(), x + y * 3, slotX, slotY)));
            }
        }
        addSlot(new CraftingGridResultSlot(
            gridPlayer,
            craftingGrid,
            130 + 4,
            playerInventoryY - Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_FIRST_CRAFTING_MATRIX_SLOT + 18
        ));
    }

    public List<Slot> getCraftingMatrixSlots() {
        return craftingMatrixSlots;
    }

    public void clear(final boolean toPlayerInventory) {
        craftingGrid.clearMatrix(gridPlayer, toPlayerInventory);
    }

    @API(status = API.Status.INTERNAL)
    public MutableResourceList getAvailableListForRecipeTransfer() {
        final MutableResourceList available = getRepository().copyBackingList();
        addContainerToList(craftingGrid.getCraftingMatrix(), available);
        addContainerToList(gridPlayer.getInventory(), available);
        return available;
    }

    private void addContainerToList(final Container container, final MutableResourceList available) {
        for (int i = 0; i < container.getContainerSize(); ++i) {
            final ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            available.add(ItemResource.ofItemStack(stack), stack.getCount());
        }
    }

    public void transferRecipe(final List<List<ItemResource>> recipe) {
        craftingGrid.transferRecipe(gridPlayer, recipe);
    }

    public void filterBasedOnCraftingMatrixItems() {
        final Set<ItemResource> craftingMatrixItems = getCraftingMatrixItems();
        filterBeforeFilteringBasedOnCraftingMatrixItems = getRepository().setFilterAndSort(
            (view, resource) -> resource instanceof ItemGridResource itemResource
                && craftingMatrixItems.contains(itemResource.getItemResource())
        );
    }

    private Set<ItemResource> getCraftingMatrixItems() {
        final Set<ItemResource> craftingMatrixItems = new HashSet<>();
        for (int i = 0; i < craftingGrid.getCraftingMatrix().getContainerSize(); ++i) {
            final ItemStack craftingMatrixStack = craftingGrid.getCraftingMatrix().getItem(i);
            if (craftingMatrixStack.isEmpty()) {
                continue;
            }
            craftingMatrixItems.add(ItemResource.ofItemStack(craftingMatrixStack));
        }
        return craftingMatrixItems;
    }

    public void stopFilteringBasedOnCraftingMatrixItems() {
        if (filterBeforeFilteringBasedOnCraftingMatrixItems == null) {
            return;
        }
        getRepository().setFilterAndSort(filterBeforeFilteringBasedOnCraftingMatrixItems);
        filterBeforeFilteringBasedOnCraftingMatrixItems = null;
    }

    @Nullable
    @Override
    protected ResourceKey getResourceForAutocraftableHint(final Slot slot) {
        if (slot.container == craftingGrid.getCraftingMatrix() || slot.container == craftingGrid.getCraftingResult()) {
            return ItemResource.ofItemStack(slot.getItem());
        }
        return super.getResourceForAutocraftableHint(slot);
    }

    @Override
    public boolean isLargeSlot(final Slot slot) {
        return slot.container == craftingGrid.getCraftingResult() || super.isLargeSlot(slot);
    }
}
