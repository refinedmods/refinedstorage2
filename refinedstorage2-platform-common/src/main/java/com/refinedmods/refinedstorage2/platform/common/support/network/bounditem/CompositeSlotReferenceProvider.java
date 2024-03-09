package com.refinedmods.refinedstorage2.platform.common.support.network.bounditem;

import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReferenceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class CompositeSlotReferenceProvider implements SlotReferenceProvider {
    private final List<SlotReferenceProvider> providers = new ArrayList<>(List.of(
        new InventorySlotReferenceProvider()
    ));

    public void addProvider(final SlotReferenceProvider provider) {
        providers.add(provider);
    }

    @Override
    public List<SlotReference> find(final Player player, final Set<Item> validItems) {
        return providers.stream().flatMap(p -> p.find(player, validItems).stream()).toList();
    }

    public Optional<SlotReference> findForUse(final Player player,
                                              final Item referenceItem,
                                              final Set<Item> validItems) {
        final List<SlotReference> foundReferences = find(player, validItems);
        if (foundReferences.size() > 1) {
            player.sendSystemMessage(createTranslation(
                "item",
                "network_item.cannot_open_with_shortcut_due_to_duplicate",
                referenceItem.getDescription()
            ).withStyle(ChatFormatting.RED));
            return Optional.empty();
        }
        if (foundReferences.isEmpty()) {
            player.sendSystemMessage(createTranslation(
                "item",
                "network_item.cannot_open_because_not_found",
                referenceItem.getDescription()
            ).withStyle(ChatFormatting.RED));
            return Optional.empty();
        }
        return Optional.of(foundReferences.get(0));
    }
}
