package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlotType;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslationAsHeading;

class ProcessingMatrixResourceSlot extends ResourceSlot {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingMatrixResourceSlot.class);

    private static final Component CLICK_TO_CONFIGURE_AMOUNT_AND_ALTERNATIVES =
        createTranslationAsHeading("gui", "pattern_grid.processing.click_to_configure_amount_and_alternatives");
    private static final MutableComponent INPUT_HELP = createTranslation(
        "gui",
        "pattern_grid.processing.input_slots_help"
    );
    private static final MutableComponent OUTPUT_HELP = createTranslation(
        "gui",
        "pattern_grid.processing.output_slots_help"
    );

    private final Supplier<PatternType> patternTypeSupplier;
    private final boolean input;
    private final int startY;
    private final int endY;

    private Set<Identifier> cachedAllowedAlternatives;

    ProcessingMatrixResourceSlot(final ResourceContainer resourceContainer,
                                 final int index,
                                 final int x,
                                 final int y,
                                 final boolean input,
                                 final Supplier<PatternType> patternTypeSupplier,
                                 final Pair<Integer, Integer> startEndY) {
        super(resourceContainer, index, input ? INPUT_HELP : OUTPUT_HELP, x, y, ResourceSlotType.FILTER_WITH_AMOUNT);
        this.patternTypeSupplier = patternTypeSupplier;
        this.cachedAllowedAlternatives =
            resourceContainer instanceof ProcessingMatrixInputResourceContainer inputResourceContainer
                ? inputResourceContainer.getAllowedTagIds(index)
                : Collections.emptySet();
        this.input = input;
        this.startY = startEndY.getLeft();
        this.endY = startEndY.getRight();
    }

    @Override
    public boolean broadcastChanges(final Player player) {
        final boolean resourceChanged = super.broadcastChanges(player);
        if (resourceContainer instanceof ProcessingMatrixInputResourceContainer inputResourceContainer
            && player instanceof ServerPlayer serverPlayer) {
            checkAllowedAlternativesChanged(inputResourceContainer, serverPlayer, resourceChanged);
        }
        return resourceChanged;
    }

    private void checkAllowedAlternativesChanged(
        final ProcessingMatrixInputResourceContainer container,
        final ServerPlayer serverPlayer,
        final boolean resourceChanged
    ) {
        final Set<Identifier> currentAllowedAlternatives = container.getAllowedTagIds(
            getContainerSlot()
        );
        // If the resource has changed, also re-send the allowed alternatives.
        // Even if only the amount changes, we need to re-send the allowed alternatives
        // as an amount change would reset the alternatives client-side.
        if (!currentAllowedAlternatives.equals(cachedAllowedAlternatives) || resourceChanged) {
            LOGGER.debug("Re-sending alternatives for resource slot {}", getContainerSlot());
            cachedAllowedAlternatives = new HashSet<>(currentAllowedAlternatives);
            S2CPackets.sendPatternGridAllowedAlternativesUpdate(
                serverPlayer,
                getContainerSlot(),
                currentAllowedAlternatives
            );
        }
    }

    boolean isInput() {
        return input;
    }

    @Override
    public boolean isActive() {
        final PatternType patternType = patternTypeSupplier.get();
        return patternType == PatternType.PROCESSING && y >= startY && y < endY;
    }

    @Override
    public boolean isHighlightable() {
        return false; // we render the highlight in the scissor render
    }

    @Override
    public Component getClickToConfigureAmountHelpTooltip() {
        if (input) {
            return CLICK_TO_CONFIGURE_AMOUNT_AND_ALTERNATIVES;
        }
        return super.getClickToConfigureAmountHelpTooltip();
    }
}
