package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.preview.CancellationToken;

import java.util.HashSet;
import java.util.Set;

public class PendingAutocraftingRequests {
    private final Set<CancellationToken> cancellationTokens = new HashSet<>();

    public void add(final CancellationToken cancellationToken) {
        cancellationTokens.add(cancellationToken);
    }

    public void cancelAll() {
        cancellationTokens.forEach(CancellationToken::cancel);
        cancellationTokens.clear();
    }
}
