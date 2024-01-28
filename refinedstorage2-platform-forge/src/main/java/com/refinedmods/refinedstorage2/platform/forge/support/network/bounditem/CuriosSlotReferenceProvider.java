package com.refinedmods.refinedstorage2.platform.forge.support.network.bounditem;

import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReferenceProvider;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosSlotReferenceProvider implements SlotReferenceProvider {
    private CuriosSlotReferenceProvider() {
    }

    @Override
    public List<SlotReference> find(final Player player, final Set<Item> validItems) {
        return CuriosApi.getCuriosInventory(player)
            .map(curiosInventory -> curiosInventory.findCurios("refinedstorage2"))
            .orElse(Collections.emptyList())
            .stream()
            .filter(slotResult -> validItems.contains(slotResult.stack().getItem()))
            .map(slotResult -> (SlotReference) new CuriosSlotReference(
                slotResult.slotContext().identifier(),
                slotResult.slotContext().index()
            ))
            .toList();
    }

    public static Optional<SlotReferenceProvider> create() {
        if (!ModList.get().isLoaded("curios")) {
            return Optional.empty();
        }
        return Optional.of(new CuriosSlotReferenceProvider());
    }
}
