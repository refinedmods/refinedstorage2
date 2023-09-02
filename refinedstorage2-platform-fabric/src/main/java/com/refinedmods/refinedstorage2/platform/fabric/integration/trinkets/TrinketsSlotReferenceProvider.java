package com.refinedmods.refinedstorage2.platform.fabric.integration.trinkets;

import com.refinedmods.refinedstorage2.platform.api.item.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.item.SlotReferenceProvider;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class TrinketsSlotReferenceProvider implements SlotReferenceProvider {
    @Override
    public List<SlotReference> find(final Player player, final Set<Item> validItems) {
        return TrinketsApi.getTrinketComponent(player)
            .map(trinkets -> trinkets.getEquipped(s -> validItems.contains(s.getItem())))
            .orElse(Collections.emptyList())
            .stream()
            .map(Tuple::getA)
            .map(trinketsSlotReference -> (SlotReference) new TrinketsSlotReference(
                trinketsSlotReference.inventory().getSlotType().getGroup(),
                trinketsSlotReference.inventory().getSlotType().getName(),
                trinketsSlotReference.index()
            ))
            .toList();
    }

    public static Optional<SlotReferenceProvider> create() {
        if (!isTrinketsLoaded()) {
            return Optional.empty();
        }
        return Optional.of(new TrinketsSlotReferenceProvider());
    }

    private static boolean isTrinketsLoaded() {
        try {
            Class.forName("dev.emi.trinkets.api.TrinketsApi");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
