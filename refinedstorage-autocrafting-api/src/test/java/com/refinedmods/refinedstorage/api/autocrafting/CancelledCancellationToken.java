package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;

public class CancelledCancellationToken implements CancellationToken {
    @Override
    public boolean isCancelled() {
        return true;
    }

    @Override
    public void cancel() {
        // no op
    }
}
