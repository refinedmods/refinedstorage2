package com.refinedmods.refinedstorage.common.grid.view;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryMapper;
import com.refinedmods.refinedstorage.common.api.grid.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResourceAttributeKey;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractItemGridResourceRepositoryMapper implements ResourceRepositoryMapper<GridResource> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractItemGridResourceRepositoryMapper.class);

    @Override
    public GridResource apply(final ResourceKey resource) {
        final ItemResource itemResource = (ItemResource) resource;
        final Item item = itemResource.item();
        final ItemStack itemStack = itemResource.toItemStack();
        final String name = item.getName(itemStack).getString();
        final String modId = getModId(itemStack);
        final String modName = getModName(modId).orElse("");
        final Map<GridResourceAttributeKey, Supplier<Set<String>>> attributes = Map.of(
            GridResourceAttributeKeys.MOD_ID, Suppliers.ofInstance(Set.of(modId)),
            GridResourceAttributeKeys.MOD_NAME, Suppliers.ofInstance(Set.of(modName)),
            GridResourceAttributeKeys.TAGS, Suppliers.memoize(() -> getTags(item)),
            GridResourceAttributeKeys.TOOLTIP, Suppliers.memoize(() -> Set.of(getTooltip(itemStack)))
        );
        return new ItemGridResource(
            itemResource,
            itemStack,
            name,
            k -> attributes.getOrDefault(k, Collections::emptySet).get()
        );
    }

    private String getTooltip(final ItemStack itemStack) {
        try {
            return itemStack
                .getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.ADVANCED)
                .stream()
                .map(Component::getString)
                .collect(Collectors.joining("\n"));
        } catch (final Throwable t) {
            LOGGER.warn("Failed to get tooltip for item {}", itemStack, t);
            return "";
        }
    }

    private Set<String> getTags(final Item item) {
        final Stream<String> itemTags = BuiltInRegistries.ITEM.getResourceKey(item)
            .flatMap(BuiltInRegistries.ITEM::get)
            .stream()
            .flatMap(Holder::tags)
            .map(tagKey -> tagKey.location().getPath());

        if (item instanceof BlockItem blockItem) {
            final Stream<String> blockTags = BuiltInRegistries.BLOCK.getResourceKey(blockItem.getBlock())
                .flatMap(BuiltInRegistries.BLOCK::get)
                .stream()
                .flatMap(Holder::tags)
                .map(tagKey -> tagKey.location().getPath());

            return Stream.concat(itemTags, blockTags).collect(Collectors.toSet());
        } else {
            return itemTags.collect(Collectors.toSet());
        }
    }

    public abstract String getModId(ItemStack itemStack);

    public abstract Optional<String> getModName(String modId);
}
