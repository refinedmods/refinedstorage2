package com.refinedmods.refinedstorage2.platform.forge.integration.curios;

import com.refinedmods.refinedstorage2.platform.api.item.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.item.SlotReferenceProvider;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosSlotReferenceProvider implements SlotReferenceProvider {
    private CuriosSlotReferenceProvider() {
    }

    @Override
    public List<SlotReference> find(final Player player, final Set<Item> validItems) {
        return CuriosApi.getCuriosInventory(player)
            .resolve()
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
