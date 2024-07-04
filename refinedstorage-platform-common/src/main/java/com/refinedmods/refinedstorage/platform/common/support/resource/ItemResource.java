package com.refinedmods.refinedstorage.platform.common.support.resource;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.platform.api.support.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceType;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public record ItemResource(Item item, DataComponentPatch components)
    implements PlatformResourceKey, FuzzyModeNormalizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemResource.class);

    public ItemResource(final Item item) {
        this(item, DataComponentPatch.EMPTY);
    }

    public ItemResource(final Item item, final DataComponentPatch components) {
        this.item = CoreValidations.validateNotNull(item, "Item must not be null");
        this.components = CoreValidations.validateNotNull(components, "Components must not be null");
    }

    public ItemStack toItemStack() {
        return toItemStack(1);
    }

    @SuppressWarnings("deprecation")
    public ItemStack toItemStack(final long amount) {
        if (amount > Integer.MAX_VALUE) {
            LOGGER.warn("Truncating too large amount for {} to fit into ItemStack {}", this, amount);
        }
        return new ItemStack(item.builtInRegistryHolder(), (int) amount, components);
    }

    @Override
    public ResourceKey normalize() {
        return new ItemResource(item);
    }

    @Override
    public long getInterfaceExportLimit() {
        return item.getDefaultMaxStackSize();
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceTypes.ITEM;
    }

    public static ItemResource ofItemStack(final ItemStack itemStack) {
        return new ItemResource(itemStack.getItem(), itemStack.getComponentsPatch());
    }
}
