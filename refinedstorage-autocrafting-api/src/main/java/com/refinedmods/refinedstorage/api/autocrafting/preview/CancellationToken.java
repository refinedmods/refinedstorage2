package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationHandler;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-beta.3")
public class CancellationToken implements CancellationHandler {
    public static final CancellationToken NONE = new CancellationToken() {
        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void cancel() {
            // no-op
        }
    };

    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    public void cancel() {
        cancelled.set(true);
    }
}
