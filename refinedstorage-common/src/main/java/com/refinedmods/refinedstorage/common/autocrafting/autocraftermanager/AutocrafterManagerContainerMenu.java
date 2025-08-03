package com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.autocrafting.PatternInventory;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;
import com.refinedmods.refinedstorage.common.support.stretching.ScreenSizeListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AutocrafterManagerContainerMenu extends AbstractBaseContainerMenu implements ScreenSizeListener,
    AutocrafterManagerWatcher {
    private final Inventory playerInventory;
    private final List<ViewGroup> groups;
    private final List<AutocrafterManagerSlot> autocrafterSlots = new ArrayList<>();

    @Nullable
    private AutocrafterManagerListener listener;
    @Nullable
    private AutocrafterManagerBlockEntity autocrafterManager;
    private String query = "";
    private boolean active;

    public AutocrafterManagerContainerMenu(final int syncId,
                                           final Inventory playerInventory,
                                           final AutocrafterManagerData data) {
        super(Menus.INSTANCE.getAutocrafterManager(), syncId);
        this.playerInventory = playerInventory;
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        this.groups = data.groups().stream().map(g -> ViewGroup.from(playerInventory.player.level(), g)).toList();
        this.active = data.active();
        resized(0, 0, 0);
    }

    AutocrafterManagerContainerMenu(final int syncId,
                                    final Inventory playerInventory,
                                    final AutocrafterManagerBlockEntity autocrafterManager,
                                    final List<AutocrafterManagerBlockEntity.Group> groups) {
        super(Menus.INSTANCE.getAutocrafterManager(), syncId);
        this.playerInventory = playerInventory;
        this.autocrafterManager = autocrafterManager;
        this.autocrafterManager.addWatcher(this);
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            autocrafterManager::getRedstoneMode,
            autocrafterManager::setRedstoneMode
        ));
        this.groups = Collections.emptyList();
        addServerSideSlots(groups);
    }

    @Override
    public void removed(final Player playerEntity) {
        super.removed(playerEntity);
        if (autocrafterManager != null) {
            autocrafterManager.removeWatcher(this);
        }
    }

    @Override
    public boolean stillValid(final Player player) {
        if (autocrafterManager == null) {
            return true;
        }
        return Container.stillValidBlockEntity(autocrafterManager, player);
    }

    void setListener(final AutocrafterManagerListener listener) {
        this.listener = listener;
    }

    void setQuery(final String query) {
        this.query = query;
        notifyListener();
    }

    private void notifyListener() {
        if (listener != null) {
            listener.slotsChanged();
        }
    }

    private void addServerSideSlots(final List<AutocrafterManagerBlockEntity.Group> serverGroups) {
        for (final AutocrafterManagerBlockEntity.Group group : serverGroups) {
            addServerSideSlots(group);
        }
        addPlayerInventory(playerInventory, 0, 0);
    }

    private void addServerSideSlots(final AutocrafterManagerBlockEntity.Group group) {
        for (final AutocrafterManagerBlockEntity.SubGroup subGroup : group.subGroups()) {
            final Container container = subGroup.container();
            for (int i = 0; i < container.getContainerSize(); i++) {
                addSlot(new Slot(container, i, 0, 0));
            }
        }
    }

    public boolean containsPattern(final ItemStack stack) {
        for (final Slot slot : autocrafterSlots) {
            if (slot.getItem() == stack) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void resized(final int playerInventoryY, final int topYStart, final int topYEnd) {
        initializeGroups(playerInventoryY, topYStart, topYEnd);
    }

    private void initializeGroups(final int playerInventoryY, final int topYStart, final int topYEnd) {
        resetSlots();
        autocrafterSlots.clear();
        final int rowX = 7 + 1;
        final int startY = topYStart - 18;
        int rowY = topYStart;
        for (final ViewGroup group : groups) {
            rowY += initializeGroup(group, rowX, rowY, startY, topYEnd);
        }
        addPlayerInventory(playerInventory, 8, playerInventoryY);
    }

    private int initializeGroup(final ViewGroup group,
                                final int rowX,
                                final int rowY,
                                final int startY,
                                final int topYEnd) {
        int slotsWithinGroup = 0;
        for (final SubViewGroup subGroup : group.subViewGroups) {
            int slotsWithinSubGroup = 0;
            final boolean visible = active && isVisible(subGroup);
            for (int i = 0; i < subGroup.backingInventory.getContainerSize(); i++) {
                final int slotX = rowX + ((slotsWithinGroup % 9) * 18);
                final int slotY = rowY + 18 + ((slotsWithinGroup / 9) * 18);
                final boolean slotVisible = visible && isSlotVisible(playerInventory.player.level(), group, i);
                final AutocrafterManagerSlot slot = new AutocrafterManagerSlot(
                    subGroup.backingInventory,
                    playerInventory.player.level(),
                    i,
                    slotX,
                    slotY,
                    IntIntPair.of(startY, topYEnd),
                    slotVisible
                );
                addSlot(slot);
                if (slotVisible) {
                    autocrafterSlots.add(slot);
                    ++slotsWithinGroup;
                    ++slotsWithinSubGroup;
                }
            }
            subGroup.visibleSlots = slotsWithinSubGroup;
        }
        group.visibleSlots = slotsWithinGroup;
        if (slotsWithinGroup == 0) {
            return 0;
        }
        return (group.getVisibleRows() + 1) * 18;
    }

    private boolean isVisible(final SubViewGroup subGroup) {
        return switch (getViewType()) {
            case VISIBLE -> subGroup.visibleToTheAutocrafterManager;
            case NOT_FULL -> !subGroup.full;
            case ALL -> true;
        };
    }

    private boolean isSlotVisible(final Level level, final ViewGroup group, final int index) {
        final String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        if (normalizedQuery.isEmpty()) {
            return true;
        }
        return getSearchMode().isSlotVisible(group, level, normalizedQuery, index);
    }

    List<ViewGroup> getGroups() {
        return groups;
    }

    List<AutocrafterManagerSlot> getAutocrafterSlots() {
        return autocrafterSlots;
    }

    AutocrafterManagerSearchMode getSearchMode() {
        return Platform.INSTANCE.getConfig().getAutocrafterManager().getSearchMode();
    }

    void setSearchMode(final AutocrafterManagerSearchMode searchMode) {
        Platform.INSTANCE.getConfig().getAutocrafterManager().setSearchMode(searchMode);
        notifyListener();
    }

    AutocrafterManagerViewType getViewType() {
        return Platform.INSTANCE.getConfig().getAutocrafterManager().getViewType();
    }

    void setViewType(final AutocrafterManagerViewType toggle) {
        Platform.INSTANCE.getConfig().getAutocrafterManager().setViewType(toggle);
        notifyListener();
    }

    public void setActive(final boolean active) {
        this.active = active;
        notifyListener();
    }

    boolean isActive() {
        return active;
    }

    @Override
    public void activeChanged(final boolean newActive) {
        if (playerInventory.player instanceof ServerPlayer serverPlayerEntity) {
            S2CPackets.sendAutocrafterManagerActive(serverPlayerEntity, newActive);
        }
    }

    static class SubViewGroup {
        private final boolean visibleToTheAutocrafterManager;
        private final Container backingInventory;
        private final boolean full;
        private int visibleSlots;

        private SubViewGroup(final boolean visibleToTheAutocrafterManager,
                             final Container backingInventory,
                             final boolean full) {
            this.visibleToTheAutocrafterManager = visibleToTheAutocrafterManager;
            this.backingInventory = backingInventory;
            this.full = full;
        }

        private static SubViewGroup from(final AutocrafterManagerData.SubGroup subGroup, final Level level) {
            final PatternInventory backingInventory = new PatternInventory(subGroup.slotCount(), () -> level);
            return new SubViewGroup(
                subGroup.visibleToTheAutocrafterManager(),
                backingInventory,
                subGroup.full()
            );
        }

        int getVisibleSlots() {
            return visibleSlots;
        }

        boolean hasPatternInput(final Level level, final String normalizedQuery, final int index) {
            final ItemStack patternStack = backingInventory.getItem(index);
            return RefinedStorageApi.INSTANCE.getPattern(patternStack, level).map(
                pattern -> hasIngredient(pattern.layout().ingredients(), normalizedQuery)
            ).orElse(false);
        }

        boolean hasPatternOutput(final Level level, final String normalizedQuery, final int index) {
            final ItemStack patternStack = backingInventory.getItem(index);
            return RefinedStorageApi.INSTANCE.getPattern(patternStack, level).map(
                pattern -> hasResource(pattern.layout().outputs(), normalizedQuery)
            ).orElse(false);
        }

        private static boolean hasIngredient(final List<Ingredient> ingredients, final String normalizedQuery) {
            return ingredients.stream().flatMap(i -> i.inputs().stream()).anyMatch(key ->
                hasResource(normalizedQuery, key));
        }

        private static boolean hasResource(final List<ResourceAmount> resources, final String normalizedQuery) {
            return resources.stream().map(ResourceAmount::resource).anyMatch(key ->
                hasResource(normalizedQuery, key));
        }

        private static boolean hasResource(final String normalizedQuery, final ResourceKey key) {
            return RefinedStorageClientApi.INSTANCE.getResourceRendering(key.getClass())
                .getDisplayName(key)
                .getString()
                .toLowerCase(Locale.ROOT)
                .trim()
                .contains(normalizedQuery);
        }
    }

    static class ViewGroup {
        private final String name;
        private final List<SubViewGroup> subViewGroups;
        private final List<SubViewGroup> subViewGroupsView;
        private int visibleSlots;

        private ViewGroup(final String name, final List<SubViewGroup> subViewGroups) {
            this.name = name;
            this.subViewGroups = subViewGroups;
            this.subViewGroupsView = Collections.unmodifiableList(subViewGroups);
        }

        private static ViewGroup from(final Level level, final AutocrafterManagerData.Group group) {
            return new ViewGroup(
                group.name(),
                group.subGroups().stream().map(subGroup -> SubViewGroup.from(subGroup, level)).toList()
            );
        }

        String getName() {
            return name;
        }

        List<SubViewGroup> getSubViewGroups() {
            return subViewGroupsView;
        }

        boolean isVisible() {
            return visibleSlots > 0;
        }

        int getVisibleRows() {
            return Math.ceilDiv(visibleSlots, 9);
        }

        boolean nameContains(final String normalizedQuery) {
            return name.toLowerCase(Locale.ROOT).trim().contains(normalizedQuery);
        }

        boolean hasPatternInput(final Level level, final String normalizedQuery, final int index) {
            return subViewGroups.stream().anyMatch(
                subGroup -> subGroup.hasPatternInput(level, normalizedQuery, index)
            );
        }

        boolean hasPatternOutput(final Level level, final String normalizedQuery, final int index) {
            return subViewGroups.stream().anyMatch(
                subGroup -> subGroup.hasPatternOutput(level, normalizedQuery, index)
            );
        }
    }
}
