package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PendingAutocraftingRequests {
    private static final Logger LOGGER = LoggerFactory.getLogger(PendingAutocraftingRequests.class);

    private final Set<CancellationToken> cancellationTokens = new HashSet<>();

    public void add(final CancellationToken cancellationToken) {
        cancellationTokens.add(cancellationToken);
    }

    public void cancelAll() {
        if (!cancellationTokens.isEmpty()) {
            LOGGER.info("Cancelling {} pending autocrafting requests", cancellationTokens.size());
        }
        cancellationTokens.forEach(CancellationToken::cancel);
        cancellationTokens.clear();
    }
}
