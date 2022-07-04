package com.refinedmods.refinedstorage2.platform;

public class SimpleListener implements Runnable {
    private int changes;

    @Override
    public void run() {
        changes++;
    }

    public boolean isChanged() {
        return changes > 0;
    }

    public int getChanges() {
        return changes;
    }

    public void reset() {
        changes = 0;
    }
}
