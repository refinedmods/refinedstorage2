package com.refinedmods.refinedstorage2.platform.common.internal.resource;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.screen.tooltip.MouseWithIconClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.util.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToLongFunction;
import javax.annotation.Nullable;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ResourceContainer {
    private final ResourceAmountTemplate<?>[] slots;
    private final ResourceContainerType type;
    private final ToLongFunction<ResourceTemplate<?>> maxAmountProvider;
    private final ResourceFactory<?> primaryResourceFactory;
    private final Set<ResourceFactory<?>> alternativeResourceFactories;

    @Nullable
    private Runnable listener;

    public ResourceContainer(final int size,
                             final ResourceContainerType type,
                             final ToLongFunction<ResourceTemplate<?>> maxAmountProvider,
                             final ResourceFactory<?> primaryResourceFactory,
                             final Set<ResourceFactory<?>> alternativeResourceFactories) {
        this.slots = new ResourceAmountTemplate<?>[size];
        this.type = type;
        this.maxAmountProvider = maxAmountProvider;
        this.primaryResourceFactory = primaryResourceFactory;
        this.alternativeResourceFactories = alternativeResourceFactories;
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

    public void setListener(@Nullable final Runnable listener) {
        this.listener = listener;
    }

    public void change(final int index, final ItemStack stack, final boolean tryAlternatives) {
        if (tryAlternatives) {
            for (final ResourceFactory<?> resourceFactory : alternativeResourceFactories) {
                final var result = resourceFactory.create(stack);
                if (result.isPresent()) {
                    set(index, result.get());
                    return;
                }
            }
        }
        primaryResourceFactory.create(stack).ifPresentOrElse(
            resource -> set(index, resource),
            () -> remove(index)
        );
    }

    public <T> void set(final int index, final ResourceAmountTemplate<T> resourceAmount) {
        setSilently(index, resourceAmount);
        changed();
    }

    private <T> void setSilently(final int index, final ResourceAmountTemplate<T> resourceAmount) {
        slots[index] = resourceAmount;
    }

    public long getAmount(final int index) {
        final ResourceAmountTemplate<?> resourceAmount = slots[index];
        if (resourceAmount == null) {
            return 0;
        }
        return resourceAmount.getAmount();
    }

    public void grow(final int index, final long amount) {
        CoreValidations.validateNotNegative(amount, "Amount to grow cannot be negative.");
        setAmount(index, getAmount(index) + amount);
    }

    public void shrink(final int index, final long amount) {
        CoreValidations.validateNotNegative(amount, "Amount to shrink cannot be negative.");
        setAmount(index, getAmount(index) - amount);
    }

    public void setAmount(final int index, final long amount) {
        if (!supportsAmount()) {
            return;
        }
        final ResourceAmountTemplate<?> resourceAmount = slots[index];
        if (resourceAmount == null) {
            return;
        }
        final long newAmount = MathHelper.clamp(amount, 0, getMaxAmount(resourceAmount));
        if (newAmount == 0) {
            remove(index);
        } else {
            slots[index] = resourceAmount.withAmount(newAmount);
        }
        changed();
    }

    public <T> long getMaxAmount(final ResourceAmountTemplate<T> resourceAmount) {
        return maxAmountProvider.applyAsLong(resourceAmount.getResourceTemplate());
    }

    public void remove(final int index) {
        removeSilently(index);
        changed();
    }

    private void removeSilently(final int index) {
        slots[index] = null;
    }

    public int size() {
        return slots.length;
    }

    @Nullable
    public ResourceAmountTemplate<?> get(final int index) {
        return slots[index];
    }

    public Set<Object> getUniqueTemplates() {
        final Set<Object> result = new HashSet<>();
        for (int i = 0; i < size(); ++i) {
            final ResourceAmountTemplate<?> resourceAmount = slots[i];
            if (resourceAmount == null) {
                continue;
            }
            result.add(resourceAmount.getResourceTemplate().resource());
        }
        return result;
    }

    public List<ResourceTemplate<?>> getTemplates() {
        final List<ResourceTemplate<?>> result = new ArrayList<>();
        for (int i = 0; i < size(); ++i) {
            final ResourceAmountTemplate<?> resourceAmount = slots[i];
            if (resourceAmount == null) {
                continue;
            }
            result.add(resourceAmount.getResourceTemplate());
        }
        return result;
    }

    public void writeToUpdatePacket(final FriendlyByteBuf buf) {
        for (final ResourceAmountTemplate<?> slot : slots) {
            if (slot == null) {
                buf.writeBoolean(false);
                continue;
            }
            writeToUpdatePacket(buf, slot);
        }
    }

    private <T> void writeToUpdatePacket(final FriendlyByteBuf buf, final ResourceAmountTemplate<T> resourceAmount) {
        final PlatformStorageChannelType<T> storageChannelType = resourceAmount.getStorageChannelType();
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getId(storageChannelType).ifPresentOrElse(id -> {
            buf.writeBoolean(true);
            buf.writeResourceLocation(id);
            storageChannelType.toBuffer(resourceAmount.getResource(), buf);
            buf.writeLong(resourceAmount.getAmount());
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
        setSilently(index, new ResourceAmountTemplate<>(resource, amount, storageChannelType));
    }

    public CompoundTag toTag() {
        final CompoundTag tag = new CompoundTag();
        for (int i = 0; i < size(); ++i) {
            final ResourceAmountTemplate<?> resourceAmount = slots[i];
            if (resourceAmount == null) {
                continue;
            }
            addToTag(tag, i, resourceAmount);
        }
        return tag;
    }

    private <T> void addToTag(final CompoundTag tag,
                              final int index,
                              final ResourceAmountTemplate<T> resourceAmount) {
        final PlatformStorageChannelType<T> storageChannelType = resourceAmount.getStorageChannelType();
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getId(storageChannelType).ifPresent(
            storageChannelTypeId -> addToTag(tag, index, resourceAmount, storageChannelType, storageChannelTypeId)
        );
    }

    private <T> void addToTag(final CompoundTag tag,
                              final int index,
                              final ResourceAmountTemplate<T> resourceAmount,
                              final PlatformStorageChannelType<T> storageChannelType,
                              final ResourceLocation storageChannelTypeId) {
        final CompoundTag serialized = new CompoundTag();
        serialized.putString("t", storageChannelTypeId.toString());
        serialized.put("v", storageChannelType.toTag(resourceAmount.getResource()));
        serialized.putLong("a", resourceAmount.getAmount());
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
            new ResourceAmountTemplate<>(resource, amount, storageChannelType)
        ));
    }

    public List<ClientTooltipComponent> getHelpTooltip(final ItemStack carried) {
        if (carried.isEmpty()) {
            return Collections.emptyList();
        }
        final List<ClientTooltipComponent> lines = new ArrayList<>();
        primaryResourceFactory.create(carried).ifPresent(primaryResourceInstance -> lines.add(
            new MouseWithIconClientTooltipComponent(
                MouseWithIconClientTooltipComponent.Type.LEFT,
                getResourceRendering(primaryResourceInstance.getResource()),
                null
            )
        ));
        for (final ResourceFactory<?> alternativeResourceFactory : alternativeResourceFactories) {
            final var result = alternativeResourceFactory.create(carried);
            result.ifPresent(alternativeResourceInstance -> lines.add(new MouseWithIconClientTooltipComponent(
                MouseWithIconClientTooltipComponent.Type.RIGHT,
                getResourceRendering(alternativeResourceInstance.getResource()),
                null
            )));
        }
        return lines;
    }

    public static <T> MouseWithIconClientTooltipComponent.IconRenderer getResourceRendering(final T resource) {
        return (graphics, x, y) -> PlatformApi.INSTANCE.getResourceRendering(resource).render(resource, graphics, x, y);
    }

    private void changed() {
        if (listener != null) {
            listener.run();
        }
    }

    public AbstractResourceContainerContainerAdapter toContainer() {
        if (type != ResourceContainerType.CONTAINER) {
            throw new UnsupportedOperationException();
        }
        return new AbstractResourceContainerContainerAdapter(this) {
            @Override
            public void setChanged() {
                changed();
            }
        };
    }

    public static ResourceContainer createForFilter() {
        return createForFilter(9);
    }

    public static ResourceContainer createForFilter(final int size) {
        return createForFilter(size, ResourceContainerType.FILTER);
    }

    public static ResourceContainer createForFilter(final int size, final ResourceContainerType type) {
        return new ResourceContainer(
            size,
            type,
            resource -> Long.MAX_VALUE,
            PlatformApi.INSTANCE.getItemResourceFactory(),
            PlatformApi.INSTANCE.getAlternativeResourceFactories()
        );
    }

    public static <T> ResourceContainer createForFilter(final ResourceFactory<T> resourceFactory) {
        return new ResourceContainer(
            9,
            ResourceContainerType.FILTER,
            resource -> Long.MAX_VALUE,
            resourceFactory,
            Collections.emptySet()
        );
    }
}
