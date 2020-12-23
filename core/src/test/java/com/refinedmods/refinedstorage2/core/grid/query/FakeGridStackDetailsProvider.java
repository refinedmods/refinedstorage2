package com.refinedmods.refinedstorage2.core.grid.query;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;

public class FakeGridStackDetailsProvider implements GridStackDetailsProvider<ItemStack> {
    private final Map<Item, String> modNames = new HashMap<>();
    private final Map<Item, String> modIds = new HashMap<>();
    private final Map<Item, Set<String>> tags = new HashMap<>();

    public void setModName(Item item, String name) {
        modNames.put(item, name);
    }

    public void setModId(Item item, String name) {
        modIds.put(item, name);
    }

    public void setTags(Item item, String... tagsForItem) {
        tags.put(item, new HashSet<>(Arrays.asList(tagsForItem)));
    }

    @Override
    public GridStackDetails getDetails(ItemStack stack) {
        return new GridStackDetails(
            stack.getName().getString(),
            modIds.getOrDefault(stack.getItem(), "mc"),
            modNames.getOrDefault(stack.getItem(), "Minecraft"),
            tags.getOrDefault(stack.getItem(), Collections.emptySet())
        );
    }
}
