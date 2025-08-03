package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceTag;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.packet.c2s.C2SPackets;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.autocrafting.patterngrid.AlternativesScreen.ALTERNATIVE_HEIGHT;
import static com.refinedmods.refinedstorage.common.autocrafting.patterngrid.AlternativesScreen.ALTERNATIVE_ROW_HEIGHT;
import static com.refinedmods.refinedstorage.common.autocrafting.patterngrid.AlternativesScreen.RESOURCES_PER_ROW;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.getTagTranslationKey;

class AlternativeContainerMenu extends AbstractResourceContainerMenu {
    private final List<Alternative> alternatives;
    private final ResourceSlot amountSlot;

    AlternativeContainerMenu(final ResourceSlot amountSlot) {
        super(null, 0);
        this.amountSlot = amountSlot.forAmountScreen(127, 48);
        final List<ResourceTag> tags = amountSlot.getResource() == null
            ? Collections.emptyList()
            : amountSlot.getResource().getTags();
        this.alternatives = tags.stream().map(tag -> new Alternative(
            tag.key().location(),
            getTagTranslationKey(tag.key()),
            tag.resources()
        )).toList();
        addSlot(this.amountSlot);
        addAlternativeSlots();
    }

    void filter(final String query) {
        final String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        alternatives.forEach(alternative -> {
            final boolean titleMatch = I18n.exists(alternative.getTranslationKey())
                && I18n.get(alternative.getTranslationKey()).trim().toLowerCase(Locale.ROOT).contains(normalizedQuery);
            final boolean idMatch = alternative.getId().toString().trim().toLowerCase(Locale.ROOT)
                .contains(normalizedQuery);
            alternative.setVisible(titleMatch || idMatch);
        });
    }

    private void addAlternativeSlots() {
        final int x = 8;
        for (int i = 0; i < alternatives.size(); ++i) {
            final int y = 122 + (i * ALTERNATIVE_HEIGHT);
            final Alternative alternative = alternatives.get(i);
            final ResourceContainer resources = ResourceContainerImpl.createForFilter(
                alternative.getResources().size()
            );
            for (int j = 0; j < resources.size(); ++j) {
                resources.set(j, new ResourceAmount(alternative.getResources().get(j), 1));
                final int row = j / RESOURCES_PER_ROW;
                final int col = j % RESOURCES_PER_ROW;
                final int slotX = x + 1 + 1 + col * 18;
                final int slotY = y + ALTERNATIVE_ROW_HEIGHT + (row * 18) + 1;
                final AlternativeSlot resourceSlot = new AlternativeSlot(resources, j, slotX, slotY);
                if (j < RESOURCES_PER_ROW) {
                    alternative.getMainSlots().add(resourceSlot);
                } else {
                    alternative.getOverflowSlots().add(resourceSlot);
                }
                addSlot(resourceSlot);
            }
        }
    }

    ResourceSlot getAmountSlot() {
        return amountSlot;
    }

    List<Alternative> getAlternatives() {
        return alternatives;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(final Player player) {
        return true;
    }

    void sendAllowedAlternatives(final Set<Alternative> allowedAlternatives) {
        C2SPackets.sendPatternGridAllowedAlternativesChange(
            amountSlot.getContainerSlot(),
            allowedAlternatives.stream().map(Alternative::getId).collect(Collectors.toSet())
        );
    }
}
