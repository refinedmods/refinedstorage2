package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.Amount;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationException;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorListener;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.NumberOverflowDuringCalculationException;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.PatternCycleDetectedException;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collections;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public class TreePreviewCraftingCalculatorListener implements CraftingCalculatorListener<TreePreviewNode> {
    @Nullable
    private TreePreviewNode currentNode;

    private TreePreviewCraftingCalculatorListener(@Nullable final TreePreviewNode currentNode) {
        this.currentNode = currentNode;
    }

    public static TreePreview calculateTree(final CraftingCalculator calculator,
                                            final ResourceKey resource,
                                            final long amount,
                                            final CancellationToken cancellationToken) {
        final TreePreviewCraftingCalculatorListener listener = new TreePreviewCraftingCalculatorListener(null);
        try {
            calculator.calculate(resource, amount, listener, cancellationToken);
        } catch (final PatternCycleDetectedException e) {
            return new TreePreview(PreviewType.CYCLE_DETECTED, null, e.getPattern().layout().outputs());
        } catch (final NumberOverflowDuringCalculationException e) {
            return new TreePreview(PreviewType.OVERFLOW, null, Collections.emptyList());
        } catch (final CancellationException e) {
            return new TreePreview(PreviewType.CANCELLED, null, Collections.emptyList());
        }
        return listener.buildPreview();
    }

    @Override
    public void rootCalculationStarted(final ResourceKey resource, final long amount) {
        currentNode = new TreePreviewNode(resource, amount);
    }

    @Override
    public CraftingCalculatorListener<TreePreviewNode> childCalculationStarted(final Pattern childPattern,
                                                                               final ResourceKey resource,
                                                                               final Amount amount) {
        return new TreePreviewCraftingCalculatorListener(new TreePreviewNode(resource));
    }

    @Override
    public void childCalculationCompleted(final CraftingCalculatorListener<TreePreviewNode> childListener) {
        if (currentNode == null) {
            currentNode = requireNonNull(childListener.getData());
            return;
        }
        currentNode.merge(childListener.getData());
    }

    @Override
    public void ingredientUsed(final Pattern ingredientPattern, final int ingredientIndex, final ResourceKey resource,
                               final long amount) {
        requireNonNull(currentNode).add(resource, amount);
    }

    @Override
    public void ingredientsExhausted(final ResourceKey resource, final long amount) {
        requireNonNull(currentNode).add(resource).missing(amount);
    }

    @Override
    public void ingredientExtractedFromStorage(final ResourceKey resource, final long amount) {
        requireNonNull(currentNode).add(resource).available(amount);
    }

    @Override
    public TreePreviewNode getData() {
        return requireNonNull(currentNode);
    }

    public TreePreview buildPreview() {
        return requireNonNull(currentNode).asRootInPreview();
    }
}
