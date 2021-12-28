package com.refinedmods.refinedstorage2.platform.common.util;

import java.util.ArrayDeque;
import java.util.Deque;

public final class TickHandler {
    private static final Deque<Runnable> actions = new ArrayDeque<>();

    private TickHandler() {
    }

    public static void runQueuedActions() {
        synchronized (actions) {
            Runnable action;
            while ((action = actions.poll()) != null) {
                action.run();
            }
        }
    }

    public static void runWhenReady(Runnable runnable) {
        synchronized (actions) {
            actions.add(runnable);
        }
    }
}
