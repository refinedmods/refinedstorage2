package com.refinedmods.refinedstorage2.platform.common.internal.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.TypedTemplate;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceInstance;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.util.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToLongFunction;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ResourceContainer implements Container {
    private final ResourceInstance<?>[] items;
    private final ResourceContainerType type;
    private final ToLongFunction<ResourceInstance<?>> maxAmountProvider;
    @Nullable
    private Runnable listener;

    public ResourceContainer(final int size, final ResourceContainerType type) {
        this(size, type, resourceInstance -> Long.MAX_VALUE);
    }

    public ResourceContainer(final int size,
                             final ResourceContainerType type,
                             final ToLongFunction<ResourceInstance<?>> maxAmountProvider) {
        this.items = new ResourceInstance<?>[size];
        this.type = type;
        this.maxAmountProvider = maxAmountProvider;
    }

    public boolean supportsAmount() {
        return type == ResourceContainerType.CONTAINER || type == ResourceContainerType.FILTER_WITH_AMOUNT;
    }

    public boolean canModifyAmount() {
        return type == ResourceContainerType.FILTER_WITH_AMOUNT;
    }

    public boolean supportsItemSlotInteractions() {
        return type == ResourceContainerType.CONTAINER;
    }

    public long getMaxAmount(final ResourceInstance<?> resource) {
        return maxAmountProvider.applyAsLong(resource);
    }

    public void setListener(@Nullable final Runnable listener) {
        this.listener = listener;
    }

    public <T> void set(final int index, final ResourceInstance<T> resourceInstance) {
        setSilently(index, resourceInstance);
        if (listener != null) {
            listener.run();
        }
    }

    private <T> void setSilently(final int index, final ResourceInstance<T> resourceInstance) {
        items[index] = resourceInstance;
    }

    public void setAmount(final int index, final long amount) {
        if (!supportsAmount()) {
            return;
        }
        final ResourceInstance<?> entry = items[index];
        if (entry == null) {
            return;
        }
        final long newAmount = MathHelper.clamp(amount, 1, getMaxAmount(entry));
        items[index] = entry.withAmount(newAmount);
        if (listener != null) {
            listener.run();
        }
    }

    public void remove(final int index) {
        removeSilently(index);
        if (listener != null) {
            listener.run();
        }
    }

    private void removeSilently(final int index) {
        items[index] = null;
    }

    public int size() {
        return items.length;
    }

    @Nullable
    public ResourceInstance<?> get(final int index) {
        return items[index];
    }

    public Set<TypedTemplate<?>> getUniqueTemplates() {
        return getTemplates(new HashSet<>());
    }

    public List<TypedTemplate<?>> getTemplates() {
        return getTemplates(new ArrayList<>());
    }

    private <C extends Collection<TypedTemplate<?>>> C getTemplates(final C result) {
        for (int i = 0; i < size(); ++i) {
            final ResourceInstance<?> entry = items[i];
            if (entry == null) {
                continue;
            }
            result.add(createTemplate(entry));
        }
        return result;
    }

    private <T> TypedTemplate<T> createTemplate(final ResourceInstance<T> entry) {
        return new TypedTemplate<>(
            entry.getResource(),
            entry.getStorageChannelType()
        );
    }

    public void writeToUpdatePacket(final FriendlyByteBuf buf) {
        for (int index = 0; index < items.length; ++index) {
            final ResourceInstance<?> entry = items[index];
            if (entry == null) {
                buf.writeBoolean(false);
                continue;
            }
            writeToUpdatePacket(buf, entry);
        }
    }

    private <T> void writeToUpdatePacket(final FriendlyByteBuf buf,
                                         final ResourceInstance<T> entry) {
        final PlatformStorageChannelType<T> storageChannelType = entry.getStorageChannelType();
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getId(storageChannelType).ifPresentOrElse(id -> {
            buf.writeBoolean(true);
            buf.writeResourceLocation(id);
            storageChannelType.toBuffer(entry.getResource(), buf);
            buf.writeLong(entry.getAmount());
        }, () -> buf.writeBoolean(false));
    }

    public void readFromUpdatePacket(final int index, final FriendlyByteBuf buf) {
        final boolean present = buf.readBoolean();
        if (!present) {
            removeSilently(index);
            return;
        }
        final ResourceLocation id = buf.readResourceLocation();
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().get(id).ifPresent(
            storageChannelType -> readFromUpdatePacket(index, buf, storageChannelType)
        );
    }

    private <T> void readFromUpdatePacket(final int index,
                                          final FriendlyByteBuf buf,
                                          final PlatformStorageChannelType<T> storageChannelType) {
        final T resource = storageChannelType.fromBuffer(buf);
        final long amount = buf.readLong();
        setSilently(index, new ResourceInstance<>(new ResourceAmount<>(resource, amount), storageChannelType));
    }

    public CompoundTag toTag() {
        final CompoundTag tag = new CompoundTag();
        for (int i = 0; i < size(); ++i) {
            final ResourceInstance<?> entry = items[i];
            if (entry == null) {
                continue;
            }
            addToTag(tag, i, entry);
        }
        return tag;
    }

    private <T> void addToTag(final CompoundTag tag,
                              final int index,
                              final ResourceInstance<T> entry) {
        final PlatformStorageChannelType<T> storageChannelType = entry.getStorageChannelType();
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getId(storageChannelType).ifPresent(
            storageChannelTypeId -> addToTag(tag, index, entry, storageChannelType, storageChannelTypeId)
        );
    }

    private <T> void addToTag(final CompoundTag tag,
                              final int index,
                              final ResourceInstance<T> entry,
                              final PlatformStorageChannelType<T> storageChannelType,
                              final ResourceLocation storageChannelTypeId) {
        final CompoundTag serialized = new CompoundTag();
        serialized.putString("t", storageChannelTypeId.toString());
        serialized.put("v", storageChannelType.toTag(entry.getResource()));
        serialized.putLong("a", entry.getAmount());
        tag.put("s" + index, serialized);
    }

    public void fromTag(final CompoundTag tag) {
        for (int i = 0; i < size(); ++i) {
            final String key = "s" + i;
            if (!tag.contains(key)) {
                continue;
            }
            final CompoundTag item = tag.getCompound(key);
            fromTag(i, item);
        }
    }

    private void fromTag(final int index, final CompoundTag tag) {
        final ResourceLocation storageChannelTypeId = new ResourceLocation(tag.getString("t"));
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().get(storageChannelTypeId).ifPresent(
            storageChannelType -> fromTag(index, tag, storageChannelType)
        );
    }

    private <T> void fromTag(final int index,
                             final CompoundTag tag,
                             final PlatformStorageChannelType<T> storageChannelType) {
        final long amount = tag.getLong("a");
        storageChannelType.fromTag(tag.getCompound("v")).ifPresent(resource -> setSilently(
            index,
            new ResourceInstance<>(new ResourceAmount<>(resource, amount), storageChannelType)
        ));
    }

    @Override
    public int getContainerSize() {
        return size();
    }

    @Override
    public boolean isEmpty() {
        if (!supportsItemSlotInteractions()) {
            return true;
        }
        for (int i = 0; i < size(); ++i) {
            if (get(i) != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(final int slot) {
        if (!supportsItemSlotInteractions()) {
            return ItemStack.EMPTY;
        }
        final ResourceInstance<?> entry = items[slot];
        if (entry != null) {
            return entry.getStackRepresentation();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(final int slot, final int amount) {
        if (!supportsItemSlotInteractions()) {
            return ItemStack.EMPTY;
        }
        final ResourceInstance<?> entry = get(slot);
        if (entry != null && entry.getResource() instanceof ItemResource item) {
            final long maxRemove = Math.min(amount, entry.getAmount());
            final long remainder = entry.getAmount() - maxRemove;
            if (remainder == 0) {
                remove(slot);
            } else {
                setAmount(slot, remainder);
            }
            return item.toItemStack(maxRemove);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(final int slot) {
        if (!supportsItemSlotInteractions()) {
            return ItemStack.EMPTY;
        }
        final ResourceInstance<?> entry = get(slot);
        if (entry != null && entry.getResource() instanceof ItemResource item) {
            final ItemStack stack = item.toItemStack();
            final long maxRemove = Math.min(stack.getMaxStackSize(), entry.getAmount());
            final long remainder = entry.getAmount() - maxRemove;
            if (remainder == 0) {
                remove(slot);
            } else {
                setAmount(slot, remainder);
            }
            return stack.copyWithCount((int) maxRemove);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(final int slot, final ItemStack itemStack) {
        if (!supportsItemSlotInteractions()) {
            return;
        }
        if (itemStack.isEmpty()) {
            remove(slot);
        } else {
            final ResourceAmount<ItemResource> resourceAmount = new ResourceAmount<>(
                ItemResource.ofItemStack(itemStack),
                itemStack.getCount()
            );
            set(slot, new ResourceInstance<>(resourceAmount, StorageChannelTypes.ITEM));
        }
    }

    @Override
    public void setChanged() {
        if (!supportsItemSlotInteractions()) {
            return;
        }
        if (listener != null) {
            listener.run();
        }
    }

    @Override
    public boolean stillValid(final Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        if (!supportsItemSlotInteractions()) {
            return;
        }
        for (int i = 0; i < size(); ++i) {
            removeSilently(i);
        }
        if (listener != null) {
            listener.run();
        }
    }
}
