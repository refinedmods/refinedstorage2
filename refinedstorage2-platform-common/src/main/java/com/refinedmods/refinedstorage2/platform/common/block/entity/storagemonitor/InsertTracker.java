package com.refinedmods.refinedstorage2.platform.common.block.entity.storagemonitor;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.item.ItemStack;

class InsertTracker {
    private static final long MAX_DELAY = 500;

    private final Map<UUID, Entry> entries = new HashMap<>();

    public void trackInsertedItem(final GameProfile gameProfile, final ItemStack stack) {
        entries.put(gameProfile.getId(), new Entry(System.currentTimeMillis(), ItemResource.ofItemStack(stack)));
    }

    public Optional<ItemResource> getLastInsertedItem(final GameProfile gameProfile) {
        final Entry entry = entries.get(gameProfile.getId());
        if (entry == null) {
            return Optional.empty();
        }
        final long time = System.currentTimeMillis();
        if (time - entry.time > MAX_DELAY) {
            entries.remove(gameProfile.getId());
            return Optional.empty();
        }
        return Optional.of(entry.itemResource);
    }

    private record Entry(long time, ItemResource itemResource) {
    }
}
