package com.refinedmods.refinedstorage.common.support.network;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderItem;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ResourceSorters {
    private ResourceSorters() {
    }

    public static Comparator<ResourceKey> create(@Nullable final RootStorage rootStorage,
                                                 final Inventory playerInventory) {
        return create(rootStorage, Collections.emptySet(), playerInventory, Function.identity());
    }

    public static <T> Comparator<T> create(@Nullable final RootStorage rootStorage,
                                           final Set<Pattern> patterns,
                                           final Inventory playerInventory,
                                           final Function<T, ResourceKey> resourceExtractor) {
        final MutableResourceList available = MutableResourceListImpl.create();
        addRootStorageItemsIntoList(rootStorage, available);
        addPlayerInventoryItemsIntoList(playerInventory, available);
        addPatternOutputsIntoList(patterns, available);
        return sortByHighestAvailableFirst(available, resourceExtractor);
    }

    private static void addRootStorageItemsIntoList(@Nullable final RootStorage rootStorage,
                                                    final MutableResourceList list) {
        if (rootStorage != null) {
            rootStorage.getAll().forEach(list::add);
        }
    }

    private static void addPlayerInventoryItemsIntoList(final Inventory playerInventory,
                                                        final MutableResourceList list) {
        for (int i = 0; i < playerInventory.getContainerSize(); ++i) {
            final ItemStack playerInventoryStack = playerInventory.getItem(i);
            if (playerInventoryStack.isEmpty()) {
                continue;
            }
            list.add(ItemResource.ofItemStack(playerInventoryStack), playerInventoryStack.getCount());
            if (playerInventoryStack.getItem() instanceof PatternProviderItem) {
                RefinedStorageApi.INSTANCE.getPattern(playerInventoryStack, playerInventory.player.level())
                    .ifPresent(pattern -> addPatternOutputsIntoList(list, pattern));
            }
        }
    }

    private static void addPatternOutputsIntoList(final Set<Pattern> patterns, final MutableResourceList available) {
        patterns.forEach(pattern -> addPatternOutputsIntoList(available, pattern));
    }

    private static void addPatternOutputsIntoList(final MutableResourceList available, final Pattern pattern) {
        pattern.layout().outputs().forEach(output -> available.add(output.resource(), output.amount()));
    }

    private static <T> Comparator<T> sortByHighestAvailableFirst(
        final ResourceList available,
        final Function<T, ResourceKey> resourceExtractor
    ) {
        return Comparator.<T>comparingLong(obj -> available.get(resourceExtractor.apply(obj))).reversed();
    }
}
