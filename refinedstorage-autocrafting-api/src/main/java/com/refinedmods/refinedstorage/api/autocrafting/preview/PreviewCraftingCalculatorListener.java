package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.Amount;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorListener;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.NumberOverflowDuringCalculationException;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.PatternCycleDetectedException;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collections;

public class PreviewCraftingCalculatorListener implements CraftingCalculatorListener<PreviewBuilder> {
    private PreviewBuilder builder;

    private PreviewCraftingCalculatorListener(final PreviewBuilder builder) {
        this.builder = builder;
    }

    public static Preview calculatePreview(final CraftingCalculator calculator,
                                           final ResourceKey resource,
                                           final long amount) {
        final PreviewCraftingCalculatorListener listener = new PreviewCraftingCalculatorListener(
            PreviewBuilder.create()
        );
        try {
            calculator.calculate(resource, amount, listener);
        } catch (final PatternCycleDetectedException e) {
            return new Preview(PreviewType.CYCLE_DETECTED, Collections.emptyList(), e.getPattern().layout().outputs());
        } catch (final NumberOverflowDuringCalculationException e) {
            return new Preview(PreviewType.OVERFLOW, Collections.emptyList(), Collections.emptyList());
        }
        return listener.buildPreview();
    }

    @Override
    public CraftingCalculatorListener<PreviewBuilder> childCalculationStarted(final Pattern childPattern,
                                                                              final ResourceKey resource,
                                                                              final Amount amount) {
        final PreviewBuilder copy = builder.copy();
        copy.addToCraft(resource, amount.getTotal());
        return new PreviewCraftingCalculatorListener(copy);
    }

    @Override
    public void childCalculationCompleted(final CraftingCalculatorListener<PreviewBuilder> childListener) {
        this.builder = childListener.getData();
    }

    @Override
    public void childCalculationCancelled(final CraftingCalculatorListener<PreviewBuilder> childListener) {
        builder.cancelled();
    }

    @Override
    public void ingredientsExhausted(final ResourceKey resource, final long amount) {
        builder.addMissing(resource, amount);
    }

    @Override
    public void ingredientUsed(final Pattern ingredientPattern,
                               final int ingredientIndex,
                               final ResourceKey resource,
                               final long amount) {
        // no op
    }

    @Override
    public void ingredientExtractedFromStorage(final ResourceKey resource, final long amount) {
        builder.addAvailable(resource, amount);
    }

    @Override
    public PreviewBuilder getData() {
        return builder;
    }

    private Preview buildPreview() {
        return builder.build();
    }
}
