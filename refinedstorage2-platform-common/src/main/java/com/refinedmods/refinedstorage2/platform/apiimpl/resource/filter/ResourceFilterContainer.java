package com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourceFilterContainer {
    private static final Logger LOGGER = LogManager.getLogger();

    private final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry;
    private final FilteredResource[] slots;
    private final Runnable listener;

    public ResourceFilterContainer(OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry, int size, Runnable listener) {
        this.resourceTypeRegistry = resourceTypeRegistry;
        this.slots = new FilteredResource[size];
        this.listener = listener;
    }

    public <T> void set(int slot, FilteredResource resource) {
        setSilently(slot, resource);
        listener.run();
    }

    private <T> void setSilently(int slot, FilteredResource resource) {
        slots[slot] = resource;
    }

    public void remove(int slot) {
        removeSilently(slot);
        listener.run();
    }

    private void removeSilently(int slot) {
        slots[slot] = null;
    }

    public int size() {
        return slots.length;
    }

    public FilteredResource get(int slot) {
        return slots[slot];
    }

    public Set<Object> getTemplates() {
        Set<Object> result = new HashSet<>();
        for (int i = 0; i < size(); ++i) {
            FilteredResource slot = slots[i];
            if (slot == null) {
                continue;
            }
            Object value = slot.getValue();
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    public ResourceType determineDefaultType() {
        List<ResourceType> distinctTypes = Arrays.stream(slots)
                .filter(Objects::nonNull)
                .map(FilteredResource::getType)
                .distinct()
                .toList();
        if (distinctTypes.size() == 1) {
            return distinctTypes.get(0);
        }
        return resourceTypeRegistry.getDefault();
    }

    public void writeToUpdatePacket(FriendlyByteBuf buf) {
        buf.writeResourceLocation(resourceTypeRegistry.getId(determineDefaultType()).orElseThrow(() -> new IllegalStateException("Default resource type not registered")));
        for (int slotIndex = 0; slotIndex < slots.length; ++slotIndex) {
            writeToUpdatePacket(slotIndex, buf);
        }
    }

    public void writeToUpdatePacket(int slotIndex, FriendlyByteBuf buf) {
        FilteredResource slot = slots[slotIndex];
        if (slot == null) {
            buf.writeBoolean(false);
            return;
        }
        resourceTypeRegistry.getId(slot.getType()).ifPresentOrElse(
                id -> {
                    buf.writeBoolean(true);
                    buf.writeResourceLocation(id);
                    slot.writeToPacket(buf);
                },
                () -> buf.writeBoolean(false)
        );
    }

    public void readFromUpdatePacket(int slotIndex, FriendlyByteBuf buf) {
        boolean present = buf.readBoolean();
        if (!present) {
            removeSilently(slotIndex);
            return;
        }
        ResourceLocation id = buf.readResourceLocation();
        resourceTypeRegistry.get(id).ifPresentOrElse(
                type -> setSilently(slotIndex, type.fromPacket(buf)),
                () -> LOGGER.warn("Resource type {} is not registered on the client, cannot read from packet", id)
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < size(); ++i) {
            FilteredResource slot = slots[i];
            if (slot == null) {
                continue;
            }
            int index = i;
            resourceTypeRegistry.getId(slot.getType()).ifPresentOrElse(
                    id -> addToTag(tag, index, slot, id),
                    () -> LOGGER.warn("Resource type {} is not registered, cannot serialize", slot.getType())
            );
        }
        return tag;
    }

    private void addToTag(CompoundTag tag, int index, FilteredResource slot, ResourceLocation typeId) {
        CompoundTag item = new CompoundTag();
        item.putString("t", typeId.toString());
        item.put("v", slot.toTag());
        tag.put("s" + index, item);
    }

    public void load(CompoundTag tag) {
        for (int i = 0; i < size(); ++i) {
            String key = "s" + i;
            if (!tag.contains(key)) {
                continue;
            }
            CompoundTag item = tag.getCompound(key);
            load(i, item);
        }
    }

    private void load(int index, CompoundTag item) {
        ResourceLocation typeId = new ResourceLocation(item.getString("t"));
        resourceTypeRegistry.get(typeId).ifPresentOrElse(
                type -> load(index, item, type),
                () -> LOGGER.warn("Resource type {} is not registered, cannot deserialize", typeId)
        );
    }

    private void load(int index, CompoundTag item, ResourceType type) {
        type.fromTag(item.getCompound("v")).ifPresent(filteredResource -> setSilently(index, filteredResource));
    }
}
