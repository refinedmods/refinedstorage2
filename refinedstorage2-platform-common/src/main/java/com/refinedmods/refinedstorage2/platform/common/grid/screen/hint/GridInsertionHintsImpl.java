package com.refinedmods.refinedstorage2.platform.common.grid.screen.hint;

import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHints;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class GridInsertionHintsImpl implements GridInsertionHints {
    private final GridInsertionHint defaultHint;
    private final GridInsertionHint defaultAlternativeHint;
    private final List<GridInsertionHint> alternativeHints = new ArrayList<>();

    public GridInsertionHintsImpl(final GridInsertionHint defaultHint, final GridInsertionHint defaultAlternativeHint) {
        this.defaultHint = defaultHint;
        this.defaultAlternativeHint = defaultAlternativeHint;
    }

    public void addAlternativeHint(final GridInsertionHint hint) {
        alternativeHints.add(hint);
    }

    @Override
    public List<ClientTooltipComponent> getHints(final ItemStack carried) {
        final List<ClientTooltipComponent> hints = new ArrayList<>();
        defaultHint.getHint(carried).ifPresent(hints::add);
        alternativeHints.stream().flatMap(ah -> ah.getHint(carried).stream()).findFirst().ifPresentOrElse(
            hints::add,
            () -> defaultAlternativeHint.getHint(carried).ifPresent(hints::add)
        );
        return hints;
    }
}
