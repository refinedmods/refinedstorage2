package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import org.apiguardian.api.API;

/**
 * A token that can be used to cancel an ongoing crafting calculation.
 * When {@link #isCancelled()} returns true, the calculation will stop as soon as possible.
 * Listeners will be notified via
 * {@link CraftingCalculatorListener#childCalculationCancelled(CraftingCalculatorListener)}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-beta.3")
public interface CancellationToken {
    CancellationToken NONE = new CancellationToken() {
        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void cancel() {
            // no op
        }
    };

    boolean isCancelled();

    void cancel();
}
