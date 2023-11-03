package com.refinedmods.refinedstorage2.platform.common.util;

import java.util.ArrayDeque;
import java.util.Deque;

public final class ServerEventQueue {
    private static final Deque<Runnable> ACTIONS = new ArrayDeque<>();

    private ServerEventQueue() {
    }

    public static void runQueuedActions() {
        synchronized (ACTIONS) {
            Runnable action;
            while ((action = ACTIONS.poll()) != null) {
                action.run();
            }
        }
    }

    public static void queue(final Runnable runnable) {
        synchronized (ACTIONS) {
            ACTIONS.add(runnable);
        }
    }
}
