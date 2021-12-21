package com.refinedmods.refinedstorage2.platform.fabric.internal;

import java.util.ArrayDeque;
import java.util.Deque;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class TickHandler {
    private static final Deque<Runnable> actions = new ArrayDeque<>();

    private TickHandler() {
    }

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(server -> runQueuedActions());
    }

    private static void runQueuedActions() {
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
