package com.refinedmods.refinedstorage.common.support.slotreference;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReferenceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class CompositePlayerSlotReferenceProvider implements PlayerSlotReferenceProvider {
    private final List<PlayerSlotReferenceProvider> providers = new ArrayList<>(List.of(
        new InventoryPlayerSlotReferenceProvider()
    ));

    public void addProvider(final PlayerSlotReferenceProvider provider) {
        providers.add(provider);
    }

    @Override
    public List<PlayerSlotReference> find(final Player player, final Set<Item> validItems) {
        return providers.stream().flatMap(p -> p.find(player, validItems).stream()).toList();
    }

    public Optional<PlayerSlotReference> findForUse(final Player player,
                                                    final Item referenceItem,
                                                    final Set<Item> validItems) {
        final List<PlayerSlotReference> foundReferences = find(player, validItems);
        if (foundReferences.size() > 1) {
            player.sendSystemMessage(createTranslation(
                "item",
                "network_item.cannot_open_with_shortcut_due_to_duplicate",
                referenceItem.getName(referenceItem.getDefaultInstance())
            ).withStyle(ChatFormatting.RED));
            return Optional.empty();
        }
        if (foundReferences.isEmpty()) {
            player.sendSystemMessage(createTranslation(
                "item",
                "network_item.cannot_open_because_not_found",
                referenceItem.getName(referenceItem.getDefaultInstance())
            ).withStyle(ChatFormatting.RED));
            return Optional.empty();
        }
        return Optional.of(foundReferences.getFirst());
    }
}
